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
package dk.alexandra.fresco.suite.ninja;

import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.SBool;

public class NinjaSBool implements SBool{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8582913017231020209L;
	private byte value;
	
	public NinjaSBool() {
		 value = -1;
	}
	
	@Override
	public byte[] getSerializableContent() {
		return new byte[] {value};
	}

	@Override
	public void setSerializableContent(byte[] val) {
		this.value = val[0];
	}

	@Override
	public boolean isReady() {
		return value != -1;
	}

	public byte getValue() {
		return value;
	}

	public void setValue(byte value) {
		this.value = value;
	}

	public byte xor(NinjaSBool other) {
		return ByteArithmetic.xor(this.value, other.getValue());		
	}

	public byte and(NinjaSBool other) {
		return ByteArithmetic.mult(this.value, other.getValue());
	}

	@Override
	public String toString() {
		return "NinjaSBool [value=" + value + "]";
	}

	public byte not() {
		return ByteArithmetic.not(value);
	}
	
	
	
}
