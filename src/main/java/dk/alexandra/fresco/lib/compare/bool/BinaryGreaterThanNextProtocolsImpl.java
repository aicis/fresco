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
 * Represents a comparison protocol between two bitstrings. Concretely, the
 * protocol computes the 'greater than' relation of strings A and B, i.e., it
 * computes C := A > B.
 * 
 * This uses the method of GenericBinaryComparison2 but is implemented a lot
 * cleaner and fixes some bugs.
 * 
 * @author psn
 * 
 */
public class BinaryGreaterThanNextProtocolsImpl extends AbstractSimpleProtocol
		implements BinaryGreaterThanProtocol {

	private SBool[] inA;
	private SBool[] inB;
	private SBool outC;

	private SBool[] postfixResult;

	private AbstractBinaryFactory factory;

	private int length;
	
	private boolean done;
	private int round;
	private ProtocolProducer curPP;
	private SBool[] xor;

	/**
	 * Construct a protocol to compare strings A and B. The bitstrings A and B
	 * are assumed to be even length and to be ordered from most- to least
	 * significant bit.
	 * 
	 * @param inA
	 *            input string A
	 * @param inB
	 *            input string B
	 * @param outC
	 *            a bit to hold the output C := A > B.
	 * @param factory
	 *            a protocol factory
	 */
	public BinaryGreaterThanNextProtocolsImpl(SBool[] inA, SBool[] inB, SBool outC,
			AbstractBinaryFactory factory) {
		if (inA.length == inB.length) {
			this.factory = factory;
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
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if(round == 0){
			if(curPP == null){		
				curPP = new ParallelProtocolProducer();
		//		BasicLogicBuilder blb = new BasicLogicBuilder(provider);
		//		xor = blb.xor(inB, inA);
		//		curGP = blb.getprotocol();
				xor = new SBool[inB.length];
				for(int i = 0; i< inB.length; i++){
					xor[i] = factory.getKnownConstantSBool(false);
					((ParallelProtocolProducer)curPP).append(factory.getXorProtocol(inB[i], inA[i], xor[i]));	
				}
			}
			if(curPP.hasNextProtocols()){
				pos = curPP.getNextProtocols(nativeProtocols, pos);
			}
			else if(!curPP.hasNextProtocols()){
				round++;
				curPP = null;
			}
		}else if(round == 1){
			if(curPP == null){				
			//	BasicLogicBuilder blb = new BasicLogicBuilder(provider);
				//provider.getAndprotocol(inA[length-1], inRight, out)
				postfixResult[length-1] = factory.getSBool();
				curPP = factory.getAndProtocol(inA[length-1], xor[length-1], postfixResult[length-1]);
			//	postfixResult[length - 1] = blb.and(inA[length - 1], xor[length - 1]);
			//	curGP = blb.getprotocol();
			}
			if(curPP.hasNextProtocols()){
				pos = curPP.getNextProtocols(nativeProtocols, pos);
			}
			else if(!curPP.hasNextProtocols()){
				round++;
				curPP = null;
			}
		}else if(round >= 2 && round <= length){
			if(curPP == null){
				curPP = new SequentialProtocolProducer();
				//BasicLogicBuilder blb = new BasicLogicBuilder(provider);
				//blb.beginSeqScope();
				int i = length - round;
				SBool tmp = factory.getSBool();
				//postfixResult[i+1] = provider.getKnownConstantSBool(false);
				postfixResult[i] = factory.getSBool();
				((SequentialProtocolProducer)curPP).append(factory.getXorProtocol(inA[i], postfixResult[i+1], tmp));
				//SBool tmp = blb.xor(inA[i], postfixResult[i + 1]);
				((SequentialProtocolProducer)curPP).append(factory.getAndProtocol(xor[i], tmp, tmp));
				//tmp = blb.and(xor[i], tmp);
				//postfixResult[i] = blb.xor(tmp, postfixResult[i + 1]);
				((SequentialProtocolProducer)curPP).append(factory.getXorProtocol(tmp, postfixResult[i+1], postfixResult[i]));
			//	blb.endCurScope();
				//curGP = blb.getCircuit();
			}
			if(curPP.hasNextProtocols()){
				pos = curPP.getNextProtocols(nativeProtocols, pos);
			}
			else if(!curPP.hasNextProtocols()){
				round++;
				curPP = null;
			}
		}else if(round >length){
			if(curPP == null){				
				//BasicLogicBuilder blb = new BasicLogicBuilder(provider);
				curPP = factory.getCopyProtocol(postfixResult[0], outC);
				//blb.copy(postfixResult[0], outC);		
				//curGP = blb.getCircuit();
			}
			if(curPP.hasNextProtocols()){
				pos = curPP.getNextProtocols(nativeProtocols, pos);
			}
			else if(!curPP.hasNextProtocols()){
				round++;
				curPP = null;
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
	protected ProtocolProducer initializeProtocolProducer() {
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
