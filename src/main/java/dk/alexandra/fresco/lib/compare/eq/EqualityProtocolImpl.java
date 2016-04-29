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
package dk.alexandra.fresco.lib.compare.eq;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/** Implements an equality protocol -- given inputs x, y set output to x==y
 * @author ttoft
 *
 */
public class EqualityProtocolImpl extends AbstractSimpleProtocol implements EqualityProtocol {

	// params
	private final int bitLength;
	private final int securityParameter;
	private final SInt x, y;
	private final SInt output;
	
	// Factories, etc
	private final BasicNumericFactory bnFactory;
	private final ZeroTestProtocolFactory ztFactory;	
	
	public EqualityProtocolImpl(int bitLength, int securityParameter, SInt x,
			SInt y, SInt output, BasicNumericFactory bnFactory,
			ZeroTestProtocolFactory ztFactory) {
		super();
		this.bitLength = bitLength;
		this.securityParameter = securityParameter;
		this.x = x;
		this.y = y;
		this.output = output;
		this.bnFactory = bnFactory;
		this.ztFactory = ztFactory;
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
	protected ProtocolProducer initializeProtocolProducer() {
		// z = x -y
		SInt diff = bnFactory.getSInt();
		ProtocolProducer diffPP = bnFactory.getSubtractProtocol(x, y, diff);

		// output = ZeroTest(z)
		ProtocolProducer zeroTestPP = ztFactory.getZeroProtocol(bitLength, securityParameter, diff, output);
		
		return new SequentialProtocolProducer(diffPP, zeroTestPP);
	}

	
}