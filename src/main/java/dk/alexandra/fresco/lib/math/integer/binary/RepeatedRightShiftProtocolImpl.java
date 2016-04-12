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
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

public class RepeatedRightShiftProtocolImpl implements RepeatedRightShiftProtocol {
	
	// Input
	private SInt x;
	private int n;
	private SInt result;
	
	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;

	// Variables used for calculation
	private int round = 0;
	private SInt in, out;
	private ProtocolProducer gp;
	private SInt[] remainders;
	
	public RepeatedRightShiftProtocolImpl(SInt x, int n, SInt result,
			BasicNumericFactory basicNumericFactory, 
			RightShiftFactory rightShiftFactory) {
		
		if (n < 1) {
			throw new MPCException("n must be positive");
		}
		
		this.x = x;
		this.n = n;
		this.result = result;
		
		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	public RepeatedRightShiftProtocolImpl(SInt x, int n, SInt result, SInt[] remainders,
			BasicNumericFactory basicNumericFactory, 
			RightShiftFactory rightShiftFactory) {
		
		this(x, n, result, basicNumericFactory, rightShiftFactory);
		if (remainders.length != n) {
			throw new MPCException("Length of array for remainders must match number of performed shifts");
		}
		this.remainders = remainders;
	}

	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {

			if (round == 0) {
				out = basicNumericFactory.getSInt();

				Protocol rightShift;
				if (remainders != null) {
					rightShift = rightShiftFactory.getRightShiftProtocol(x, out, remainders[round]); 
				} else {
					rightShift = rightShiftFactory.getRightShiftProtocol(x, out); 
				}
				gp = rightShift;
			} else if (round < n - 1) {
				in = out;
				out = basicNumericFactory.getSInt();
				Protocol rightShift;
				if (remainders != null) {
					rightShift = rightShiftFactory.getRightShiftProtocol(in, out, remainders[round]); 
				} else {
					rightShift = rightShiftFactory.getRightShiftProtocol(in, out); 
				}
				gp = rightShift;
			} else if (round == n - 1) {
				in = out;
				out = null;
				Protocol rightShift;
				if (remainders != null) {
					rightShift = rightShiftFactory.getRightShiftProtocol(in, result, remainders[round]); 
				} else {
					rightShift = rightShiftFactory.getRightShiftProtocol(in, result); 
				}
				gp = rightShift;
			}
			
		}
		if (gp.hasNextProtocols()) {
			pos = gp.getNextProtocols(gates, pos);
		} else if (!gp.hasNextProtocols()) {
			round++;
			gp = null;
		}
		return pos;
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
	public boolean hasNextProtocols() {
		return round < n;
	}

}
