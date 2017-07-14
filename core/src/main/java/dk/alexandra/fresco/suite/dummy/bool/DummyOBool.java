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
package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.value.OBool;

/**
 * A public boolean value.
 * 
 * TODO: The O-types should rather be common to all protocol suites,
 * i.e., not DummyOBool, ShamirOBool, etc.
 *
 */
public class DummyOBool implements OBool {

	private static final long serialVersionUID = -4762843635114299987L;
	
	private final String id;
	
	private Boolean value;

	public DummyOBool(String id, boolean b) {
		this.id = id;
		this.value = b;
	}

	public DummyOBool(String id) {
		this.id = id;
		this.value = null;
	}

	@Override
	public byte[] getSerializableContent() {
		byte s;
		if (this.value) { 
			s = 1;
		} else {
			s = 0;
		}
		return new byte[] {s};
	}

	@Override
	public void setSerializableContent(byte[] val) {
		this.value = val[0] == 1;
	}

	@Override
	public boolean isReady() {
		return this.value != null;
	}


	@Override
	public boolean getValue() {
		return this.value;
	}

	@Override
	public void setValue(boolean b) {
		this.value = b;
	}
	
	@Override
	public String toString() {
		return "DummyOBool(" + this.id + "; " + this.value + ")";
	}
	
}
