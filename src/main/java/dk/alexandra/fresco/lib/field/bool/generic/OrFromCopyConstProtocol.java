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
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SBoolFactory;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.OrProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;

/**
 * An efficient way of OR'ing an SBool with an OBool if we can construct SBools
 * of constants and if we can copy gates.
 * 
 */
public class OrFromCopyConstProtocol implements OrProtocol {

	private CopyProtocol<SBool> copyCir;
	private CopyProtocolFactory<SBool> copyProvider;
	private SBoolFactory sboolProvider;
	private SBool inLeft;
	private OBool inRight;
	private SBool out;
	
	public OrFromCopyConstProtocol(CopyProtocolFactory<SBool> copyProvider, SBoolFactory sboolProvider, SBool inLeft, OBool inRight, SBool out) {
		this.copyProvider = copyProvider;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;
	}
	
	@Override
	public Value[] getInputValues() {
		return new Value[] { inLeft, inRight };
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
	
		if (copyCir == null) {
			if (inRight.getValue()) {
				copyCir = copyProvider.getCopyCircuit(sboolProvider.getKnownConstantSBool(true), out);
			} else {
				copyCir = copyProvider.getCopyCircuit(inLeft, out);
			}
		}
		
		return copyCir.getNextProtocols(gates, pos);
	}

	@Override
	public boolean hasNextProtocols() {
		return (copyCir == null || copyCir.hasNextProtocols());
	}

}
