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
package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

/**
 * This circuit provides comparison stuff. It should really be merged into the
 * numeric circuit builder but at present this would require having an
 * LPProvider in the numeric circuit builder, which I thought would be a little
 * ugly.
 * 
 * @author psn
 *
 */
public class ComparisonProtocolBuilder extends AbstractProtocolBuilder {

	private final ComparisonProtocolFactory comProvider;
	private final BasicNumericFactory bnf;

	protected ComparisonProtocolFactory getComProvider() {
		return comProvider;
	}

	protected BasicNumericFactory getBnf() {
		return bnf;
	}

	public ComparisonProtocolBuilder(ComparisonProtocolFactory comProvider,
			BasicNumericFactory bnf) {
		this.comProvider = comProvider;
		this.bnf = bnf;
	}

	@Override
	public void addGateProducer(ProtocolProducer gp) {
		append(gp);
	}

	/**
	 * Compares if left < right
	 * 
	 * @param left
	 * @param right
	 * @return 1 if left<=right and 0 otherwise
	 */
	public SInt compare(SInt left, SInt right) {
		SInt result = bnf.getSInt();
		ProtocolProducer gp = comProvider.getGreaterThanProtocol(left, right,
				result, false);
		append(gp);
		return result;
	}

	/**
	 * Compares if left < right, but with twice the possible bit-length.
	 * Requires that the maximum bit length is set to something that can handle
	 * this scenario.
	 * 
	 * @param left
	 * @param right
	 * @return 1 if left<=right and 0 otherwise
	 */
	public SInt longCompare(SInt left, SInt right) {
		SInt result = bnf.getSInt();
		ProtocolProducer gp = comProvider.getGreaterThanProtocol(left, right,
				result, true);
		append(gp);
		return result;
	}

	/**
	 * Compares x == y
	 * @param x
	 * @param y
	 * @return 1 if true, 0 if false
	 */
	public SInt compareEqual(SInt x, SInt y) {
		SInt result = bnf.getSInt();
		ProtocolProducer gp = comProvider.getEqualityProtocol(
				bnf.getMaxBitLength(), x, y, result);
		append(gp);
		return result;
	}
}
