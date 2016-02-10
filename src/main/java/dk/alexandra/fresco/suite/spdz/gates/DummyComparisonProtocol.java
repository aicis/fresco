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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class DummyComparisonProtocol implements ComparisonProtocol {

	private final SpdzSInt a, b, result;
	private final BasicNumericFactory provider;
	private ProtocolProducer currGP;
	private boolean done = false;

	public DummyComparisonProtocol(SInt a, SInt b, SInt result,
			BasicNumericFactory provider) {
		this.a = (SpdzSInt) a;
		this.b = (SpdzSInt) b;
		this.result = (SpdzSInt) result;
		this.provider = provider;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (currGP == null) {
			OInt a_open = provider.getOInt();
			OInt b_open = provider.getOInt();
			OpenIntProtocol openA = provider.getOpenProtocol(a, a_open);
			OpenIntProtocol openB = provider.getOpenProtocol(b, b_open);
			SpdzOInt a_open_ = (SpdzOInt) a_open;
			SpdzOInt b_open_ = (SpdzOInt) b_open;

			DummyInternalChooseGate chooseGate = new DummyInternalChooseGate(
					a_open_, b_open_, result);
			ParallelProtocolProducer parrGP = new ParallelProtocolProducer(openA, openB);
			currGP = new SequentialProtocolProducer(parrGP, chooseGate);
		}
		if (currGP.hasNextProtocols()) {
			pos = currGP.getNextProtocols(gates, pos);
		} else if (!currGP.hasNextProtocols()) {
			currGP = null;
			done = true;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return !done;
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

	private class DummyInternalChooseGate extends SpdzNativeProtocol {

		private SpdzOInt a, b;
		private SpdzSInt result;

		public DummyInternalChooseGate(SpdzOInt a, SpdzOInt b, SInt result) {
			this.a = a;
			this.b = b;
			this.result = (SpdzSInt) result;
		}

		@Override
		public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
				SCENetwork network) {
			SpdzProtocolSuite spdzPii = SpdzProtocolSuite
					.getInstance(resourcePool.getMyId());
			SpdzOInt min = null;
			if (compareModP(a.getValue(), b.getValue()) <= 0) {
				min = new SpdzOInt(BigInteger.ONE);
			} else {
				min = new SpdzOInt(BigInteger.ZERO);
			}
			SpdzElement elm;
			if (min.getValue().equals(BigInteger.ONE)) {
				if (resourcePool.getMyId() == 1) {
					elm = new SpdzElement(BigInteger.ONE, min.getValue()
							.multiply(spdzPii.getStore(network.getThreadId()).getSSK()));
				} else {
					elm = new SpdzElement(BigInteger.ZERO, min.getValue()
							.multiply(spdzPii.getStore(network.getThreadId()).getSSK()));
				}
			} else {
				elm = new SpdzElement(BigInteger.ZERO, BigInteger.ZERO);
			}
			this.result.value = elm;
			return EvaluationStatus.IS_DONE;
		}

		/**
		 * @param a
		 * @param b
		 * @return a comparison where numbers (P - a) that are larger than ((P -
		 *         1) / 2) are interpreted as the negative number (- a)
		 */
		private int compareModP(BigInteger a, BigInteger b) {
			BigInteger realA = a;
			BigInteger realB = b;
			BigInteger halfPoint = Util.getModulus().subtract(BigInteger.ONE)
					.divide((BigInteger.valueOf(2)));
			if (a.compareTo(halfPoint) > 0) {
				realA = a.subtract(Util.getModulus());
			}
			if (b.compareTo(halfPoint) > 0) {
				realB = b.subtract(Util.getModulus());
			}
			return realA.compareTo(realB);
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

	}
}
