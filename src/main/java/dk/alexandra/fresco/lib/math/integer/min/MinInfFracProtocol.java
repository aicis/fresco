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
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;

/**
 * Implements a version of the <code>MinimumFractionProtocol</code> that allows
 * to indicate that certain fractions should be regarded as having infinite
 * value. I.e., to indicate that those fractions should never be chosen as the
 * minimum. We do this by taking for each fraction a infinity indicator bit
 * which can than be used to adjust any comparison result for the indicated
 * fractions. This solves a problem in the Simplex solver where we need to find
 * the minimum fraction larger than 0, in a list of fractions.
 * 
 * This improves on a previous solution that simply tried to set all fractions
 * smaller than or equal to 0 to a very large value (essentially assuming this
 * value would be a good approximation of infinity). Such a solution however,
 * turns out to be prone to overflow problems, and picking the very larger
 * value, is also non-trivial.
 * 
 *
 */
public class MinInfFracProtocol extends AbstractRoundBasedProtocol implements MinimumFractionProtocol {

	private SInt[] cs;
	private ComparisonProtocolFactory cFac;
	private BasicNumericFactory nFac;
	private int layer = 0;
	private Frac[] fs;
	private Frac fm;
	private SInt one;
	private State state;
	private SInt[] tmpCs;

	private enum State {
		FIND_MIN, UPDATE_CS
	};

	/**
	 * Constructs a protocol finding the minimum of a list of fractions. For
	 * each fraction a 0/1 value should be given to indicate whether or not that
	 * fraction should be disregarded when finding the minimum (similar to
	 * setting that fraction to a value of infinity).
	 * 
	 * @param ns
	 *            input - a list of numerators
	 * @param ds
	 *            input - a list of denominators
	 * @param infs
	 *            input - a list of infinity indicators (should be a 0/1 value,
	 *            1 indicating infinity)
	 * @param nm
	 *            output - the numerator of the minimum fraction
	 * @param dm
	 *            output - the denominator of the minimum fraction
	 * @param infm
	 *            output - the infinity indicator of the minimum fraction (i.e.,
	 *            should only be one if all fractions are set to infinity)
	 * @param cs
	 *            output - an index into the input arrays indicating the
	 *            position of the minimum fraction. I.e., <code>c[i] == 1</code>
	 *            if the i'th fraction was the minimum and
	 *            <code>c[i] == 0</code> otherwise.
	 * @param nFac
	 *            a basic numeric factory to be used.
	 * @param cFac
	 *            a comparison factory to be used.
	 */
	public MinInfFracProtocol(SInt[] ns, SInt[] ds, SInt[] infs, SInt nm, SInt dm, SInt infm, SInt[] cs,
			BasicNumericFactory nFac, ComparisonProtocolFactory cFac) {
		if (ns.length == ds.length && ns.length == cs.length) {
			this.fs = new Frac[ns.length];
			for (int i = 0; i < ns.length; i++) {
				fs[i] = new Frac(ns[i], ds[i], infs[i]);
			}
			this.fm = new Frac(nm, dm, infm);
			this.cs = cs;
			this.nFac = nFac;
			this.cFac = cFac;
			this.state = State.FIND_MIN;
		} else {
			throw new MPCException("Sizes of input arrays does not match");
		}
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol#nextProtocolProducer()
	 */
	@Override
	public ProtocolProducer nextProtocolProducer() {
		NumericProtocolBuilder npb = new NumericProtocolBuilder(nFac);
		if (state == State.FIND_MIN) {
			if (layer == 0) {
				one = npb.known(1);
				if (fs.length == 1) { // The trivial case
					npb.copy(fm.n, fs[0].n);
					npb.copy(fm.d, fs[0].d);
					npb.copy(fm.inf, fs[0].inf);
					npb.copy(cs[0], one);
					return npb.getProtocol();
				}
			} else if (fs.length == 1) {
				return null;
			}
			Frac[] tmpFs = new Frac[fs.length / 2 + (fs.length % 2)];
			tmpFs[0] = new Frac(fm.n, fm.d, fm.inf);
			for (int i = 1; i < tmpFs.length; i++) {
				tmpFs[i] = new Frac(npb.getSInt(), npb.getSInt(), npb.getSInt());
			}
			tmpCs = npb.getSIntArray(fs.length / 2);
			npb.beginParScope();
			for (int i = 0; i < tmpCs.length; i++) {
				npb.addProtocolProducer(minFraction(fs[i * 2], fs[i * 2 + 1], tmpCs[i], tmpFs[i]));
			}
			npb.endCurScope();
			if (fs.length % 2 == 1) {
				tmpFs[tmpFs.length - 1] = fs[fs.length - 1];
			}
			fs = tmpFs;
			state = State.UPDATE_CS;
		} else if (state == State.UPDATE_CS) {
			int offset = 1 << (layer + 1);
			if (layer == 0) {
				npb.beginParScope();
				for (int i = 0; i < tmpCs.length; i++) {
					npb.beginSeqScope();
					SInt c = tmpCs[i];
					SInt notC = npb.sub(one, c);
					npb.copy(cs[i * 2], c);					
					npb.copy(cs[i*2+1], notC);
					npb.endCurScope();
				}
				if (cs.length % 2 == 1) {
					npb.copy(cs[cs.length - 1], one);
				}
				npb.endCurScope();
			} else {
				npb.beginParScope(); // CHANGE TO PAR!
				for (int i = 0; i < tmpCs.length; i++) {
					SInt c = tmpCs[i];
					for (int j = i * offset; j < i * offset + offset / 2; j++) {
						npb.beginSeqScope();
						SInt tmp = npb.mult(c, cs[j]);
						npb.copy(cs[j], tmp);
						npb.endCurScope();
					}
					npb.beginSeqScope();
					SInt notC = npb.sub(one, c);
					npb.beginParScope();
					int limit = (i + 1) * offset > cs.length ? cs.length : (i + 1) * offset;
					for (int j = i * offset + offset / 2; j < limit; j++) {
						npb.beginSeqScope();
						SInt tmp = npb.mult(notC, cs[j]);
						npb.copy(cs[j], tmp);
						npb.endCurScope();
					}
					npb.endCurScope();
					npb.endCurScope();
				}
				npb.endCurScope();
			}
			layer++;
			state = State.FIND_MIN;
		}
		return npb.getProtocol();
	}

	private ProtocolProducer minFraction(Frac f0, Frac f1, SInt c, Frac r) {
		NumericProtocolBuilder npb = new NumericProtocolBuilder(nFac);
		npb.beginParScope();
		SInt p1 = npb.mult(f0.n, f1.d);
		SInt p2 = npb.mult(f1.n, f0.d);
		npb.endCurScope();
		npb.beginSeqScope();
		SInt tmpC = npb.getSInt();
		ComparisonProtocol comp = cFac.getGreaterThanProtocol(p1, p2, tmpC, true);
		npb.addProtocolProducer(comp);
		SInt notInf0 = npb.sub(one, f0.inf);
		tmpC = npb.mult(notInf0, tmpC);
		tmpC = npb.conditionalSelect(f1.inf, f1.inf, tmpC);
		npb.copy(c, tmpC);
		npb.beginParScope();
		SInt rn = npb.conditionalSelect(c, f0.n, f1.n);
		SInt rd = npb.conditionalSelect(c, f0.d, f1.d);
		SInt rinf = npb.mult(f0.inf, f1.inf);
		npb.endCurScope();
		npb.beginParScope();
		npb.copy(r.n, rn);
		npb.copy(r.d, rd);
		npb.copy(r.inf, rinf);
		npb.endCurScope();
		npb.endCurScope();
		return npb.getProtocol();
	}

	/**
	 * Helper class to represent a fraction consisting of a numerator denominator 
	 * and an infinity indicator.
	 */
	private class Frac {
		public SInt n, d, inf;

		public Frac(SInt n, SInt d, SInt inf) {
			super();
			this.n = n;
			this.d = d;
			this.inf = inf;
		}
	}
}
