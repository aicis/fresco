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
package dk.alexandra.fresco.lib.compare.bool;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

/**
 * Represents a comparison circuit between two bitstrings. Concretely, the
 * circuit computes the 'greater than' relation of strings A and B, i.e., it
 * computes C := A > B.
 * 
 * This uses the method of GenericBinaryComparison2 but is implemented a lot
 * cleaner and fixes some bugs.
 * 
 * @author psn
 * 
 */
public class BinaryGreaterThanNextGatesImpl extends AbstractSimpleProtocol
		implements BinaryGreaterThanProtocol {

	private SBool[] inA;
	private SBool[] inB;
	private SBool outC;

	private SBool[] postfixResult;

	private AbstractBinaryFactory provider;

	private int length;
	
	private boolean done;
	private int round;
	private ProtocolProducer curGP;
	private SBool[] xor;

	/**
	 * Construct a circuit to compare strings A and B. The bitstrings A and B
	 * are assumed to be even length and to be ordered from most- to least
	 * significant bit.
	 * 
	 * @param inA
	 *            input string A
	 * @param inB
	 *            input string B
	 * @param outC
	 *            a bit to hold the output C := A > B.
	 * @param provider
	 *            a circuit provider
	 */
	public BinaryGreaterThanNextGatesImpl(SBool[] inA, SBool[] inB, SBool outC,
			AbstractBinaryFactory provider) {
		if (inA.length == inB.length) {
			this.provider = provider;
			this.outC = outC;
			this.inA = inA;
			this.inB = inB;
			this.length = inA.length;
		} else {
			throw new RuntimeException("Comparison failed: bitsize differs");
		}
		SBool[] inputs = new SBool[inA.length + inB.length];
		System.arraycopy(inA, 0, inputs, 0, inA.length);
		System.arraycopy(inB, 0, inputs, inA.length, inB.length);
		round = 0;
		setInputValues(inputs);
		setOutputValues(new SBool[] { outC });
		postfixResult = new SBool[this.length];
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if(round == 0){
			if(curGP == null){		
				curGP = new ParallelProtocolProducer();
		//		BasicLogicBuilder blb = new BasicLogicBuilder(provider);
		//		xor = blb.xor(inB, inA);
		//		curGP = blb.getCircuit();
				xor = new SBool[inB.length];
				for(int i = 0; i< inB.length; i++){
					xor[i] = provider.getKnownConstantSBool(false);
					((ParallelProtocolProducer)curGP).append(provider.getXorCircuit(inB[i], inA[i], xor[i]));	
				}
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}else if(round == 1){
			if(curGP == null){				
			//	BasicLogicBuilder blb = new BasicLogicBuilder(provider);
				//provider.getAndCircuit(inA[length-1], inRight, out)
				postfixResult[length-1] = provider.getSBool();
				curGP = provider.getAndCircuit(inA[length-1], xor[length-1], postfixResult[length-1]);
			//	postfixResult[length - 1] = blb.and(inA[length - 1], xor[length - 1]);
			//	curGP = blb.getCircuit();
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}else if(round >= 2 && round <= length){
			if(curGP == null){
				curGP = new SequentialProtocolProducer();
				//BasicLogicBuilder blb = new BasicLogicBuilder(provider);
				//blb.beginSeqScope();
				int i = length - round;
				SBool tmp = provider.getSBool();
				//postfixResult[i+1] = provider.getKnownConstantSBool(false);
				postfixResult[i] = provider.getSBool();
				((SequentialProtocolProducer)curGP).append(provider.getXorCircuit(inA[i], postfixResult[i+1], tmp));
				//SBool tmp = blb.xor(inA[i], postfixResult[i + 1]);
				((SequentialProtocolProducer)curGP).append(provider.getAndCircuit(xor[i], tmp, tmp));
				//tmp = blb.and(xor[i], tmp);
				//postfixResult[i] = blb.xor(tmp, postfixResult[i + 1]);
				((SequentialProtocolProducer)curGP).append(provider.getXorCircuit(tmp, postfixResult[i+1], postfixResult[i]));
			//	blb.endCurScope();
				//curGP = blb.getCircuit();
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}else if(round >length){
			if(curGP == null){				
				//BasicLogicBuilder blb = new BasicLogicBuilder(provider);
				curGP = provider.getCopyCircuit(postfixResult[0], outC);
				//blb.copy(postfixResult[0], outC);		
				//curGP = blb.getCircuit();
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
				done = true;
				if(Thread.currentThread().getName().equals("Thread-1")){
			//		System.out.println("finished a comp");	
				}
				
			}
		}		
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return !done;
	}
	
	
	@Override
	protected ProtocolProducer initializeGateProducer() {
/*		BasicLogicBuilder blb = new BasicLogicBuilder(provider);
		SBool[] postfixResult = new SBool[this.length];
		blb.beginSeqScope();
		SBool[] xor = blb.xor(inB, inA);
		postfixResult[length - 1] = blb.and(inA[length - 1], xor[length - 1]);
		SBool tmp;
		for (int i = length - 2; i >= 0; i--) {
			tmp = blb.xor(inA[i], postfixResult[i + 1]);
			tmp = blb.and(xor[i], tmp);
			postfixResult[i] = blb.xor(tmp, postfixResult[i + 1]);
		}
		blb.copy(postfixResult[0], outC);
		blb.endCurScope();
		return blb.getCircuit();*/
		return this;
	}
}
