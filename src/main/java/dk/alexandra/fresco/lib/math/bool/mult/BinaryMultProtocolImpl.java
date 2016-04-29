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
package dk.alexandra.fresco.lib.math.bool.mult;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.bool.add.AdderProtocolFactory;

/**
 * This class implements a Binary Multiplication Circuit by doing the school method.
 * This means that we connect O(n^2) 1-Bit-FullAdders in order to get the result. 
 * As one would imagine, this is not the most efficient method, but it works as a basic case. 
 * @author Kasper Damgaard
 *
 */
public class BinaryMultProtocolImpl implements BinaryMultProtocol{	

	private SBool[] lefts, rights, outs;
	private SBool[][] andMatrix;
	private SBool[] intermediateResults;
	private SBool[] carries;
	private BasicLogicFactory basicProvider;
	private AdderProtocolFactory adderProvider;
	private int round;
	private int stopRound;
	private ProtocolProducer curGP;
	
	public BinaryMultProtocolImpl(SBool[] lefts, SBool[] rights, SBool[] outs,
			BasicLogicFactory basicProvider, AdderProtocolFactory adderProvider) {
		if(lefts.length+rights.length != outs.length){
			throw new MPCException("input arrays must be same length, and output array must be twice that of the inputs.");
		}
		this.lefts = lefts;
		this.rights = rights;
		this.outs = outs;
		this.basicProvider = basicProvider;
		this.adderProvider = adderProvider;
		this.round = 0;
		this.stopRound = rights.length;		
		this.curGP = null;
		
		//For the rest of the file: j equals row or round
		//i is index in that row 
		
		this.carries = new SBool[lefts.length];
		for(int i = 0; i < carries.length; i++){
			carries[i] = basicProvider.getSBool();
		}
		
		intermediateResults = new SBool[lefts.length];
		andMatrix = new SBool[lefts.length][rights.length-1];
		
		for(int i = 0; i < lefts.length; i++){	
			intermediateResults[i] = basicProvider.getSBool();
			for(int j = 0; j < rights.length-1; j++){				
				andMatrix[i][j] = basicProvider.getSBool();
			}
		}
	}

	/**
	 * Round 1: Create a matrix that is the AND of every possible input combination. 
	 * Round 2-(stopRound-1): Create layers of adders that takes the last layers result
	 * 	as well as the corresponding andMatrix layer and adds it. 
	 * Round stopRound-1: Do the final layer. Same as the other rounds, except the last carry is outputted.
	 */
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if(round == 0){
			if(curGP == null){				
				curGP = new ParallelProtocolProducer();
				for(int i = 0; i < lefts.length; i++){
					for(int j = 0; j < rights.length; j++){
						if(j == rights.length-1){ //corresponding to having least significant bit steady. 
							if(i == lefts.length-1){
								((ParallelProtocolProducer)curGP).append(basicProvider.getAndProtocol(lefts[i], rights[j], outs[outs.length-1]));
							}else{
								((ParallelProtocolProducer)curGP).append(basicProvider.getAndProtocol(lefts[i], rights[j], intermediateResults[intermediateResults.length-2-i]));
							}
						}
						else{
							((ParallelProtocolProducer)curGP).append(basicProvider.getAndProtocol(lefts[i], rights[j], andMatrix[lefts.length-1-i][rights.length-2-j]));
						}
					}
				}
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
				Protocol firstHA = adderProvider.getOneBitHalfAdderProtocol(andMatrix[0][round-1], intermediateResults[0], outs[outs.length-1-round], carries[0]);
				Protocol[] FAs = new Protocol[lefts.length-1];
				for(int i = 1; i < lefts.length; i++){
					if(round == 1 && i == lefts.length-1){
						//special case where we need a half adder, not a full adder since we do not have a carry from first layer. 
						FAs[i-1] = adderProvider.getOneBitHalfAdderProtocol(andMatrix[i][round-1], carries[i-1], intermediateResults[i-1], intermediateResults[i]);
					}
					else if(i == lefts.length-1){
						FAs[i-1] = adderProvider.getOneBitFullAdderProtocol(andMatrix[i][round-1], intermediateResults[i], carries[i-1], intermediateResults[i-1], intermediateResults[i]);
					}					
					else{						
						FAs[i-1] = adderProvider.getOneBitFullAdderProtocol(andMatrix[i][round-1], intermediateResults[i], carries[i-1], intermediateResults[i-1], carries[i]);
					}
				}
				SequentialProtocolProducer tmp = new SequentialProtocolProducer(FAs); 
				curGP = new SequentialProtocolProducer(firstHA, tmp);				
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
				Protocol firstHA = adderProvider.getOneBitHalfAdderProtocol(andMatrix[0][round-1], intermediateResults[0], outs[outs.length-1-round], carries[0]);
				Protocol[] FAs = new Protocol[lefts.length-1];
				for(int i = 1; i < lefts.length; i++){
					if(i == lefts.length-1){
						FAs[i-1] = adderProvider.getOneBitFullAdderProtocol(andMatrix[i][round-1], intermediateResults[i], carries[i-1], outs[1], outs[0]);
					}
					else{
						FAs[i-1] = adderProvider.getOneBitFullAdderProtocol(andMatrix[i][round-1], intermediateResults[i], carries[i-1], outs[outs.length-1-round-i], carries[i]);
					}
				}
				SequentialProtocolProducer tmp = new SequentialProtocolProducer(FAs); 
				curGP = new SequentialProtocolProducer(firstHA, tmp);					
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
