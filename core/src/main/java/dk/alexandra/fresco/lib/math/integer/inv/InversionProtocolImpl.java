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
package dk.alexandra.fresco.lib.math.integer.inv;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementProtocol;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class InversionProtocolImpl implements InversionProtocol {
	
	private final SInt x, result;
	private final BasicNumericFactory factory;
	private final LocalInversionFactory invFactory;
	private final RandomFieldElementFactory randFactory;
	private ProtocolProducer pp;
	boolean done;
	
	public InversionProtocolImpl(SInt x, SInt result, BasicNumericFactory factory, 
			LocalInversionFactory invFactory, RandomFieldElementFactory randFactory) {
		this.x = x;
		this.result = result;
		this.factory = factory;
		this.invFactory = invFactory;
		this.randFactory = randFactory;
		this.pp = null;
		this.done = false;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos){
		if (pp == null){
			OInt inverse = factory.getOInt();
			SInt sProduct = factory.getSInt();
			OInt oProduct = factory.getOInt();
			SInt random = factory.getSInt();
			RandomFieldElementProtocol randomProt = randFactory.getRandomFieldElement(random);
			MultProtocol blinding = factory.getMultProtocol(x, random, sProduct);
			OpenIntProtocol open = factory.getOpenProtocol(sProduct, oProduct);
			LocalInversionProtocol invert = invFactory.getLocalInversionProtocol(oProduct, inverse);
			MultProtocol unblinding = factory.getMultProtocol(inverse, random, result);
			
			pp = new SequentialProtocolProducer(randomProt, blinding, open, invert, unblinding);
		}
		if (pp.hasNextProtocols()){
			pos = pp.getNextProtocols(nativeProtocols, pos);
		}
		else if (!pp.hasNextProtocols()){
			pp = null;
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

}
