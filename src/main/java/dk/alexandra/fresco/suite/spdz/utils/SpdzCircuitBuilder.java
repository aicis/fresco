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
package dk.alexandra.fresco.suite.spdz.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.AbstractProtocolBuilder;

/**
 * A circuit builder to handle spdz specific circuits and gates
 * 
 * @author psn
 *
 */
public class SpdzCircuitBuilder extends AbstractProtocolBuilder {

	private final SpdzFactory provider;
	
	public SpdzCircuitBuilder(SpdzFactory provider) {
		this.provider = provider;
	}
	
	@Override
	public void addProtocolProducer(ProtocolProducer gp) {
		append(gp);
	}
	
	public SInt input(BigInteger o, int inputterId) {
		SInt s = provider.getSInt();
		ProtocolProducer gp = provider.getCloseProtocol(o, s, inputterId);
		append(gp);
		return s;
	}
	
	public SInt input(int inputterId) {
		return input(null, inputterId);
	}
	
	public OInt outputAll(SInt s) {
		OInt o = provider.getOInt();
		ProtocolProducer gp = provider.getOpenProtocol(s, o);
		append(gp);
		return o;
	}
	
	public OInt output(SInt s, int outputterId) {
		OInt o = provider.getOInt();
		ProtocolProducer gp = provider.getOpenProtocol(outputterId, s, o);
		append(gp);
		return o;
	}
}
