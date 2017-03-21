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
package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class AltInnerProductProtocolImpl extends AbstractSimpleProtocol implements
		InnerProductProtocol {

	private final SInt[] aVector;
	private final SInt[] bVector;
	private SInt result;
	private BasicNumericFactory bnFactory;
	private CopyProtocolFactory<SInt> copyFactory;
	
	public AltInnerProductProtocolImpl(SInt[] aVector, SInt[] bVector, SInt result, 
			BasicNumericFactory bnFactory, CopyProtocolFactory<SInt> copyFactory){
		if(aVector.length != bVector.length){
			throw new MPCException("Lengths of input arrays do not match");
		}
		this.aVector = aVector;
		this.bVector = bVector;
		this.result = result;
		this.bnFactory = bnFactory;
		this.copyFactory = copyFactory;
	}
	
	@Override
	protected ProtocolProducer initializeProtocolProducer() {
		NumericProtocolBuilder ncb = new NumericProtocolBuilder(bnFactory);
		SInt[] directProduct = ncb.mult(aVector, bVector);
		SInt innerproduct = directProduct[0];
		for (int i = 1; i < directProduct.length; i++) {
			innerproduct = ncb.add(innerproduct, directProduct[i]);
		}
		ProtocolProducer copyResult = copyFactory.getCopyProtocol(innerproduct, this.result);
		return new SequentialProtocolProducer(ncb.getProtocol(), copyResult);
	}
}
