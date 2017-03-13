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
package dk.alexandra.fresco.lib.helper.bristol;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;

/**
 * A circuit that is based on Bristol circuits, i.e., it reads a textual
 * description of a circuit in Bristol format (see
 * https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/) using the
 * BristolCircuitParser.
 * 
 */
public class BristolCircuit implements Protocol {

	private SBool[] in1, in2, out;
	
	private BristolCircuitParser parser;
	
	private int pos = 0;
	
	public BristolCircuit(BristolCircuitParser parser, SBool[] in1, SBool[] in2, SBool[] out) {
		this.in1= in1;
		this.in2= in2;
		this.out = out;
		this.parser = parser;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] protocols, int pos) {
		// TODO: This is a bit hacky. 
		// It only works if the BasicLogicFactory given to CircuitParser
		// produces circuits for AND, XOR, NOT that are native protocols.
		// Otherwise, more book-keeping is needed here.
		NativeProtocol[] tmp = new NativeProtocol[5];
		Protocol[] c = new Protocol[1];
		while (pos < protocols.length) {
			int resCircuit = this.parser.getNext(c, 0);
			if (resCircuit == 0) {
				// The next circuit has input that depends on some unReady input, or end of circuit reached. End batch.
				break;
			} else if (resCircuit != 1) {
				throw new MPCException("Weird, should give one circuit exactly, pos: " + resCircuit);
			}
			
			// Got exactly one circuit.
			int res = c[0].getNextProtocols(tmp, 0);
			if (res == 0) {
				break; // End of circuit reached.
			}
			if (res != 1)
				throw new MPCException("Seems like you gave circuit parser a logic factory with non-native XOR,AND,NOT protocols");
			protocols[pos] = tmp[0];
			pos++; this.pos++;
		}
		//System.out.println("Returning protocols; internal pos: " + this.pos);
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return this.pos < this.parser.getNoOfGates();
	}

	@Override
	public Value[] getInputValues() {
		List<Value> res = new ArrayList<Value>();
		for(SBool b : in1) {
			res.add(b);
		}
		for(SBool b : in2) {
			res.add(b);
		}
		return (Value[])res.toArray();		
	}

	@Override
	public Value[] getOutputValues() {
		return this.out;
	}

}
