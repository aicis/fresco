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
package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzOpenCommitProtocol extends SpdzNativeProtocol<Map<Integer, BigInteger>> {

  private SpdzCommitment commitment;
  private Map<Integer, BigInteger> ss;
  private Map<Integer, BigInteger> commitments;
  private boolean openingValidated;
  private boolean done = false;
  private byte[] digest;

  public SpdzOpenCommitProtocol(SpdzCommitment commitment,
      Map<Integer, BigInteger> commitments, Map<Integer, BigInteger> ss) {
    this.commitment = commitment;
    this.commitments = commitments;
    this.ss = ss;
  }

  @Override
  public Map<Integer, BigInteger> getOutput() {
    return ss;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      SCENetwork network) {
    int players = spdzResourcePool.getNoOfParties();
    switch (round) {
      case 0: // Send your opening to all players
        BigInteger value = this.commitment.getValue();
        BigInteger randomness = this.commitment.getRandomness();
        BigInteger[] opening = new BigInteger[]{value, randomness};
        network.sendToAll(BigIntegerSerializer.toBytes(opening));
        network.expectInputFromAll();
        break;
      case 1: // Receive openings from all parties and check they are valid
        List<ByteBuffer> buffers = network.receiveFromAll();
        Map<Integer, BigInteger[]> openings = new HashMap<>();
        for (int i = 0; i < buffers.size(); i++) {
          opening = BigIntegerSerializer.toBigIntegers(buffers.get(i), 2);
          openings.put(i + 1, opening);
        }

        openingValidated = true;
        BigInteger[] broadcastMessages = new BigInteger[2 * openings.size()];
        for (int i : openings.keySet()) {
          BigInteger[] open = openings.get(i);
          BigInteger com = commitments.get(i);
          boolean validate = checkCommitment(
              spdzResourcePool, com,
              open[0], open[1]);
          openingValidated = openingValidated && validate;
          ss.put(i, open[0]);
          broadcastMessages[(i - 1) * 2] = open[0];
          broadcastMessages[(i - 1) * 2 + 1] = open[1];
        }
        if (players < 3) {
          if (!openingValidated) {
            throw new MPCException("Opening commitments failed.");
          }
          done = true;
        } else {
          digest = sendBroadcastValidation(
              spdzResourcePool.getMessageDigest(),
              network, Arrays.asList(broadcastMessages));
          network.expectInputFromAll();
        }
        break;
      case 2: // If more than three players check if openings where
        // broadcasted correctly
        boolean validated = receiveBroadcastValidation(network, digest);
        if (!(validated && openingValidated)) {
          throw new MPCException("Opening commitments failed.");
        }
        done = true;
        break;
      default:
        throw new MPCException("No more rounds to evaluate.");
    }
    if (done) {
      return EvaluationStatus.IS_DONE;
    } else {
      return EvaluationStatus.HAS_MORE_ROUNDS;
    }
  }

  private boolean checkCommitment(SpdzResourcePool spdzResourcePool, BigInteger commitment,
      BigInteger value, BigInteger randomness) {
    MessageDigest messageDigest = spdzResourcePool.getMessageDigest();
    messageDigest.update(value.toByteArray());
    messageDigest.update(randomness.toByteArray());
    BigInteger testSubject = new BigInteger(messageDigest.digest())
        .mod(spdzResourcePool.getModulus());
    return commitment.equals(testSubject);
  }
}
