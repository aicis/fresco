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
package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.AddProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractCircuit;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class ConditionalSelectCircuitImpl implements ConditionalSelectCircuit{
	
	private final SInt a, b, selector, result;
	private final BasicNumericFactory provider;
	private ProtocolProducer gp;
	boolean done;
	
	public ConditionalSelectCircuitImpl(SInt selector, SInt a, SInt b, SInt result, BasicNumericFactory provider) {
		this.a = a;
		this.b = b;
		this.selector = selector;
		this.result = result;
		this.provider = provider;
		this.gp = null;
		this.done = false;
	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos){
		if (gp == null){
			SInt subResult = provider.getSInt();
			SInt multResult = provider.getSInt();
			SubtractCircuit subCircuit = provider.getSubtractCircuit(a, b, subResult);
			MultProtocol multCircuit = provider.getMultCircuit(selector, subResult, multResult);
			AddProtocol addCircuit = provider.getAddProtocol(multResult, b, result);
			
			this.gp = new SequentialProtocolProducer(subCircuit, multCircuit, addCircuit);
		}
		if (gp.hasNextProtocols()){
			pos = gp.getNextProtocols(gates, pos);
		}
		else if (!gp.hasNextProtocols()){
			gp = null;
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
