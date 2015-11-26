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
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class OpenAndPrintCircuit implements Protocol {
	
	private SInt number = null;
	private SInt[] vector = null;
	private SInt[][] matrix = null;
	
	private OInt oNumber = null;
	private OInt[] oVector = null;
	private OInt[][] oMatrix = null;
	
	private enum STATE {OUTPUT, WRITE, DONE};
	private STATE state = STATE.OUTPUT;
	private String label;
	
	ProtocolProducer gp = null;
	
	private BasicNumericFactory provider;
	

	public OpenAndPrintCircuit(String label, SInt number, BasicNumericFactory provider) {
		this.number = number;
		this.provider = provider;
		this.label = label;
	}
	
	public OpenAndPrintCircuit(String label, SInt[] vector, BasicNumericFactory provider) {
		this.vector = vector;
		this.provider = provider;
		this.label = label;
	}
	
	public OpenAndPrintCircuit(String label, SInt[][] matrix, BasicNumericFactory provider) {
		this.matrix = matrix;
		this.provider = provider;
		this.label = label;
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
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {
			if (state == STATE.OUTPUT) {
				if (number != null) {
					oNumber = provider.getOInt();
					gp = provider.getOpenCircuit(number, oNumber);
				} else if (vector != null) {
					oVector = Util.oIntFill(new OInt[vector.length], provider);
					gp = Util.makeOpenCircuit(vector, oVector, provider);
				} else {
					oMatrix = Util.oIntFill(new OInt[matrix.length][matrix[0].length], provider);
					gp = Util.makeOpenCircuit(matrix, oMatrix, provider);
				}
			} else if (state == STATE.WRITE) {
				StringBuilder sb = new StringBuilder();
				sb.append(label);
				if (oNumber != null) {
					sb.append(oNumber.getValue().toString());
				} else if (oVector != null) {
					sb.append('\n');
					for (OInt entry: oVector) {
						sb.append(entry.getValue().toString() + "\t");
					}
				} else if (oMatrix != null) {
					sb.append('\n');
					for (OInt[] row: oMatrix) {
						for (OInt entry: row) {
							sb.append(entry.getValue().toString() + "\t");
						}
						sb.append('\n');
					}
				}
				gp = new MarkerProtocolImpl(sb.toString());
			}			
		}
		if (gp.hasNextProtocols()) {
			pos = gp.getNextProtocols(gates, pos);
		} else if (!gp.hasNextProtocols()) {
			switch (state) {
			case OUTPUT:
				state = STATE.WRITE;
				gp = null;
				break;
			case WRITE:
				state = STATE.DONE;
				gp = null;
				break;
			default:
				break;
			}
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		// TODO Auto-generated method stub
		return state != STATE.DONE;
	}
	
}
