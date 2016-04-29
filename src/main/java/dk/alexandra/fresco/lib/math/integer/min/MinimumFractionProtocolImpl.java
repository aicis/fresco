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
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractProtocol;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.AppendableProtocolProducer;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;

public class MinimumFractionProtocolImpl implements MinimumFractionProtocol {

	private SInt[] ns, ds, cs;
	private SInt nm, dm;
	private ProtocolProducer currGP;
	private LPFactory lpProvider;
	private BasicNumericFactory numericProvider;
	private final int k;
	private boolean done = false;
	
	public MinimumFractionProtocolImpl(SInt[] ns, SInt[] ds, SInt nm, SInt dm, SInt[] cs,
			BasicNumericFactory numericProvider, LPFactory lpProvider) {
		if (ns.length == ds.length && ns.length == cs.length) {
			this.k = ns.length;
			this.ns = ns;
			this.ds = ds;
			this.nm = nm;
			this.dm = dm;
			this.cs = cs;
			this.numericProvider = numericProvider;
			this.lpProvider = lpProvider;
		} else {
			throw new MPCException("Sizes of input arrays does not match");
		}
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if(currGP == null) {
			if(this.k == 1) {
				throw new MPCException("k should never be 1.");
			} else if (this.k == 2) {
				ProtocolProducer comparison = minFraction(ns[0], ds[0], ns[1], ds[1], cs[0], nm, dm);
				SInt one = numericProvider.getSInt(1);
				SubtractProtocol subtract = numericProvider.getSubtractProtocol(one, this.cs[0], this.cs[1]);
				currGP = new SequentialProtocolProducer(comparison, subtract);
			} else if (this.k == 3) {
				SInt c1_prime = numericProvider.getSInt();
				SInt nm1 = numericProvider.getSInt();
				SInt dm1 = numericProvider.getSInt();
				ProtocolProducer min1 = minFraction(ns[0], ds[0], ns[1], ds[1], c1_prime, nm1, dm1);
				
				SInt c2_prime = numericProvider.getSInt();
				ProtocolProducer min2 = minFraction(nm1, dm1, ns[2], ds[2], c2_prime, nm, dm);
				
				MultProtocol mult1 = numericProvider.getMultProtocol(c1_prime, c2_prime, this.cs[0]);
				SubtractProtocol sub1 = numericProvider.getSubtractProtocol(c2_prime, this.cs[0], this.cs[1]);
				SInt one = numericProvider.getSInt(1);
				SInt tmp = numericProvider.getSInt();
				SubtractProtocol sub2 = numericProvider.getSubtractProtocol(one, this.cs[0], tmp);
				
				SubtractProtocol sub3 = numericProvider.getSubtractProtocol(tmp, this.cs[1], this.cs[2]);
				
				SequentialProtocolProducer seqGP = new SequentialProtocolProducer(min1, min2, mult1);
				ParallelProtocolProducer parGP = new ParallelProtocolProducer(sub1, sub2);
				currGP = new SequentialProtocolProducer(seqGP, parGP, sub3);				
			} else {
				currGP = new RecursionPart();				
			}
		}
		if(currGP.hasNextProtocols()){
			pos = currGP.getNextProtocols(gates, pos);
		}
		else if(!currGP.hasNextProtocols()){
			currGP = null;
			done = true;
		}
		return pos;
	}
	
	
	private class RecursionPart extends AbstractRoundBasedProtocol {

		private int round = 0;
		private SInt nm1;
		private SInt dm1;
		private SInt nm2;
		private SInt dm2;
		private SInt[] cs1_prime;
		private SInt[] cs2_prime;
		private SInt one;
		
		@Override
		public ProtocolProducer nextProtocolProducer() {
			ProtocolProducer gp = null;
			if (round == 0) {
				int k1 = k/2;
				int k2 = k - k1;
				SInt[] N1 = new SInt[k1];
				SInt[] D1 = new SInt[k1];
				SInt[] N2 = new SInt[k2];
				SInt[] D2 = new SInt[k2];
				System.arraycopy(ns, 0, N1, 0, N1.length);
				System.arraycopy(ds, 0, D1, 0, D1.length);
				System.arraycopy(ns, N1.length, N2, 0, N2.length);
				System.arraycopy(ds, D1.length, D2, 0, D2.length);
				
				cs1_prime = new SInt[k1];
				cs2_prime = new SInt[k2];
				nm1 = numericProvider.getSInt();
				dm1 = numericProvider.getSInt();
				nm2 = numericProvider.getSInt();
				dm2 = numericProvider.getSInt();
				System.arraycopy(cs, 0, cs1_prime, 0, k1);
				System.arraycopy(cs, k1, cs2_prime, 0, k2);
				MinimumFractionProtocol min1 = lpProvider.getMinimumFractionCircuit(N1, D1, nm1, dm1, cs1_prime);
				MinimumFractionProtocol min2 = lpProvider.getMinimumFractionCircuit(N2, D2, nm2, dm2, cs2_prime);
				one = numericProvider.getSInt();
				ProtocolProducer load1 = numericProvider.getSInt(1, one);
				gp = new ParallelProtocolProducer(min1, min2, load1);
				round++;
			} else if (round == 1){
				SInt c = numericProvider.getSInt();
				ProtocolProducer min = minFraction(nm1, dm1, nm2, dm2, c, nm, dm);	
				SInt notC = numericProvider.getSInt();
				SubtractProtocol subtract = numericProvider.getSubtractProtocol(one, c, notC);
				VectorScale scale1 = new VectorScale(c, cs1_prime, cs, 0);
				VectorScale scale2 = new VectorScale(notC, cs2_prime, cs, k/2);
				ProtocolProducer multGates = new ParallelProtocolProducer(scale1, scale2);				
				gp = new SequentialProtocolProducer(min, subtract, multGates);
				round++;
			} else {
				cs1_prime = null;
				cs2_prime = null;
				nm1 = null;
				nm2 = null;
				dm1 = null;
				dm2 = null;
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
		protected ProtocolProducer initializeProtocolProducer() {
			AppendableProtocolProducer par = new ParallelProtocolProducer();
			for (int i = 0; i < vector.length; i++) {
				ProtocolProducer mult = numericProvider.getMultProtocol(scale, vector[i],
						output[from + i]);
				par.append(mult);
			}
			return par;
		}	
	}
	
	/**
	 * Computes the minimum of two fractions
	 * @param n0 input - the numerator of fraction 0
	 * @param d0 input - the denominator of fraction 0
	 * @param n1 input - the numerator of fraction 1
	 * @param d1 input - the denominator of fraction 1
	 * @param c output - a bit describing the minimum fraction 
	 * @param nm output - the numerator of the minimum fraction
	 * @param dm output - the denominator of the minimum fraction
	 * @return
	 */
	private ProtocolProducer minFraction(SInt n0, SInt d0, SInt n1, SInt d1, 
			SInt c, SInt nm, SInt dm) {
		SInt prod1 = numericProvider.getSInt();
		SInt prod2 = numericProvider.getSInt();
		MultProtocol mult1 = numericProvider.getMultProtocol(n0, d1, prod1);
		MultProtocol mult2 = numericProvider.getMultProtocol(n1, d0, prod2);
		ProtocolProducer multiplications = new ParallelProtocolProducer(mult1, mult2);
		ComparisonProtocol comp = lpProvider.getComparisonCircuit(prod1, prod2, c, true);
		ConditionalSelectProtocol cond1 = lpProvider.getConditionalSelectProtocol(c, n0, n1, nm);
		ConditionalSelectProtocol cond2 = lpProvider.getConditionalSelectProtocol(c, d0, d1, dm);
		ProtocolProducer conditionalSelects = new ParallelProtocolProducer(cond1, cond2);
		return new SequentialProtocolProducer(multiplications, comp, conditionalSelects);		
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
