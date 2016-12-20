/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.tinytables.util.ot.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class represents the input to an oblivious transfer protocol from the
 * reveicer.</p>
 * 
 * <p>In an oblivious transfer, the sender provides two boolean values,
 * <i>x<sub>0</sub></i> and <i>x<sub>1</sub></i>, and the receiver provides one
 * boolean value <i>&sigma; &isin; {0,1}</i>. After the protocol has finished,
 * the receiver knows <i>x<sub>&sigma;</sub></i>, but not the other input given
 * by the sender, and the sender does not know <i>&sigma;</i>.</p>
 * 
 * @author jonas
 *
 */
public class OTSigma {

	private boolean sigma;

	public OTSigma(boolean sigma) {
		this.sigma = sigma;
	}
	
	public boolean getSigma() {
		return this.sigma;
	}
	
	/**
	 * Create a list of <code>OTSigma</code>'s from a list of <i>&sigma;</i>'s.
	 * 
	 * @param sigmas
	 * @return
	 */
	public static List<OTSigma> fromList(List<Boolean> sigmas) {
		List<OTSigma> out = new ArrayList<OTSigma>();
		for (boolean s : sigmas) {
			out.add(new OTSigma(s));
		}
		return out;
	}
	
	/**
	 * Given a list of OTSigmas's, this methods return a list of all
	 * <i>&sigma</i>'s.
	 * 
	 * @param inputs
	 * @return
	 */
	public static List<Boolean> getAll(List<OTSigma> sigmas) {
		List<Boolean> out = new ArrayList<Boolean>();
		for (OTSigma sigma : sigmas) {
			out.add(sigma.getSigma());
		}
		return out;
	}
}
