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
package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class OneBitFullAdderProtocolImpl implements OneBitFullAdderProtocol{

	private SBool a, b, c, outS, outCarry;
	private SBool xor1Out, and1Out, and2Out;
	private BasicLogicFactory provider;
	private ProtocolProducer curGP;
	private int round;
	
	public OneBitFullAdderProtocolImpl(SBool a, SBool b, SBool c, 
			SBool outS, SBool outCarry, BasicLogicFactory provider){
		this.a = a;
		this.b = b;
		this.c = c;
		this.outS = outS;
		this.outCarry = outCarry;
		this.provider = provider;
		this.round = 0;
	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if(round == 0){
			if(curGP == null){
				xor1Out = provider.getSBool();
				and1Out = provider.getSBool();
				ProtocolProducer xor1 = provider.getXorProtocol(a, b, xor1Out);
				ProtocolProducer and1 = provider.getAndProtocol(a, b, and1Out);
				curGP = new ParallelProtocolProducer(xor1, and1);				
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}
		else if(round == 1){
			if(curGP == null){
				and2Out = provider.getSBool();
				ProtocolProducer and2 = provider.getAndProtocol(xor1Out, c, and2Out);
				ProtocolProducer xor2 = provider.getXorProtocol(xor1Out, c, outS);			
				curGP = new ParallelProtocolProducer(and2, xor2);
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}
		else if(round == 2){
			if(curGP == null){
				ProtocolProducer xor3 = provider.getXorProtocol(and2Out, and1Out, outCarry);
				curGP = new SequentialProtocolProducer(xor3);
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return round < 3;
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
