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
package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.MPCException;

public class GenericOBool implements OBool {

	/**
	 * 
	 */
	private static final long serialVersionUID = 901034067486901690L;
	protected Boolean value = null;
	
	public GenericOBool() {}
	
	public GenericOBool(boolean value) {
		this.value = value;
	}
	
	// TODO: is there any good reason to use Boolean instead of boolean here?
	public void setValue(boolean value) {
		this.value = value;
	}
	
	@Override
	public boolean getValue() {
		return value.booleanValue();
	}

	@Override
	public boolean isReady() {
		return !(value==null);
	}

	@Override
	public byte[] getSerializableContent() {
		return value == true ? new byte[] {1} : new byte[] {0};
	}

	@Override
	public void setSerializableContent(byte[] val) {
		if(val.length != 1) {
			throw new MPCException("Cannot set a boolean value from a byte array of length "+val.length);
		}
		if(val[0] == (byte)0) {
			this.value = false;
		} else if(val[0] == (byte)1){
			this.value = true;
		} else {
			throw new MPCException("Cannot set a boolean value from a byte array containing "+val[0]);
		}
	}

}
