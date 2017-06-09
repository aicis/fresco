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
package dk.alexandra.fresco.suite.spdz.utils;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.AbstractProtocolBuilder;
import java.math.BigInteger;

/**
 * A protocol builder to handle spdz specific protocols and gates
 * 
 * @author psn
 *
 */
public class SpdzProtocolBuilder extends AbstractProtocolBuilder {

	private final SpdzFactory factory;
	
	public SpdzProtocolBuilder(SpdzFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public void addProtocolProducer(ProtocolProducer pp) {
		append(pp);
	}
	
	public SInt input(BigInteger o, int inputterId) {
		SInt s = factory.getSInt();
		NativeProtocol gp = factory.getCloseProtocol(o, s, inputterId);
		append(gp);
		return s;
	}
	
	public SInt input(int inputterId) {
		return input(null, inputterId);
	}
	
	public OInt outputAll(SInt s) {
		OInt o = factory.getOInt();
		NativeProtocol gp = factory.getOpenProtocol(s, o);
		append(gp);
		return o;
	}
	
	public OInt output(SInt s, int outputterId) {
		OInt o = factory.getOInt();
		NativeProtocol gp = factory.getOpenProtocol(outputterId, s, o);
		append(gp);
		return o;
	}
}
