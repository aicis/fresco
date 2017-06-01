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
package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;

public class ExponentiationProtocolImpl extends AbstractSimpleProtocol implements ExponentiationProtocol {

	private SInt input;
	private OInt openInput;
	private final SInt exponent;
	private final int maxExponentBitLength;
	private final SInt output;
	
	private final BasicNumericFactory basicNumericFactory;
	private final IntegerToBitsFactory integerToBitsFactory;

	public ExponentiationProtocolImpl(SInt input, SInt exponent, int maxExponentBitLength, SInt output,
			BasicNumericFactory basicNumericFactory, IntegerToBitsFactory integerToBitsFactory) {
		
		this.input = input;
		this.exponent = exponent;
		this.maxExponentBitLength = maxExponentBitLength;
		this.output = output;
		
		this.basicNumericFactory = basicNumericFactory;
		this.integerToBitsFactory = integerToBitsFactory;
		
	}

	public ExponentiationProtocolImpl(OInt input, SInt exponent, int maxExponentBitLength, SInt output,
			BasicNumericFactory basicNumericFactory, IntegerToBitsFactory integerToBitsFactory) {
		
		this.openInput = input;
		this.exponent = exponent;
		this.maxExponentBitLength = maxExponentBitLength;
		this.output = output;
		
		this.basicNumericFactory = basicNumericFactory;
		this.integerToBitsFactory = integerToBitsFactory;
		
	}
	
	@Override
	protected ProtocolProducer initializeProtocolProducer() {
		
		NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);
		
		SInt[] bits = builder.getSIntArray(maxExponentBitLength);
		builder.addProtocolProducer(integerToBitsFactory.getIntegerToBitsCircuit(exponent, maxExponentBitLength, bits));
		
		SInt result = builder.getSInt(1);
		
		if (input != null) {
			SInt e = builder.getSInt();
			builder.copy(e, input);
			
			for (int i = 0; i < maxExponentBitLength; i++) {
				/*
				 * result += bits[i] * (result * r - r) + r
				 * 
				 *  aka.
				 * 
				 *            result       if bits[i] = 0
				 * result = {
				 *            result * e   if bits[i] = 1
				 */
				result = builder.add(builder.mult(bits[i], builder.sub(builder.mult(result, e), result)), result);
				e = builder.mult(e, e);
			}
			
		} else if (openInput != null) {
			OInt e = basicNumericFactory.getOInt(openInput.getValue());
			
			for (int i = 0; i < maxExponentBitLength; i++) {
				/*
				 * result += bits[i] * (result * r - r) + r
				 * 
				 *  aka.
				 * 
				 *            result       if bits[i] = 0
				 * result = {
				 *            result * e   if bits[i] = 1
				 */
				result = builder.add(builder.mult(bits[i], builder.sub(builder.mult(e, result), result)), result);
				e = basicNumericFactory.getOInt(e.getValue().multiply(e.getValue()));
			}
		} else {
			throw new IllegalArgumentException("Either input or openInput must not be null");
		}

		builder.copy(output, result);
		return builder.getProtocol();
	}
}
