/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.tinytables.prepro;

import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproOBool;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproCloseProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproNOTProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproOpenToAllProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproXORProtocol;

public class TinyTablesPreproFactory extends AbstractBinaryFactory implements BasicLogicFactory {

	private int counter;
	
	public TinyTablesPreproFactory() {
		this.counter = 0;
	}
	
	private int getNextId() {
		return counter++;
	}
	
	@Override
	public CloseBoolProtocol getCloseProtocol(int source, OBool open, SBool closed) {
		return new TinyTablesPreproCloseProtocol(getNextId(), source, open, closed);
	}

	@Override
	public OpenBoolProtocol getOpenProtocol(SBool closed, OBool open) {
		return new TinyTablesPreproOpenToAllProtocol(getNextId(), (TinyTablesPreproSBool)closed, (TinyTablesPreproOBool)open);
	}

	@Override
	public OpenBoolProtocol getOpenProtocol(int target, SBool closed, OBool open) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool getSBool() {
		return new TinyTablesPreproSBool();
	}

	@Override
	public SBool[] getSBools(int amount) {
		SBool[] res = new SBool[amount];
		for(int i = 0; i < amount; i++) {
			res [i] = this.getSBool();
		}
		return res;
	}

	@Override
	public SBool getKnownConstantSBool(boolean b) {
		return new TinyTablesPreproSBool(new TinyTablesElement(false)); // Ignore the value and use trivial mask
	}

	@Override
	public SBool[] getKnownConstantSBools(boolean[] bools) {
		SBool[] tinyTableSBools = new SBool[bools.length];
		for (int i = 0; i < tinyTableSBools.length; i++) {
			tinyTableSBools[i] = getKnownConstantSBool(bools[i]);
		}
		return tinyTableSBools;
	}

	@Override
	public OBool getOBool() {
		return new TinyTablesPreproOBool();
	}

	@Override
	public OBool getKnownConstantOBool(boolean b) {
		return getOBool(); // The assigned value to OBools does not make sense here
	}

	@Override
	public AndProtocol getAndProtocol(SBool inLeft, SBool inRight, SBool out) {
		return new TinyTablesPreproANDProtocol(getNextId(), (TinyTablesPreproSBool)inLeft, (TinyTablesPreproSBool)inRight, (TinyTablesPreproSBool)out);
	}

	@Override
	public AndProtocol getAndProtocol(SBool inLeft, OBool inRight, SBool out) {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public NotProtocol getNotProtocol(SBool in, SBool out) {
		return new TinyTablesPreproNOTProtocol(getNextId(), (TinyTablesPreproSBool)in, (TinyTablesPreproSBool)out);
	}

	@Override
	public XorProtocol getXorProtocol(SBool inLeft, SBool inRight, SBool out) {
		return new TinyTablesPreproXORProtocol(getNextId(), (TinyTablesPreproSBool)inLeft, (TinyTablesPreproSBool)inRight, (TinyTablesPreproSBool)out);
	}

	@Override
	public XorProtocol getXorProtocol(SBool inLeft, OBool inRight, SBool out) {
		throw new RuntimeException("Not implemented yet");
	}

}
