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
package dk.alexandra.fresco.demo.inputsum;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;

/**
 * Demo application. Takes a number of inputs and converts them to secret shared
 * inputs by having party 1 input them all.
 * 
 * @author Kasper Damgaard
 *
 */
public class InputApplication implements Application {

	private static final long serialVersionUID = -6059451155463638044L;
	
	private int[] inputs;
	private int length;
	private SInt[] ssInputs;

	public InputApplication(int[] inputs) {
		this.inputs = inputs;
		this.length = inputs.length;
	}

	public InputApplication(int length) {
		this.length = length;
	}

	@Override
	public ProtocolProducer prepareApplication(ProtocolFactory factory) {
		BasicNumericFactory fac = (BasicNumericFactory) factory;
		this.ssInputs = new SInt[this.length];
		
		NumericIOBuilder ioBuilder = new NumericIOBuilder(fac);
		ioBuilder.beginParScope();
		for(int i = 0; i < this.length; i++) {
			//create wires
			this.ssInputs[i] = fac.getSInt();
			if(this.inputs != null) {				
				this.ssInputs[i] = ioBuilder.input(this.inputs[i], 1);
			} else {
				this.ssInputs[i] = ioBuilder.input(1);
			}
		}
		ioBuilder.endCurScope();

		return ioBuilder.getProtocol();
	}

	public SInt[] getSecretSharedInput() {
		return this.ssInputs;
	}
}