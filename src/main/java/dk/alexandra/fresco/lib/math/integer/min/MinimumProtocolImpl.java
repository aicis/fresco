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
package dk.alexandra.fresco.lib.math.integer.min;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.compare.ConditionalSelectCircuit;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractCircuit;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.AppendableProtocolProducer;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;

public class MinimumProtocolImpl implements MinimumProtocol {

	private SInt[] xs, cs;
	private SInt m;
	private ProtocolProducer currGP;
	private LPFactory lpProvider;
	private BasicNumericFactory numericProvider;
	private final int k;
	private boolean done = false;

	public MinimumProtocolImpl(SInt[] xs, SInt m, SInt[] cs,
			LPFactory lpProvider, BasicNumericFactory numericProvider) {
		if (xs.length != cs.length) {
			throw new MPCException(
					"Min circuit: Output array should be same size as intput array");
		}
		this.k = xs.length;
		this.xs = xs;
		this.cs = cs;
		this.m = m;
		this.lpProvider = lpProvider;
		this.numericProvider = numericProvider;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (currGP == null) {
			if (this.k == 1) {
				throw new MPCException(
						"MinimumCircuit. k should never be 1. Just use the one you gave me as the minimum, fool ;)");
			} else if (this.k == 2) {
				ComparisonProtocol comp = lpProvider.getComparisonCircuit(
						this.xs[0], this.xs[1], this.cs[0], false);
				ConditionalSelectCircuit cond = lpProvider
						.getConditionalSelectCircuit(this.cs[0], this.xs[0],
								this.xs[1], this.m);
				SInt one = numericProvider.getSInt(1);
				SubtractCircuit subtract = numericProvider.getSubtractCircuit(
						one, this.cs[0], this.cs[1]);
				currGP = new SequentialProtocolProducer(new Protocol[] { comp, cond,
						subtract });
			} else if (this.k == 3) {
				SInt c1_prime = numericProvider.getSInt();
				ComparisonProtocol comp1 = lpProvider.getComparisonCircuit(
						this.xs[0], this.xs[1], c1_prime, false);
				SInt m1 = numericProvider.getSInt();
				ConditionalSelectCircuit cond1 = lpProvider
						.getConditionalSelectCircuit(c1_prime, this.xs[0],
								this.xs[1], m1);

				SInt c2_prime = numericProvider.getSInt();
				ComparisonProtocol comp2 = lpProvider.getComparisonCircuit(m1,
						this.xs[2], c2_prime, false);
				ConditionalSelectCircuit cond2 = lpProvider
						.getConditionalSelectCircuit(c2_prime, m1, this.xs[2],
								this.m);

				MultProtocol mult1 = numericProvider.getMultCircuit(c1_prime,
						c2_prime, this.cs[0]);
				SubtractCircuit sub1 = numericProvider.getSubtractCircuit(
						c2_prime, this.cs[0], this.cs[1]);
				SInt one = numericProvider.getSInt(1);
				SInt tmp = numericProvider.getSInt();
				SubtractCircuit sub2 = numericProvider.getSubtractCircuit(one,
						this.cs[0], tmp);
				SubtractCircuit sub3 = numericProvider.getSubtractCircuit(tmp,
						this.cs[1], this.cs[2]);

				SequentialProtocolProducer seqGP = new SequentialProtocolProducer(
						comp1, cond1, comp2, cond2, mult1);
				ParallelProtocolProducer parGP = new ParallelProtocolProducer(sub1,
						sub2);
				currGP = new SequentialProtocolProducer(seqGP, parGP, sub3);

			} else { // k > 3
				currGP = new RecursionPart();				
			}
		}
		if (currGP.hasNextProtocols()) {
			pos = currGP.getNextProtocols(gates, pos);
		} else if (!currGP.hasNextProtocols()) {
			currGP = null;
			done = true;
		}
		return pos;
	}
	
	private class RecursionPart extends AbstractRoundBasedProtocol {

		private int round = 0;
		private SInt m1;		
		private SInt m2;
		private SInt[] cs1_prime;
		private SInt[] cs2_prime;
		
		@Override
		public ProtocolProducer nextGateProducer() {
			ProtocolProducer gp = null;
			if (round == 0) {
				int k1 = k / 2;
				int k2 = k - k1;
				SInt[] X1 = new SInt[k1];
				SInt[] X2 = new SInt[k2];
				System.arraycopy(xs, 0, X1, 0, X1.length);
				System.arraycopy(xs, X1.length, X2, 0, X2.length);
				cs1_prime = new SInt[k1];
				cs2_prime = new SInt[k2];
				m1 = numericProvider.getSInt();
				m2 = numericProvider.getSInt();
				System.arraycopy(cs, 0, cs1_prime, 0, k1);
				System.arraycopy(cs, k1, cs2_prime, 0, k2);
				MinimumProtocol min1 = lpProvider.getMinimumCircuit(X1, m1,
						cs1_prime);
				MinimumProtocol min2 = lpProvider.getMinimumCircuit(X2, m2,
						cs2_prime);
				gp = new ParallelProtocolProducer(min1, min2);
				round++;
			} else if (round == 1){
				SInt c = numericProvider.getSInt();
				ComparisonProtocol comp = lpProvider.getComparisonCircuit(m1,
						m2, c, false);
				ConditionalSelectCircuit cond = lpProvider
						.getConditionalSelectCircuit(c, m1, m2, m);
				SInt one = numericProvider.getSInt(1);
				SInt oneMinusC = numericProvider.getSInt();
				SubtractCircuit subtract = numericProvider.getSubtractCircuit(
						one, c, oneMinusC);
				VectorScale scale1 = new VectorScale(c, cs1_prime, cs, 0);
				VectorScale scale2 = new VectorScale(oneMinusC, cs2_prime, cs, k/2);
				ProtocolProducer scale = new ParallelProtocolProducer(scale1, scale2);
				currGP = new SequentialProtocolProducer(comp, cond,	subtract, scale);
				round++;
			} else {
				cs1_prime = null;
				cs2_prime = null;
				m1 = null;
				m2 = null;
			}
			return gp;
		}		
	}
	
	private class VectorScale extends AbstractSimpleProtocol {

		SInt scale;
		SInt[] vector;
		SInt[] output;
		int from;
			
		public VectorScale(SInt scale, SInt[] vector, SInt[] output, int from) {
			this.scale = scale;
			this.vector = vector;
			this.output = output;
			this.from = from;
		}
		
		
		
		@Override
		protected ProtocolProducer initializeGateProducer() {
			AppendableProtocolProducer par = new ParallelProtocolProducer();
			for (int i = 0; i < vector.length; i++) {
				ProtocolProducer mult = numericProvider.getMultCircuit(scale, vector[i],
						output[from + i]);
				par.append(mult);
			}
			return par;
		}

		
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

}
