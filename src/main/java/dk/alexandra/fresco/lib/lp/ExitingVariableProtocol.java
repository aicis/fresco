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
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class ExitingVariableProtocol extends AbstractRoundBasedProtocol {

	private final LPTableau tableau;
	private final Matrix<SInt> updateMatrix;
	private final SInt[] enteringIndex, exitingIndex, updateColumn;
	private SInt[] enteringColumn;
	private final SInt pivot;
	private LPFactory lpf;
	private BasicNumericFactory bnf;
	private ProtocolProducer pp;
	private int round = 0;
	private SInt[] nonApps;
	private SInt[] updatedB;
	private SInt[] updatedEnteringColumn;
	private SInt zero, one;
	
	public ExitingVariableProtocol(LPTableau tableau, Matrix<SInt> updateMatrix, 
			SInt[] enteringIndex, SInt[] exitingIndex, SInt[] updateColumn, SInt pivot, LPFactory lpFactory, BasicNumericFactory bnFactory) {
		if (checkDimensions(tableau, updateMatrix, enteringIndex, exitingIndex, updateColumn)) {
			this.tableau = tableau;
			this.updateMatrix = updateMatrix;
			this.enteringIndex = enteringIndex;
			this.exitingIndex = exitingIndex;
			this.pivot = pivot;
			this.updateColumn = updateColumn;
			pp = null;
			this.lpf = lpFactory;
			this.bnf = bnFactory;

		} else {
			throw new MPCException("Dimensions of inputs does not match");
		}
	}
	
	private boolean checkDimensions (LPTableau tableau, Matrix<SInt> updateMatrix,
			SInt[] enteringIndex, SInt[] exitingIndex, SInt[] updateColumn) {
		int updateHeight = updateMatrix.getHeight();
		int updateWidth = updateMatrix.getWidth();
		int tableauHeight = tableau.getC().getHeight() + 1;
		int tableauWidth = tableau.getC().getWidth() + 1;		
		return (updateHeight == updateWidth && 
				updateHeight == tableauHeight && 
				enteringIndex.length == tableauWidth - 1 &&
				exitingIndex.length == tableauHeight - 1 &&
				updateColumn.length == updateWidth);
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

	@Override
	public ProtocolProducer nextProtocolProducer() {
		int tableauHeight = tableau.getC().getHeight() + 1;
		if (round == 0) {
			one = bnf.getSInt();
			zero = bnf.getSInt();
			ProtocolProducer load0 = bnf.getSInt(0, zero);
			ProtocolProducer load1 = bnf.getSInt(1, one);
			
			// Extract entering column
			enteringColumn = new SInt[tableauHeight];
			ProtocolProducer[] extractions = new ProtocolProducer[tableauHeight]; 
			for (int i = 0; i < enteringColumn.length - 1; i++) {
				enteringColumn[i] = bnf.getSInt();
				SInt[] tableauRow = tableau.getC().getIthRow(i);
				extractions[i] = lpf.getInnerProductProtocol(enteringIndex, tableauRow, enteringColumn[i]);
			}
			enteringColumn[enteringColumn.length - 1] = bnf.getSInt();
			SInt[] tableauRow = tableau.getF();
			extractions[enteringColumn.length - 1] = lpf.getInnerProductProtocol(enteringIndex, tableauRow, enteringColumn[enteringColumn.length - 1]);
			ParallelProtocolProducer par = new ParallelProtocolProducer(extractions);
			par.append(load0);
			par.append(load1);
			pp = par;
			round++;
		} else if (round == 1) {
			// Apply update matrix to entering column
			updatedEnteringColumn = new SInt[tableauHeight];
			ProtocolProducer[] enteringColumnUpdates = new ProtocolProducer[tableauHeight]; 
			for (int i = 0; i < enteringColumnUpdates.length; i++) {
				updatedEnteringColumn[i] = bnf.getSInt();
				SInt[] updateRow = updateMatrix.getIthRow(i);
				enteringColumnUpdates[i] = lpf.getInnerProductProtocol(updateRow, enteringColumn, updatedEnteringColumn[i]);
			}
			ProtocolProducer enteringColumnUpdateProducer = new ParallelProtocolProducer(enteringColumnUpdates);
						
			// Apply update matrix to the B vector
			updatedB = new SInt[tableauHeight - 1];
			ProtocolProducer[] bUpdates = new ProtocolProducer[tableauHeight - 1]; 
			for (int i = 0; i < updatedB.length; i++) {
				updatedB[i] = bnf.getSInt();
				SInt[] updateRow = new SInt[tableauHeight - 1];
				System.arraycopy(updateMatrix.getIthRow(i), 0, updateRow, 0, tableauHeight - 1);
				bUpdates[i] = lpf.getInnerProductProtocol(updateRow, tableau.getB(), updatedB[i]);
			}
			ParallelProtocolProducer par = new ParallelProtocolProducer(bUpdates);
			par.append(enteringColumnUpdateProducer);
			pp = par;
			round++;
		} else if (round == 2) {
			NumericProtocolBuilder npb = new NumericProtocolBuilder(bnf);
			nonApps = npb.getSIntArray(updatedB.length);
			npb.beginParScope();
			for (int i = 0 ; i < nonApps.length; i++) {
				ProtocolProducer pp = lpf.getComparisonProtocol(updatedEnteringColumn[i], zero, nonApps[i], true);
				npb.addProtocolProducer(pp);
			}
			npb.endCurScope();
			pp = npb.getProtocol();
			round++;
		} else if (round == 3) {
			SInt[] shortColumn = new SInt[updatedB.length];
			System.arraycopy(updatedEnteringColumn, 0, shortColumn, 0, shortColumn.length);
			// Determine exiting index
			pp = lpf.getMinInfFracProtocol(updatedB, shortColumn, 
					nonApps, bnf.getSInt(), bnf.getSInt(), bnf.getSInt(), exitingIndex);
			round++;
		} else if (round == 4) {
			// Compute column for the new update matrix 
			SInt[] negativeEnteringColumn = new SInt[tableauHeight - 1];
			ProtocolProducer[] updateColumnEntries = new ProtocolProducer[tableauHeight];
			for (int i = 0; i < tableauHeight - 1; i++) {
				negativeEnteringColumn[i] = bnf.getSInt();
				ProtocolProducer sub = bnf.getSubtractProtocol(zero, updatedEnteringColumn[i], negativeEnteringColumn[i]);
				ProtocolProducer cond = lpf.getConditionalSelectProtocol(exitingIndex[i], one, 
						negativeEnteringColumn[i], updateColumn[i]);
				updateColumnEntries[i] = new SequentialProtocolProducer(sub, cond);
			}
			updateColumnEntries[tableauHeight - 1] = bnf.getSubtractProtocol(zero, 
					updatedEnteringColumn[tableauHeight - 1], updateColumn[tableauHeight - 1]);
			pp = new ParallelProtocolProducer(updateColumnEntries);
			round++;
		} else if (round == 5) {
			// Determine pivot
			SInt[] parAdditionResults = new SInt[tableauHeight - 1];
			ProtocolProducer[] parAdditions = new ProtocolProducer[tableauHeight - 1];
			SInt[] seqAdditionResults = new SInt[tableauHeight - 1];
			ProtocolProducer[] seqAdditions = new ProtocolProducer[tableauHeight - 2];
			for (int i = 0; i < parAdditions.length; i++) {
				parAdditionResults[i] = bnf.getSInt();
				parAdditions[i] = bnf.getAddProtocol(updatedEnteringColumn[i], updateColumn[i], parAdditionResults[i]);
			}
			ProtocolProducer parAdditionProducer = new ParallelProtocolProducer(parAdditions);
			seqAdditionResults[0] = parAdditionResults[0];
			for (int i = 1; i < parAdditions.length; i++) {
				seqAdditionResults[i] = bnf.getSInt();
				seqAdditions[i - 1] = bnf.getAddProtocol(seqAdditionResults[i - 1], parAdditionResults[i], seqAdditionResults[i]);
			}
			ProtocolProducer seqAdditionProducer = new SequentialProtocolProducer(seqAdditions);
			ProtocolProducer subtractOne = bnf.getSubtractProtocol(seqAdditionResults[tableauHeight - 2], one, pivot);
			pp = new SequentialProtocolProducer(parAdditionProducer, seqAdditionProducer, subtractOne);
			round++;
		} else {
			updatedB = null;
			updatedEnteringColumn = null;
			pp = null;
		}
		return pp;		
	}

}
