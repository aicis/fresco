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
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.compare.KeyedCompareAndSwapCircuit;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class KeyedCompareAndSwapCircuitGetNextGatesImpl extends AbstractSimpleProtocol
		implements KeyedCompareAndSwapCircuit {

	private SBool[] leftKey;
	private SBool[] leftValue;
	private SBool[] rightKey;
	private SBool[] rightValue;
	private AbstractBinaryFactory bp;
	private ProtocolProducer curGP = null;
	private boolean done = false;
	private int round;
	
	private SBool compRes;
	private SBool[] tmpXORKey;
	private SBool[] tmpXORValue;

	/**
	 * Constructs a gate producer for the keyed compare and swap circuit. This
	 * circuit will compare the keys of two key-value pairs and swap the pairs
	 * so that the left pair has the largest key.
	 * 
	 * @param leftKey
	 *            the key of the left pair
	 * @param leftValue
	 *            the value of the left pair
	 * @param rightKey
	 *            the key of the right pair
	 * @param rightValue
	 *            the value of the right pair
	 * @param bp
	 *            a provider of binary circuits
	 */
	public KeyedCompareAndSwapCircuitGetNextGatesImpl(SBool[] leftKey, SBool[] leftValue,
			SBool[] rightKey, SBool[] rightValue, AbstractBinaryFactory bp) {
		this.leftKey = leftKey;
		this.leftValue = leftValue;
		this.rightKey = rightKey;
		this.rightValue = rightValue;
		this.bp = bp;
		this.round = 0;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if(round == 0){
			if(curGP == null){		
				curGP = new ParallelProtocolProducer();

				compRes=bp.getSBool();
				((ParallelProtocolProducer)curGP).append(bp.getBinaryComparisonCircuit(leftKey, rightKey, compRes));

				tmpXORKey = new SBool[leftKey.length];
				for(int i = 0; i< leftKey.length; i++){
					tmpXORKey[i] = bp.getSBool();
					((ParallelProtocolProducer)curGP).append(bp.getXorCircuit(leftKey[i], rightKey[i], tmpXORKey[i]));
				}
				
				tmpXORValue = new SBool[leftValue.length];
				for(int i = 0; i< leftValue.length; i++){
					tmpXORValue[i] = bp.getSBool();
					((ParallelProtocolProducer)curGP).append(bp.getXorCircuit(leftValue[i], rightValue[i], tmpXORValue[i]));
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
				BasicLogicBuilder blb = new BasicLogicBuilder(bp);
				blb.beginParScope();
				blb.condSelectInPlace(leftKey, compRes, leftKey, rightKey);
				blb.condSelectInPlace(leftValue, compRes, leftValue, rightValue);
				blb.endCurScope();
				curGP = blb.getCircuit();
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
			}
		}else if(round == 2){
			if(curGP == null){				
				curGP = new ParallelProtocolProducer();
				for(int i= 0; i< rightKey.length; i++){
					((ParallelProtocolProducer)curGP).append(bp.getXorCircuit(tmpXORKey[i], leftKey[i], rightKey[i]));
				}
				for(int i= 0; i< rightValue.length; i++){
					((ParallelProtocolProducer)curGP).append(bp.getXorCircuit(tmpXORValue[i], leftValue[i], rightValue[i]));
				}
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				round++;
				curGP = null;
				done = true;
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
		System.out.println("initGP new GP!");
		return this;
	}
	

}