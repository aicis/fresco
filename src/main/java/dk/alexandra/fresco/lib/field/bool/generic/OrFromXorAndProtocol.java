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
package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SBoolFactory;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocolFactory;
import dk.alexandra.fresco.lib.field.bool.OrProtocol;
import dk.alexandra.fresco.lib.field.bool.XorProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * This circuit implements
 * 
 *     a OR b
 *   
 * as
 * 
 *     (a AND b) XOR a XOR b
 *
 */
public class OrFromXorAndProtocol implements OrProtocol {

	private SBoolFactory sboolp;
	private XorProtocolFactory xorcp;
	private AndProtocolFactory andcp;
	private SBool inA;
	private SBool inB;
	private SBool out;
	
	private ProtocolProducer c;
	private SBool t0;
	private SBool t1;

	public OrFromXorAndProtocol(SBoolFactory sboolp, XorProtocolFactory xorcp, AndProtocolFactory andcp, SBool inA, SBool inB, SBool out) {
		this.sboolp = sboolp;
		this.xorcp = xorcp;
		this.andcp = andcp;
		this.inA = inA;
		this.inB = inB;
		this.out = out;
	}

	@Override
	public boolean hasNextProtocols() {
		return c == null || c.hasNextProtocols();
	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		// TODO: We could create less objects up front than this by using explicit program counter.
		
		// Lazy construction of inner circuits.
		if (c == null) {
			t0 = sboolp.getSBool();
			t1 = sboolp.getSBool();
			Protocol c0 = andcp.getAndCircuit(inA, inB, t0);
			Protocol c1 = xorcp.getXorProtocol(inA, inB, t1);
			ProtocolProducer c2 = new ParallelProtocolProducer(c0, c1); // Get ParallelGateProducer in constructor?
			Protocol c3 = xorcp.getXorProtocol(t0, t1, out);
			c = new SequentialProtocolProducer(c2, c3); // Get SequentialGateProducer in constructor?
		}

		return c.getNextProtocols(gates, pos);
		
		// TODO: Should we nullify c here if no more gates?

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
