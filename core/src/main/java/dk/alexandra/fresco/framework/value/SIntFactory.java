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
package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;


public interface SIntFactory {	
	
	/**
	 * Creates an empty container.
	 * 
	 */
	public SInt getSInt();
	
	
	@Deprecated
	public SInt getSInt(int i);
	
	@Deprecated
	public SInt getSInt(BigInteger i);

	/**
	 * A protocol to load a publicly known value into a SInt
	 * TODO: This should not be how values are loaded
	 * 
	 */
	public KnownSIntProtocol getSInt(int i, SInt si);
	
	/**
	 * Gets a protocol to load a publicly known value into a SInt. 
	 * The idea here is to not do the computation involved in loading the SInt 
	 * while we are building the protocol.
	 * TODO: This should not be how values are loaded
	 * 
	 */
	public KnownSIntProtocol getSInt(BigInteger i, SInt si);	
	
}
