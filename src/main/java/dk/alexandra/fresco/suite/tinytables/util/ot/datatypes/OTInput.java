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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class represents the input to an oblivious transfer protocol from the
 * sender.</p>
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
public class OTInput {

	private boolean x0, x1;

	public OTInput(boolean x0, boolean x1) {
		this.x0 = x0;
		this.x1 = x1;
	}

	public boolean getX0() {
		return this.x0;
	}

	public boolean getX1() {
		return this.x1;
	}

	/**
	 * Create a list of <code>OTInput</code>'s from lists of <i>x<sub>0</sub></i>'s and
	 * <i>x<sub>1</sub></i>'s.
	 * 
	 * @param x0s
	 * @param x1s
	 * @return
	 */
	public static List<OTInput> fromLists(List<Boolean> x0s, List<Boolean> x1s) {
		if (x0s.size() != x1s.size()) {
			throw new InvalidParameterException("Must have same number of x0s and x1s");
		}
		List<OTInput> out = new ArrayList<OTInput>();
		for (int i = 0; i < x0s.size(); i++) {
			out.add(new OTInput(x0s.get(i), x1s.get(i)));
		}
		return out;
	}
	
	/**
	 * Given a list of OTInput's, this methods return a list of all
	 * <i>x<sub>i</sub></i> from the inputs where <i>i = 0,1</i> is the specified
	 * index.
	 * 
	 * @param inputs
	 * @param index
	 * @return
	 */
	public static List<Boolean> getAll(List<OTInput> inputs, int index) {
		if (index < 0 || index > 1) {
			throw new InvalidParameterException("Index must be either 0 or 1");
		}
		List<Boolean> out = new ArrayList<Boolean>();
		for (OTInput input : inputs) {
			if (index == 0) {
				out.add(input.getX0());
			} else {
				out.add(input.getX1());
			}
		}
		return out;
	}
}
