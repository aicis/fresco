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
import java.util.List;
import java.util.Map;

public class SpdzCommitProtocol extends SpdzNativeProtocol<Void> {

	private SpdzCommitment commitment;
	private Map<Integer, BigInteger> comms;
	private boolean done = false;
	private byte[] broadcastDigest;

	public SpdzCommitProtocol(SpdzCommitment commitment,
			Map<Integer, BigInteger> comms) {
		this.commitment = commitment;
		this.comms = comms;
	}

	@Override
	public Void out() {
		return null;
	}

	@Override
	public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
			SCENetwork network) {
		int players = spdzResourcePool.getNoOfParties();
		BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
		switch (round) {
		case 0:
			network.sendToAll(serializer
					.toBytes(commitment.getCommitment(spdzResourcePool.getModulus())));
			network.expectInputFromAll();
			break;
		case 1:
			List<ByteBuffer> commitments = network.receiveFromAll();
			for (int i = 0; i < commitments.size(); i++) {
				comms.put(i + 1, serializer.toBigInteger(commitments.get(i)));
			}
			if (players < 3) {
				done = true;
			} else {
				broadcastDigest = sendBroadcastValidation(
						spdzResourcePool.getMessageDigest(), network, comms.values()
				);
				network.expectInputFromAll();
			}
			break;
		case 2:
			boolean validated = receiveBroadcastValidation(network,
					broadcastDigest);
			if (!validated) {
				throw new MPCException(
						"Broadcast of commitments was not validated. Abort protocol.");
			}
			done = true;
			break;
		default:
			throw new MPCException("No further rounds.");
		}
		if (done) {
			return EvaluationStatus.IS_DONE;
		} else {
			return EvaluationStatus.HAS_MORE_ROUNDS;
		}
	}
}
