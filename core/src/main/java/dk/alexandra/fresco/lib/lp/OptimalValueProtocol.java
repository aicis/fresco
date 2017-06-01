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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * Protocol extracting the optimal value from a {@link LPTableau} and an update 
 * matrix representing a terminated Simplex method.  
 */
public class OptimalValueProtocol implements Protocol {
		
	private final SInt[] B;
	private final Matrix<SInt> updateMatrix;
	private final SInt pivot;
	private final SInt optimalValue;
	private final LPTableau tableau;
	private LPFactory lpFactory;
	private BasicNumericFactory numericFactory;
	private ProtocolProducer pp;
	private boolean done = false;
	
	/**
     * An version of the protocol working for initial tableaus with the <sub>z</sup> value set to zero
     * @param updateMatrix the final update matrix
     * @param B the B vector of the initial tableau 
     * @param pivot the final pivot
     * @param optimalValue an SInt to put the result
     * @param lpFactory an LPFactory
     * @param numericFactory a BasicNumericFactory
     */
	public OptimalValueProtocol(Matrix<SInt> updateMatrix, SInt[] B, SInt pivot, SInt optimalValue, 
			LPFactory lpFactory, BasicNumericFactory numericFactory) {
		this.updateMatrix = updateMatrix;
		this.B = B;
		this.tableau = null;
		this.pivot = pivot;
		this.optimalValue = optimalValue;
		this.lpFactory = lpFactory;
		this.numericFactory = numericFactory;
	}
	
	/**
     * An general version of the protocol working any (valid) initial tableau.
     * @param updateMatrix the final update matrix
     * @param tableau the initial tableau 
     * @param pivot the final pivot
     * @param optimalValue an SInt to put the result
     * @param lpFactory an LPFactory
     * @param numericFactory a BasicNumericFactory
     */
	public OptimalValueProtocol(Matrix<SInt> updateMatrix, LPTableau tableau, SInt pivot, SInt optimalValue, 
        LPFactory lpFactory, BasicNumericFactory numericFactory) {
    this.updateMatrix = updateMatrix;
    this.B = null;
    this.tableau = tableau;
    this.pivot = pivot;
    this.optimalValue = optimalValue;
    this.lpFactory = lpFactory;
    this.numericFactory = numericFactory;
}

	@Override
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if (pp == null) {
		  if (tableau == null) {
		    SInt numerator = numericFactory.getSInt();
            SInt invDenominator = numericFactory.getSInt();         
            SInt[] row = updateMatrix.getIthRow(updateMatrix.getHeight() - 1);
            SInt[] shortenedRow = new SInt[B.length];
            System.arraycopy(row, 0, shortenedRow, 0, B.length);
            ProtocolProducer innerProduct = lpFactory.getInnerProductProtocol(B, shortenedRow, numerator);
            ProtocolProducer inversion = lpFactory.getInversionProtocol(pivot, invDenominator);
            ProtocolProducer multiplication = numericFactory.getMultProtocol(numerator, invDenominator, optimalValue);
            pp = new SequentialProtocolProducer(innerProduct, inversion, multiplication);
		  } else {
		    SInt numerator = numericFactory.getSInt();
            SInt invDenominator = numericFactory.getSInt();         
            SInt[] row = updateMatrix.getIthRow(updateMatrix.getHeight() - 1);
            SInt[] column = new SInt[row.length];
            column[column.length - 1] = tableau.getZ();
            System.arraycopy(tableau.getB(), 0, column, 0, tableau.getB().length);
            ProtocolProducer innerProduct = lpFactory.getInnerProductProtocol(row, column, numerator);
            ProtocolProducer inversion = lpFactory.getInversionProtocol(pivot, invDenominator);
            ProtocolProducer multiplication = numericFactory.getMultProtocol(numerator, invDenominator, optimalValue);
            pp = new SequentialProtocolProducer(innerProduct, inversion, multiplication);
		  }
		} 
		if (pp.hasNextProtocols()) {
			pos = pp.getNextProtocols(nativeProtocols, pos);
		} else if (!pp.hasNextProtocols()) {
			pp = null;
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
