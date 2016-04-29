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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class OptimalValueCircuit implements Protocol {
	
	
	private final SInt[] B;
	private final Matrix<SInt> updateMatrix;
	private final SInt pivot;
	private final SInt optimalValue;
	private LPFactory lpProvider;
	private BasicNumericFactory numericProvider;
	private ProtocolProducer gp;
	private boolean done = false;
	
	public OptimalValueCircuit(Matrix<SInt> updateMatrix, SInt[] B, SInt pivot, SInt optimalValue, 
			LPFactory lpProvider, BasicNumericFactory numericProvider) {
		this.updateMatrix = updateMatrix;
		this.B = B;
		this.pivot = pivot;
		this.optimalValue = optimalValue;
		this.lpProvider = lpProvider;
		this.numericProvider = numericProvider;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {
			SInt numerator = numericProvider.getSInt();
			SInt invDenominator = numericProvider.getSInt();
			
			// DEBUG			
			//GateProducer printNumerator = new OpenAndPrintCircuit("Num: ", numerator, numericProvider);
			//GateProducer printPivot = new OpenAndPrintCircuit("Pivot: ", pivot, numericProvider);
			//GateProducer printInvPivot = new OpenAndPrintCircuit("InvPivot: ", invDenominator, numericProvider);
			//GateProducer printOpt = new OpenAndPrintCircuit("Optimal: ", optimalValue, numericProvider);
			//SequentialGateProducer seq = new SequentialGateProducer(printInvPivot, printNumerator, printPivot, printOpt);
			
			SInt[] row = updateMatrix.getIthRow(updateMatrix.getHeight() - 1);
			SInt[] shortenedRow = new SInt[B.length];
			System.arraycopy(row, 0, shortenedRow, 0, B.length);
			ProtocolProducer innerProduct = lpProvider.getInnerProductCircuit(B, shortenedRow, numerator);
			ProtocolProducer inversion = lpProvider.getInversionProtocol(pivot, invDenominator);
			ProtocolProducer multiplication = numericProvider.getMultProtocol(numerator, invDenominator, optimalValue);
			gp = new SequentialProtocolProducer(innerProduct, inversion, multiplication);
			// DEBUG
			//gp = new SequentialGateProducer(innerProduct, inversion, multiplication, seq);
		} 
		if (gp.hasNextProtocols()) {
			pos = gp.getNextProtocols(gates, pos);
		} else if (!gp.hasNextProtocols()) {
			gp = null;
			done = true;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return !done;
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
