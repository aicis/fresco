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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class BlandEnteringVariableCircuit extends AbstractSimpleProtocol {

	private final LPTableau tableau;
	private final Matrix<SInt> updateMatrix;
	private final SInt[] enteringIndex;
	private final SInt termination;
	private LPFactory lpProvider;
	private BasicNumericFactory bnProvider;
	
	
	/**
	 * @param tableau  an (m + 1)x(n + m + 1) tableau
	 * @param updateMatrix an (m + 1)x(m + 1) update matrix, multiplying the tableau on the left 
	 * with the update matrix gives the new tableau 
	 * @param enteringIndex the index of the variable to enter the basis chosen by Blands rule
	 * @param termination should have the value 1 if ready for termination, and 0 otherwise
	 * @param provider
	 */
	public BlandEnteringVariableCircuit(LPTableau tableau, Matrix<SInt> updateMatrix, 
			SInt[] enteringIndex, SInt termination, LPFactory lpProvider, BasicNumericFactory bnProvider) {
		if (checkDimensions(tableau, updateMatrix, enteringIndex)) {
			this.updateMatrix = updateMatrix;
			this.tableau = tableau;
			this.enteringIndex = enteringIndex;
			this.termination = termination;
			this.lpProvider = lpProvider;
			this.bnProvider = bnProvider;
		} else {
			throw new MPCException("Dimensions of inputs do not match");
		}
	}
	
	private boolean checkDimensions (LPTableau tableau, Matrix<SInt> updateMatrix, SInt[] enteringIndex) {
		int updateHeight = updateMatrix.getHeight();
		int updateWidth = updateMatrix.getWidth();
		int tableauHeight = tableau.getC().getHeight() + 1;
		int tableauWidth = tableau.getC().getWidth() + 1;		
		return (updateHeight == updateWidth && updateHeight == tableauHeight && enteringIndex.length == tableauWidth - 1);
	}
	
	
	@Override
	protected ProtocolProducer initializeGateProducer() {
		SInt negativeOne = bnProvider.getSInt();
		SInt one = bnProvider.getSInt();		
		ProtocolProducer loadValues = new ParallelProtocolProducer(bnProvider.getSInt(-1, negativeOne), bnProvider.getSInt(1, one));
		
		SInt[] updatedF = Util.sIntFill(new SInt[tableau.getF().length], bnProvider);
		ProtocolProducer updateFProducer = getUpdateFProducer(updatedF);
		SInt[] signs = Util.sIntFill(new SInt[updatedF.length], bnProvider);
		ParallelProtocolProducer signTest = new ParallelProtocolProducer();
		for (int i = 0; i < updatedF.length; i++) {
			ProtocolProducer comp = lpProvider.getComparisonCircuit(
					updatedF[i], negativeOne, signs[i], false);
			signTest.append(comp);
		}
		
		SequentialProtocolProducer prefixSum = new SequentialProtocolProducer();
		for (int i = 1; i < updatedF.length; i++) {
			prefixSum.append(bnProvider.getAddProtocol(
					signs[i-1], 
					signs[i], 
					signs[i]));
		}
		
		SInt[] pairwiseSums = Util.sIntFill(new SInt[signs.length], bnProvider);
		ParallelProtocolProducer pairwiseSum = new ParallelProtocolProducer();
		pairwiseSums[0] = signs[0];
		for (int i = 1; i < updatedF.length; i++) {
			pairwiseSum.append(bnProvider.getAddProtocol(
					signs[i-1], 
					signs[i], 
					pairwiseSums[i]));
		}
		
		ParallelProtocolProducer findIndex = new ParallelProtocolProducer();
		int bitlength = (int)Math.log(signs.length)*2 + 1; 
		for (int i = 0; i < updatedF.length; i++) {
			// TODO: The below can probably be done more efficient with a zero test
			ProtocolProducer eq = lpProvider.getEqualityCircuit(
					bitlength, 
					80, 
					pairwiseSums[i], 
					one, 
					enteringIndex[i]);
			findIndex.append(eq);
		}		
		
		SequentialProtocolProducer decideTermination = new SequentialProtocolProducer();
		decideTermination.append(lpProvider.getCopyCircuit(enteringIndex[0], termination));
		for (int i = 1; i < enteringIndex.length; i++) {
			decideTermination.append(bnProvider.getAddProtocol(termination, enteringIndex[i], termination));
		}
		decideTermination.append(bnProvider.getSubtractCircuit(one, termination, termination));
		
		SequentialProtocolProducer gp = new SequentialProtocolProducer(
				loadValues,
				updateFProducer,
				signTest,
				prefixSum,
				pairwiseSum,
				findIndex, 
				decideTermination);
		
		return gp;
	}
	
	private ProtocolProducer getUpdateFProducer(SInt[] updatedF) {
		int updateVectorDimension = updateMatrix.getHeight();
		int numOfFs = tableau.getF().length;
		SInt[] updateVector = updateMatrix.getIthRow(updateVectorDimension - 1);
				
		ProtocolProducer[] updateFis = new ProtocolProducer[numOfFs];
		for (int i = 0; i < numOfFs; i++) {
			SInt[] constraintColumn = new SInt[updateVectorDimension];
			SInt[] temp = new SInt[updateVectorDimension - 1];
			temp = tableau.getC().getIthColumn(i, temp);
			for (int j = 0; j < updateVectorDimension - 1; j++) {
				constraintColumn[j] = temp[j];
			}
			constraintColumn[updateVectorDimension - 1] = tableau.getF()[i];
			
			updateFis[i] = lpProvider.getInnerProductCircuit(
					constraintColumn, 
					updateVector, 
					updatedF[i]);
		}
		return new ParallelProtocolProducer(updateFis);
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
