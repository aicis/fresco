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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * This class implements a Full Adder Circuit for Binary Circuits. 
 * It takes the naive approach of linking 1-Bit-Full Adders together to implement 
 * a generic length adder. 
 * @author Kasper Damgaard
 *
 */
public class FullAdderProtocolImpl implements FullAdderProtocol{

	private SBool[] lefts, rights, outs;
	private SBool inCarry, outCarry;
	private SBool tmpCarry;
	private OneBitFullAdderCircuitFactory FAProvider;
	private int round;
	private int stopRound;
	private ProtocolProducer curGP;
	
	public FullAdderProtocolImpl(SBool[] lefts, SBool[] rights, SBool inCarry, SBool[] outs,
			SBool outCarry, BasicLogicFactory basicProvider, OneBitFullAdderCircuitFactory FAProvider){
		if(lefts.length != rights.length || lefts.length != outs.length){
			throw new MPCException("input and output arrays for Full Adder must be of same length.");
		}
		this.lefts = lefts;
		this.rights = rights;
		this.inCarry = inCarry;
		this.outs = outs;
		this.outCarry = outCarry;
		this.FAProvider = FAProvider;
		this.round = 0;
		this.stopRound = lefts.length;
		this.curGP = null;
		
		tmpCarry = basicProvider.getSBool();
	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if(round == 0){
			if(curGP == null){				
				ProtocolProducer bit1 = FAProvider.getOneBitFullAdderProtocol(lefts[stopRound-1], rights[stopRound-1], inCarry, outs[stopRound-1], tmpCarry);
				curGP = new SequentialProtocolProducer(bit1);
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}
		else if(round > 0 && round < stopRound-1){
			if(curGP == null){								
				//TODO: Using tmpCarry both as in and out might not be good for all implementations of a 1Bit FA circuit?
				//But at least it works for OneBitFullAdderCircuitImpl.
				ProtocolProducer bitAdder = FAProvider.getOneBitFullAdderProtocol(lefts[stopRound-round-1], rights[stopRound-round-1], tmpCarry, outs[stopRound-round-1], tmpCarry);
				curGP = new SequentialProtocolProducer(bitAdder);
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}
		else if(round == stopRound-1){
			if(curGP == null){				
				ProtocolProducer bit8 = FAProvider.getOneBitFullAdderProtocol(lefts[0], rights[0], tmpCarry, outs[0], outCarry);
				curGP = new SequentialProtocolProducer(bit8);
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
		return round < stopRound;
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
