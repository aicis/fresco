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
package dk.alexandra.fresco.suite.dummy;

import dk.alexandra.fresco.framework.value.SBool;


public class DummySBool implements SBool {

	private static final long serialVersionUID = -4614951451129474002L;
	
	private final String id;
	
	private Boolean value;
	
	public DummySBool(String id) {
		this.value = null;
		this.id = id;
	}
	
	public DummySBool(String id, boolean b) {
		this.value = b;
		this.id = id;
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

	public boolean getValue() {
		return this.value;
	}

	public void setValue(boolean val) {
		this.value = val;
	}
	

	@Override
	public String toString() {
		if (this.value != null) {
			return "DummySBool(" + this.id + "; " + this.value + ")";
		} else {
			return "DummySBool(" + this.id + "; null)";
		}
	}
	
	
}
