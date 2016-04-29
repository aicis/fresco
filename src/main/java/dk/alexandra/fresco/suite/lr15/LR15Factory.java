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
package dk.alexandra.fresco.suite.lr15;

import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;

public class LR15Factory implements BasicLogicFactory {

	@Override
	public CloseBoolProtocol getCloseProtocol(int source, OBool open, SBool closed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpenBoolProtocol getOpenProtocol(SBool closed, OBool open) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpenBoolProtocol getOpenProtocol(int target, SBool closed, OBool open) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool getSBool() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool[] getSBools(int amount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool getKnownConstantSBool(boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool[] getKnownConstantSBools(boolean[] bools) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OBool getOBool() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OBool getKnownConstantOBool(boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AndProtocol getAndProtocol(SBool inLeft, SBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AndProtocol getAndProtocol(SBool inLeft, OBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NotProtocol getNotProtocol(SBool in, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XorProtocol getXorProtocol(SBool inLeft, SBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XorProtocol getXorProtocol(SBool inLeft, OBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}


}
