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
package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.value.SBool;

public class PreproNinjaSBool implements SBool{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7049163907480239087L;

	//Id of wire
	private int id;
	
	//value of the random value assigned to the wire
	private byte r;
		
	public PreproNinjaSBool(int id, byte r) {
		this.id = id;
		this.r = r;
	}

	@Override
	public byte[] getSerializableContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSerializableContent(byte[] val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isReady() {
		return this.id != -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte getR() {
		return r;
	}

	public void setR(byte r) {
		this.r = r;
	}

	@Override
	public String toString() {
		return "PreproNinjaSBool [id=" + id + ", r=" + r + "]";
	}
}
