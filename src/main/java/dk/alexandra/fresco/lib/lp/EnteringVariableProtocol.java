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
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductProtocol;

public class EnteringVariableProtocol extends AbstractRoundBasedProtocol {

	private final LPTableau tableau;
	private final Matrix<SInt> updateMatrix;
	private final SInt[] enteringIndex;
	private final SInt minimum;
	private LPFactory lpFactory;
	private BasicNumericFactory bnFactory;	
	int round = 0;
	private SInt[] updatedF;

	/**
	 * @param tableau
	 *            an (m + 1)x(n + m + 1) tableau
	 * @param updateMatrix
	 *            an (m + 1)x(m + 1) update matrix, multiplying the tableau on
	 *            the left with the update matrix gives the new tableau
	 * @param enteringIndex
	 *            the index of the variable to enter the basis
	 * @param factory
	 */
	public EnteringVariableProtocol(LPTableau tableau,
			Matrix<SInt> updateMatrix, SInt[] enteringIndex, SInt minimum,
			LPFactory lpFactory, BasicNumericFactory bnFactory) {
		if (checkDimensions(tableau, updateMatrix, enteringIndex)) {
			this.updateMatrix = updateMatrix;
			this.tableau = tableau;
			this.enteringIndex = enteringIndex;
			this.minimum = minimum;
			this.lpFactory = lpFactory;
			this.bnFactory = bnFactory;
		} else {
			throw new MPCException("Dimensions of inputs do not match");
		}
	}

	private boolean checkDimensions(LPTableau tableau,
			Matrix<SInt> updateMatrix, SInt[] enteringIndex) {
		int updateHeight = updateMatrix.getHeight();
		int updateWidth = updateMatrix.getWidth();
		int tableauHeight = tableau.getC().getHeight() + 1;
		int tableauWidth = tableau.getC().getWidth() + 1;
		return (updateHeight == updateWidth && updateHeight == tableauHeight && enteringIndex.length == tableauWidth - 1);
	}

	@Override
	public ProtocolProducer nextProtocolProducer() {
		ProtocolProducer pp = null;
		if (round == 0) {
			int updateVectorDimension = updateMatrix.getHeight();
			int numOfFs = tableau.getF().length;
			updatedF = new SInt[numOfFs];
			SInt[] updateVector = updateMatrix
					.getIthRow(updateVectorDimension - 1);
			ProtocolProducer[] updateFis = new ProtocolProducer[numOfFs];
			for (int i = 0; i < numOfFs; i++) {
				SInt[] dotProductResult = new SInt[updateVectorDimension];
				SInt[] constraintColumn = new SInt[updateVectorDimension];
				SInt[] temp = new SInt[updateVectorDimension - 1];
				temp = tableau.getC().getIthColumn(i, temp);
				for (int j = 0; j < updateVectorDimension - 1; j++) {
					dotProductResult[j] = bnFactory.getSInt();
					constraintColumn[j] = temp[j];
				}
				dotProductResult[updateVectorDimension - 1] = bnFactory
						.getSInt();
				constraintColumn[updateVectorDimension - 1] = tableau.getF()[i];
				EntrywiseProductProtocol dpc = lpFactory.getEntrywiseProductProtocol(
						constraintColumn, updateVector, dotProductResult);
				NumericProtocolBuilder build = new NumericProtocolBuilder(
						bnFactory);
				updatedF[i] = build.sum(dotProductResult);
				updateFis[i] = new SequentialProtocolProducer(dpc,
						build.getProtocol());
			}
			pp = new ParallelProtocolProducer(updateFis);
			round++;
		} else if (round == 1) {
			pp = lpFactory.getMinimumProtocol(updatedF, minimum, enteringIndex);
			updatedF = null;
			round++;
		} else {
			pp = null;
		}
		return pp;
	}
}
