/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

public class SpdzOpenCommitGate extends SpdzNativeProtocol {

	protected SpdzCommitment commitment;
	protected Map<Integer, BigInteger> ss;
	protected Map<Integer, BigInteger> commitments;
	private boolean openingValidated;
	private boolean done = false;
	private byte[] digest;

	public SpdzOpenCommitGate(SpdzCommitment commitment,
			Map<Integer, BigInteger> commitments, Map<Integer, BigInteger> ss) {
		this.commitment = commitment;
		this.commitments = commitments;
		this.ss = ss;
	}

	@Override
	public Value[] getInputValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value[] getOutputValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		SpdzProtocolSuite spdzPii = SpdzProtocolSuite
				.getInstance(resourcePool.getMyId());
		int players = resourcePool.getNoOfParties();
		switch (round) {
		case 0: // Send your opening to all players
			BigInteger value = this.commitment.getValue();
			BigInteger randomness = this.commitment.getRandomness();
			BigInteger[] opening = new BigInteger[] { value, randomness };
			network.sendToAll(opening);
			network.expectInputFromAll();
			break;
		case 1: // Receive openings from all parties and check they are valid
			Map<Integer, BigInteger[]> openings = receiveFromAllMap(network);
			openingValidated = true;
			BigInteger[] broadcastMessages = new BigInteger[2 * openings.size()];
			for (int i : openings.keySet()) {
				BigInteger[] open = openings.get(i);
				BigInteger com = commitments.get(i);
				boolean validate = SpdzCommitment.checkCommitment(
						spdzPii.getMessageDigest(network.getThreadId()), com,
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
						spdzPii.getMessageDigest(network.getThreadId()),
						network, Arrays.asList(broadcastMessages), players);
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
		EvaluationStatus status = done ? EvaluationStatus.IS_DONE
				: EvaluationStatus.HAS_MORE_ROUNDS;
		return status;
	}
}
