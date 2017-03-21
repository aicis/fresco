/*******************************************************************************
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

import java.math.BigInteger;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.CloseIntProtocol;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class SpdzInputProtocol extends SpdzNativeProtocol implements CloseIntProtocol {

	protected SpdzInputMask inputMask; // is opened by this gate.
	protected BigInteger input;
	private BigInteger value_masked;
	protected SpdzSInt out;
	protected int inputter;
	private byte[] digest;

	public SpdzInputProtocol(OInt input, SInt out, int inputter) {
		this.input = (input == null) ? null : input.getValue();
		this.out = (SpdzSInt) out;
		this.inputter = inputter;
	}

	public SpdzInputProtocol(BigInteger input, SpdzSInt out, int inputter) {
		this.input = input;
		this.out = out;
		this.inputter = inputter;
	}

	public SpdzInputProtocol(BigInteger input, SInt out, int inputter) {
		this.input = input;
		this.out = (SpdzSInt) out;
		this.inputter = inputter;
	}

	public int getInputter() {
		return inputter;
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {		
		int myId = resourcePool.getMyId();
		int players = resourcePool.getNoOfParties();
		BigInteger modulus = Util.getModulus();
		SpdzProtocolSuite spdzPii = SpdzProtocolSuite
				.getInstance(myId);
		SpdzStorage storage = spdzPii.getStore(network.getThreadId());
		switch (round) {
		case 0:
			this.inputMask = storage.getSupplier().getNextInputMask(this.inputter);
			if (myId == this.inputter) {
				BigInteger bcValue = this.input.subtract(this.inputMask.getRealValue());
				bcValue = bcValue.mod(modulus);	
				network.sendToAll(BigIntegerWithFixedLengthSerializer.toBytes(bcValue, Util.getModulusSize()));
			}
			network.expectInputFromPlayer(inputter);
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			this.value_masked = BigIntegerWithFixedLengthSerializer.toBigInteger(network.receive(inputter), Util.getModulusSize());
			this.digest = sendBroadcastValidation(
					spdzPii.getMessageDigest(network.getThreadId()), network,
					value_masked, players);
			network.expectInputFromAll();
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 2:
			boolean validated = receiveBroadcastValidation(network, digest);
			if (!validated) {
				throw new MPCException("Broadcast digests did not match");
			}
			SpdzElement value_masked_elm = new SpdzElement(value_masked,
					storage.getSSK().multiply(value_masked)
							.mod(Util.getModulus()));
			this.out.value = this.inputMask.getMask().add(value_masked_elm,
					myId);
			return EvaluationStatus.IS_DONE;
		}
		throw new MPCException("Cannot evaluate rounds larger than 2");
	}

	@Override
	public String toString() {
		return "SpdzInputGate(" + input + ", " + out + ")";
	}

	@Override
	public Value[] getInputValues() {
		// No point in getting input from an input gate...
		return null;
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}

}
