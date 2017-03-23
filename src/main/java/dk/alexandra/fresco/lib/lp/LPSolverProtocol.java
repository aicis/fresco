/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;

public class LPSolverProtocol implements Protocol {

	private final LPTableau tableau;
	private final Matrix<SInt> updateMatrix;
	private final SInt zero;

	private enum STATE {
		PHASE1, PHASE2, TERMINATED
	};

	private STATE state;
	private LPFactory lpFactory;
	private BasicNumericFactory bnFactory;
	private ProtocolProducer pp;
	private OInt terminationOut;
	private Matrix<SInt> newUpdateMatrix;
	private final SInt prevPivot;
	private SInt pivot;
	private SInt[] enteringIndex;
	
	private final SInt[] basis;
	private final OInt[] enumeratedVariables; //[1,2,3,...]
	private SInt E; //tmp variable for which variable is entering the basis
	
	public static int iterations = 0;
	private final int noVariables;
	private final int noConstraints;
	
	public LPSolverProtocol(LPTableau tableau, Matrix<SInt> updateMatrix,
			SInt pivot, SInt[] basis, LPFactory lpFactory, BasicNumericFactory bnFactory) {
		if (checkDimensions(tableau, updateMatrix)) {
			this.tableau = tableau;
			this.updateMatrix = updateMatrix;
			this.prevPivot = pivot;
			this.pp = null;
			this.lpFactory = lpFactory;
			this.bnFactory = bnFactory;
			this.zero = bnFactory.getSInt(0);
			this.state = STATE.PHASE1;
			iterations = 0;
			
			this.basis = basis;
			this.noVariables = tableau.getC().getWidth();
			this.noConstraints = tableau.getC().getHeight();
			
			this.enumeratedVariables = new OInt[noVariables];
			for(int i = 1; i <= enumeratedVariables.length; i++) {
				this.enumeratedVariables[i-1] = this.bnFactory.getOInt(BigInteger.valueOf(i));
			}
			
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
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if (pp == null) {
			if (state == STATE.PHASE1) {
				iterations++;
				pp = phaseOneProtocol();
			} else if (state == STATE.PHASE2) {
				boolean terminated = terminationOut.getValue().equals(
						BigInteger.ONE);
				if (!terminated) {					
					pp = phaseTwoProtocol();					
				} else {
					state = STATE.TERMINATED;
					pp = null;
					return pos;
				}
			}
		}
		if (pp.hasNextProtocols()) {
			int end = pp.getNextProtocols(nativeProtocols, pos);
			pos = end;
		} else if (!pp.hasNextProtocols()) {
			switch (state) {
			case PHASE1:
				pp = null;
				state = STATE.PHASE2;
				break;
			case PHASE2:
				pp = null;
				state = STATE.PHASE1;
				break;
			case TERMINATED:
				pp = null;
				break;
			default:
				break;
			}
		}
		return pos;
	}

	private ProtocolProducer phaseTwoProtocol() {
		// Phase 2 - Finding the exiting variable and updating the tableau
		ProtocolProducer phaseTwo = new AbstractRoundBasedProtocol() {
			int round = 0;
			SInt[] exitingIndex, updateColumn;
			SInt[][] newUpdate;			
			
			@Override
			public ProtocolProducer nextProtocolProducer() {
				switch (round) {
				case 0:					
					exitingIndex = new SInt[noConstraints];
					for (int i = 0; i < exitingIndex.length; i++) {
						exitingIndex[i] = bnFactory.getSInt();
					}
					updateColumn = new SInt[noConstraints + 1];
					for (int i = 0; i < updateColumn.length; i++) {
						updateColumn[i] = bnFactory.getSInt();
					}
					pivot = bnFactory.getSInt();
					ProtocolProducer exitingIndexProducer = lpFactory
							.getExitingVariableProtocol(tableau, updateMatrix,
									enteringIndex, exitingIndex, updateColumn, pivot);
					
					round++;
					
					return exitingIndexProducer;
				case 1:
					SequentialProtocolProducer seq = new SequentialProtocolProducer();
					//update basis							
					OmniBuilder b = new OmniBuilder(bnFactory);
					seq.append(b.getProtocol());
					ParallelProtocolProducer par = new ParallelProtocolProducer();
					for(int i = 0; i < noConstraints; i++) {
						par.append(lpFactory.getConditionalSelectProtocol(exitingIndex[i], E, basis[i], basis[i]));
					}
					
					seq.append(par);
					
					//Update matrix
					newUpdate = new SInt[noConstraints + 1][noConstraints + 1];
					for (int i = 0; i < newUpdate.length; i++) {
						for (int j = 0; j < newUpdate[i].length; j++) {
							newUpdate[i][j] = bnFactory.getSInt();
						}
					}					
					newUpdateMatrix = new Matrix<SInt>(newUpdate);
					ProtocolProducer updateMatrixProducer = lpFactory.getUpdateMatrixProtocol(
							updateMatrix, exitingIndex, updateColumn, pivot, prevPivot,
							newUpdateMatrix);
					round++;
					seq.append(updateMatrixProducer);
					return seq;
				case 2:
					ParallelProtocolProducer parCopy = new ParallelProtocolProducer();
					for (int i = 0; i < newUpdate.length; i++) {
						for (int j = 0; j < newUpdate[i].length; j++) {
							CopyProtocol<SInt> copy = lpFactory.getCopyProtocol(
									newUpdateMatrix.getElement(i, j),
									updateMatrix.getElement(i, j));
							parCopy.append(copy);
						}
					}
					CopyProtocol<SInt> copy = lpFactory.getCopyProtocol(pivot, prevPivot);
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

	private ProtocolProducer phaseOneProtocol() {
		terminationOut = bnFactory.getOInt();
		// Phase 1 - Finding the entering variable and outputting
		// whether or not the corresponding F value is positive (a positive
		// value indicating termination)
		enteringIndex = new SInt[noVariables];
		for (int i = 0; i < noVariables; i++) {
			enteringIndex[i] = bnFactory.getSInt();
		}

		SInt minimum = bnFactory.getSInt();
		ProtocolProducer enteringProducer = lpFactory.getEnteringVariableProtocol(
				tableau, updateMatrix, enteringIndex, minimum);
		SInt positive = bnFactory.getSInt();
		ProtocolProducer comp = lpFactory.getComparisonProtocol(zero, minimum,
				positive, true);
		ProtocolProducer output = bnFactory.getOpenProtocol(positive,
				terminationOut);
		
		//entering found and is on the form [0,0,...,1,0,0,...], where the 1 is at the index of the entering variable
		//We now do the inner product between entering and [1,2,3,...,n] where n is the number of variables.
		InnerProductFactory ipf = new InnerProductFactoryImpl(bnFactory);
		E = bnFactory.getSInt();
		ProtocolProducer ipp = ipf.getInnerProductProtocol(enteringIndex, enumeratedVariables, E);
		
		ProtocolProducer phaseOne = new SequentialProtocolProducer(enteringProducer,
				comp, output, ipp);
		return phaseOne;
	}
		
	@SuppressWarnings("unused")
	private ProtocolProducer blandPhaseOneProtocol() {
		terminationOut = bnFactory.getOInt();
		// Phase 1 - Finding the entering variable and outputting
		// whether or not the corresponding F value is positive (a positive
		// value indicating termination)
		enteringIndex = new SInt[noVariables];
		for (int i = 0; i < noVariables; i++) {
			enteringIndex[i] = bnFactory.getSInt();
		}

		SInt first = bnFactory.getSInt();
		ProtocolProducer blandEnter = new BlandEnteringVariableProtocol(tableau,
				updateMatrix, enteringIndex, first, lpFactory, bnFactory);

		ProtocolProducer output = bnFactory.getOpenProtocol(first,
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
		SInt[] basis;
		LPFactory lpFactory;
		BasicNumericFactory bnFactory;

		public Builder() {
			this.tableau = null;
			this.updateMatrix = null;
			this.pivot = null;
			this.basis = null;
			this.lpFactory = null;			
			this.bnFactory = null;
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
		
		public Builder basis(SInt[] basis) {
			this.basis = basis;
			return this;
		}

		public Builder lpFactory(LPFactory lpf) {
			this.lpFactory = lpf;
			return this;
		}

		public Builder bnFactory(BasicNumericFactory bnf) {
			this.bnFactory = bnf;
			return this;
		}

		public <T extends BasicNumericFactory & LPFactory> Builder omniProvider(
				T factory) {
			this.lpFactory = factory;
			this.bnFactory = factory;
			return this;
		}

		public LPSolverProtocol build() {
			if (this.tableau != null && this.updateMatrix != null
					&& this.pivot != null && this.basis != null && this.lpFactory != null
					&& this.bnFactory != null) {
				return new LPSolverProtocol(tableau, updateMatrix, pivot,
						basis, lpFactory, bnFactory);
			} else {
				throw new IllegalStateException(
						"Not ready to build. Some values where not set.");
			}
		}
	}
}
