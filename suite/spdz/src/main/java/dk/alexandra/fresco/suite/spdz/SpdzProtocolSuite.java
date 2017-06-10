/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollectionList;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.gates.SpdzCommitProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOpenCommitProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SpdzProtocolSuite implements ProtocolSuite<SpdzResourcePool> {

  private static final int macCheckThreshold = 100000;
  private SpdzConfiguration spdzConf;

  public SpdzProtocolSuite(SpdzConfiguration spdzConf) {
    this.spdzConf = spdzConf;
  }

  @Override
  public ProtocolFactory init(SpdzResourcePool resourcePool) {
    int maxBitLength = spdzConf.getMaxBitLength();
    return new SpdzFactory(resourcePool.getStore(), resourcePool.getMyId(), maxBitLength);
  }

  @Override
  public void destroy() {

  }

  @Override
  public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
    return new SpdzRoundSynchronization();
  }


  private class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

    boolean commitDone = false;
    boolean openDone = false;
    int roundNumber = 0;
    private SpdzCommitProtocol commitProtocol;
    private SpdzOpenCommitProtocol openProtocol;

    private void MACCheck(SpdzResourcePool resourcePool,
        SCENetwork sceNetworks) throws IOException {

      SpdzStorage storage = resourcePool.getStore();

      SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(
          resourcePool.getSecureRandom(),
          resourcePool.getMessageDigest(),
          storage,
          null, resourcePool.getModulus());

      int batchSize = 128;

      do {
        ProtocolCollectionList protocolCollectionList =
            new ProtocolCollectionList(batchSize);
        macCheck.getNextProtocols(protocolCollectionList);

        BatchedStrategy.processBatch(protocolCollectionList, sceNetworks, 0, resourcePool);
      } while (macCheck.hasNextProtocols());

      //reset boolean value
      resourcePool.setOutputProtocolInBatch(false);
      commitDone = false;
      openDone = false;
      roundNumber = 0;
    }

    @Override
    public void finishedEval(SpdzResourcePool resourcePool, SCENetwork sceNetwork) {
      try {
        MACCheck(resourcePool, sceNetwork);
      } catch (IOException e) {
        throw new MPCException("Could not complete MACCheck.", e);
      }
    }

    @Override
    public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool,
        SCENetwork sceNetwork)
        throws MPCException {
      gatesEvaluated += gatesEvaluated;
      if (gatesEvaluated > macCheckThreshold || resourcePool.isOutputProtocolInBatch()) {
        try {
          MACCheck(resourcePool, sceNetwork);
        } catch (IOException e) {
          throw new MPCException("Could not complete MACCheck.", e);
        }
      }
    }

    @Override
    public boolean roundFinished(int round, SpdzResourcePool resourcePool, SCENetwork sceNetwork)
        throws MPCException {
      if (resourcePool.isOutputProtocolInBatch()) {
        checkInit(resourcePool);

        if (!commitDone) {
          NativeProtocol.EvaluationStatus evaluate = commitProtocol
              .evaluate(round, resourcePool, sceNetwork);
          roundNumber = round + 1;
          commitDone = evaluate.equals(NativeProtocol.EvaluationStatus.IS_DONE);
          return false;
        }
        if (!openDone) {
          NativeProtocol.EvaluationStatus evaluate = openProtocol
              .evaluate(round - roundNumber, resourcePool, sceNetwork);
          openDone = evaluate.equals(NativeProtocol.EvaluationStatus.IS_DONE);
        }
        return openDone;
      }
      return true;
    }

    private void checkInit(SpdzResourcePool resourcePool) {
      BigInteger modulus = resourcePool.getModulus();
      Random rand = resourcePool.getRandom();
      MessageDigest messageDigest = resourcePool.getMessageDigest();

      BigInteger s = new BigInteger(modulus.bitLength(), rand).mod(modulus);
      SpdzCommitment commitment = new SpdzCommitment(messageDigest, s, rand);
      Map<Integer, BigInteger> comms = new HashMap<>();
      commitProtocol = new SpdzCommitProtocol(commitment, comms);
      Map<Integer, BigInteger> commitments = new HashMap<>();
      openProtocol = new SpdzOpenCommitProtocol(commitment, comms, commitments);
    }
  }
}