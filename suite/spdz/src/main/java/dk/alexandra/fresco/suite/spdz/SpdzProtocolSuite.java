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
import dk.alexandra.fresco.framework.Reporter;
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

  private int gatesEvaluated = 0;
  private static final int macCheckThreshold = 100000;
  private SpdzConfiguration spdzConf;

  private long totalMacTime;
  private long lastMacEnd;
  private long totalNoneMacTime;
  private int totalSizeOfValues;

  public SpdzProtocolSuite(SpdzConfiguration spdzConf) {
    this.spdzConf = spdzConf;
  }

  @Override
  public ProtocolFactory init(SpdzResourcePool resourcePool) {
    int maxBitLength = spdzConf.getMaxBitLength();
    return new SpdzFactory(resourcePool.getStore(), resourcePool.getMyId(), maxBitLength);
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, SCENetwork sceNetwork) {
    try {
      MACCheck(null, resourcePool, sceNetwork);
      this.gatesEvaluated = 0;
    } catch (IOException e) {
      throw new MPCException("Could not complete MACCheck.", e);
    }
  }

  @Override
  public void destroy() {

  }

  @Override
  public RoundSynchronization createRoundSynchronization() {
    return new SpdzRoundSynchronization();
  }

  private int logCount = 0;
  private int synchronizeCalls;
  private long sumOfGates = 0;

  private void MACCheck(Map<Integer, BigInteger> commitments, SpdzResourcePool resourcePool,
      SCENetwork sceNetworks) throws IOException {

    long start = System.currentTimeMillis();
    if (lastMacEnd > 0) {
      totalNoneMacTime += start - lastMacEnd;
    }

    SpdzStorage storage = resourcePool.getStore();
    sumOfGates += SpdzProtocolSuite.this.gatesEvaluated;
    totalSizeOfValues += storage.getOpenedValues().size();

    if (logCount++ > 1500) {
      Reporter.info("MacChecking(" + logCount + ")"
          + ", AverageGateSize=" + (logCount > 0 ? sumOfGates / logCount : "?")
          + ", OpenedValuesSize=" + totalSizeOfValues
          + ", SynchronizeCalls=" + synchronizeCalls
          + ", MacTime=" + totalMacTime
          + ", noneMacTime=" + totalNoneMacTime);
      logCount = 0;
      synchronizeCalls = 0;
      sumOfGates = 0;
      totalSizeOfValues = 0;
    }
    SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(
        resourcePool.getSecureRandom(),
        resourcePool.getMessageDigest(),
        storage,
        commitments, resourcePool.getModulus());

    int batchSize = 128;

    do {
      ProtocolCollectionList protocolCollectionList =
          new ProtocolCollectionList(batchSize);
      macCheck.getNextProtocols(protocolCollectionList);

      BatchedStrategy.processBatch(protocolCollectionList, sceNetworks, 0, resourcePool);
    } while (macCheck.hasNextProtocols());

    //reset boolean value
    resourcePool.setOutputProtocolInBatch(false);
    this.gatesEvaluated = 0;
    totalMacTime += System.currentTimeMillis() - start;
    lastMacEnd = System.currentTimeMillis();
  }

  private class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

    private Map<Integer, BigInteger> commitments;
    boolean commitDone = false;
    boolean openDone = false;
    int roundNumber = 0;
    private SpdzCommitProtocol commitProtocol;
    private SpdzOpenCommitProtocol openProtocol;

    @Override
    public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool,
        SCENetwork sceNetwork)
        throws MPCException {
      SpdzProtocolSuite.this.gatesEvaluated += gatesEvaluated;
      SpdzProtocolSuite.this.synchronizeCalls++;
      if (SpdzProtocolSuite.this.gatesEvaluated > macCheckThreshold || resourcePool
          .isOutputProtocolInBatch()) {
        try {
          MACCheck(openDone ? this.commitments : null, resourcePool, sceNetwork);
          this.commitments = null;
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