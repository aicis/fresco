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

import java.math.BigInteger;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.GateRegister;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class LPSolverCircuit implements Protocol {

	private final LPTableau tableau;
	private final Matrix<SInt> updateMatrix;
	private final SInt zero;

	private enum STATE {
		PHASE1, PHASE2, TERMINATED
	};

	private STATE state;
	private LPFactory lpProvider;
	private BasicNumericFactory bnProvider;
	private ProtocolProducer gp;
	private OInt terminationOut;
	private Matrix<SInt> newUpdateMatrix;
	private final SInt prevPivot;
	private SInt pivot;
	private SInt[] enteringIndex;
	public static int iterations = 0;
	
	public LPSolverCircuit(LPTableau tableau, Matrix<SInt> updateMatrix,
			SInt pivot, LPFactory lpProvider, BasicNumericFactory bnProvider) {
		if (checkDimensions(tableau, updateMatrix)) {
			this.tableau = tableau;
			this.updateMatrix = updateMatrix;
			this.prevPivot = pivot;
			this.gp = null;
			this.lpProvider = lpProvider;
			this.bnProvider = bnProvider;
			this.zero = bnProvider.getSInt(0);
			this.state = STATE.PHASE1;
			iterations = 0;
		} else {
			throw new MPCException("Dimensions of inputs does not match");
		}
	}

	private boolean checkDimensions(LPTableau tableau, Matrix<SInt> updateMatrix) {
		int updateHeight = updateMatrix.getHeight();
		int updateWidth = updateMatrix.getWidth();
		int tableauHeight = tableau.getC().getHeight() + 1;
		return (updateHeight == updateWidth && updateHeight == tableauHeight);

	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {
			if (state == STATE.PHASE1) {
				iterations++;
				gp = phaseOneCircuit();
				// gp = blandPhaseOneCircuit();				
			} else if (state == STATE.PHASE2) {
				boolean terminated = terminationOut.getValue().equals(
						BigInteger.ONE);
				if (!terminated) {					
					gp = phaseTwoCircuit();					
				} else {
					state = STATE.TERMINATED;
					gp = null;
					return pos;
				}
			}
		}
		if (gp.hasNextProtocols()) {
			int end = gp.getNextProtocols(gates, pos);
			GateRegister.registerGates(gates, pos, end, this);
			pos = end;
		} else if (!gp.hasNextProtocols()) {
			switch (state) {
			case PHASE1:
				gp = null;
				state = STATE.PHASE2;
				break;
			case PHASE2:
				gp = null;
				state = STATE.PHASE1;
				break;
			case TERMINATED:
				gp = null;
				break;
			default:
				break;
			}
		}
		return pos;
	}

	private ProtocolProducer phaseTwoCircuit() {
		// Phase 2 - Finding the exiting variable and updating the tableau
		ProtocolProducer phaseTwo = new AbstractRoundBasedProtocol() {
			int round = 0;
			SInt[] exitingIndex, updateColumn;
			SInt[][] newUpdate;			
			
			@Override
			public ProtocolProducer nextProtocolProducer() {
				int noConstraints = tableau.getC().getHeight();
				switch (round) {
				case 0:					
					exitingIndex = new SInt[noConstraints];
					for (int i = 0; i < exitingIndex.length; i++) {
						exitingIndex[i] = bnProvider.getSInt();
					}
					updateColumn = new SInt[noConstraints + 1];
					for (int i = 0; i < updateColumn.length; i++) {
						updateColumn[i] = bnProvider.getSInt();
					}

					pivot = bnProvider.getSInt();
					ProtocolProducer exitingIndexProducer = lpProvider
							.getExitingVariableCircuit(tableau, updateMatrix,
									enteringIndex, exitingIndex, updateColumn, pivot);
					round++;
					return exitingIndexProducer;
				case 1:
					newUpdate = new SInt[noConstraints + 1][noConstraints + 1];
					for (int i = 0; i < newUpdate.length; i++) {
						for (int j = 0; j < newUpdate[i].length; j++) {
							newUpdate[i][j] = bnProvider.getSInt();
						}
					}					
					newUpdateMatrix = new Matrix<SInt>(newUpdate);
					ProtocolProducer updateMatrixProducer = lpProvider.getUpdateMatrixCircuit(
							updateMatrix, exitingIndex, updateColumn, pivot, prevPivot,
							newUpdateMatrix);
					round++;
					return updateMatrixProducer;
				case 2:
					ParallelProtocolProducer parCopy = new ParallelProtocolProducer();
					for (int i = 0; i < newUpdate.length; i++) {
						for (int j = 0; j < newUpdate[i].length; j++) {
							CopyProtocol<SInt> copy = lpProvider.getCopyCircuit(
									newUpdateMatrix.getElement(i, j),
									updateMatrix.getElement(i, j));
							parCopy.append(copy);
						}
					}
					CopyProtocol<SInt> copy = lpProvider.getCopyCircuit(pivot, prevPivot);
					parCopy.append(copy);
					round++;
					return parCopy;
				default:
					break;
				}
				return null;
			}
		};		
		return phaseTwo;
	}

	private ProtocolProducer phaseOneCircuit() {
		int noVariables = tableau.getC().getWidth();
		terminationOut = bnProvider.getOInt();
		// Phase 1 - Finding the entering variable and outputting
		// whether or not the corresponding F value is positive (a positive
		// value indicating termination)
		enteringIndex = new SInt[noVariables];
		for (int i = 0; i < noVariables; i++) {
			enteringIndex[i] = bnProvider.getSInt();
		}

		SInt minimum = bnProvider.getSInt();
		ProtocolProducer enteringProducer = lpProvider.getEnteringVariableCircuit(
				tableau, updateMatrix, enteringIndex, minimum);
		SInt positive = bnProvider.getSInt();
		ProtocolProducer comp = lpProvider.getComparisonCircuit(zero, minimum,
				positive, true);
		ProtocolProducer output = bnProvider.getOpenProtocol(positive,
				terminationOut);
		ProtocolProducer phaseOne = new SequentialProtocolProducer(enteringProducer,
				comp, output);
		return phaseOne;
	}
		
	@SuppressWarnings("unused")
	private ProtocolProducer blandPhaseOneCircuit() {
		int noVariables = tableau.getC().getWidth();
		terminationOut = bnProvider.getOInt();
		// Phase 1 - Finding the entering variable and outputting
		// whether or not the corresponding F value is positive (a positive
		// value indicating termination)
		enteringIndex = new SInt[noVariables];
		for (int i = 0; i < noVariables; i++) {
			enteringIndex[i] = bnProvider.getSInt();
		}

		SInt first = bnProvider.getSInt();
		ProtocolProducer blandEnter = new BlandEnteringVariableCircuit(tableau,
				updateMatrix, enteringIndex, first, lpProvider, bnProvider);

		ProtocolProducer output = bnProvider.getOpenProtocol(first,
				terminationOut);
		ProtocolProducer phaseOne = new SequentialProtocolProducer(blandEnter, output);
		return phaseOne;
	}

	@Override
	public boolean hasNextProtocols() {
		return (state != STATE.TERMINATED);
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

	public static class Builder {

		LPTableau tableau;
		Matrix<SInt> updateMatrix;
		SInt pivot;
		LPFactory lpProvider;
		BasicNumericFactory bnProvider;

		public Builder() {
			this.tableau = null;
			this.updateMatrix = null;
			this.pivot = null;
			this.lpProvider = null;
			this.bnProvider = null;
		}

		public Builder tableau(LPTableau tableau) {
			this.tableau = tableau;
			return this;
		}

		public Builder updateMatrix(Matrix<SInt> updateMatrix) {
			this.updateMatrix = updateMatrix;
			return this;
		}

		public Builder pivot(SInt pivot) {
			this.pivot = pivot;
			return this;
		}

		public Builder lpProvider(LPFactory lpProvider) {
			this.lpProvider = lpProvider;
			return this;
		}

		public Builder bnProvider(BasicNumericFactory bnProvider) {
			this.bnProvider = bnProvider;
			return this;
		}

		public <T extends BasicNumericFactory & LPFactory> Builder omniProvider(
				T provider) {
			this.lpProvider = provider;
			this.bnProvider = provider;
			return this;
		}

		public LPSolverCircuit build() {
			if (this.tableau != null && this.updateMatrix != null
					&& this.pivot != null && this.lpProvider != null
					&& this.bnProvider != null) {
				return new LPSolverCircuit(tableau, updateMatrix, pivot,
						lpProvider, bnProvider);
			} else {
				throw new IllegalStateException(
						"Not ready to build. Some values where not set.");
			}
		}
	}
}
