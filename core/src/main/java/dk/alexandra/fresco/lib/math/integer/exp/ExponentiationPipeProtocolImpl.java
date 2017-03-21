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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocolFactory;

public class ExponentiationPipeProtocolImpl implements ExponentiationPipeProtocol {

	private final SInt R;
	private final int exp_size;
	private final SInt[] outputs;
	private final InversionProtocolFactory invFactory;
	private final BasicNumericFactory factory;
	private final CopyProtocolFactory<SInt> copyFactory;
	private ProtocolProducer pp;
	private int state = 0;
	private boolean running = false;
	
	public ExponentiationPipeProtocolImpl(SInt R, SInt[] outputs, 
			InversionProtocolFactory invFactory, BasicNumericFactory factory, CopyProtocolFactory<SInt> copyFactory){
		this.R = R;
		this.exp_size = outputs.length;
		this.outputs = outputs;
		this.invFactory = invFactory;
		this.factory = factory;
		this.copyFactory = copyFactory;
	}	

	@Override
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if(state == 0){
			Protocol invC = invFactory.getInversionProtocol(R, outputs[0]);
			Protocol copyR = copyFactory.getCopyProtocol(R, outputs[1]);
			Protocol mult = factory.getMultProtocol(R, R, outputs[2]);			
			pp = new ParallelProtocolProducer(invC, copyR, mult);
			state = 2;
		}else if(!running){
			//Should initially multiply R with R^2 => R^3 
			Protocol mult = factory.getMultProtocol(R, outputs[state++], outputs[state]);
			pp = new ParallelProtocolProducer(mult);
		}
		if (pp.hasNextProtocols()){
			pos = pp.getNextProtocols(nativeProtocols, pos);
			running = true;
		}
		else if (!pp.hasNextProtocols()){
			pp = null;
			running = false;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return state < exp_size-1;
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
