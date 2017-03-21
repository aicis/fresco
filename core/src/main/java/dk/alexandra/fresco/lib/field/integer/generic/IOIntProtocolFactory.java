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
package dk.alexandra.fresco.lib.field.integer.generic;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.CloseIntProtocol;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;

/**
 * Factory for protocols that converts between open and closed integers.
 * 
 */
public interface IOIntProtocolFactory {
	
	/**
	 * Gets a new open protocol.
	 * 
	 * @param closed a closed value
	 * @param open a known value
	 * @param targetID the id of the specific player that provides the input.
	 * @return the protocol to do the transformation
	 * 
	 */
	public CloseIntProtocol getCloseProtocol(BigInteger open, SInt closed,
			int targetID);
	
	/**
	 * Gets a new open protocol.
	 * 
	 * @param closed a closed value
	 * @param open a opened value
	 * @param source the id of a specific player that provides the input.
	 * @return the protocol to do the transformation
	 * 
	 */
	CloseIntProtocol getCloseProtocol(int source, OInt open, SInt closed);

	
	/**
	 * Gets a new open protocol that opens up the integer to all.
	 * 
	 * @param closed a closed value
	 * @param open a opened value
	 * @return the protocol to do the transformation
	 * 
	 */
	OpenIntProtocol getOpenProtocol(SInt closed, OInt open);
	
	
	/**
	 * Gets a new open protocol
	 * 
	 * @param closed a closed value
	 * @param open a opened value
	 * @param targetID the id of a specific player to output to.
	 * @return the protocol to do the transformation
	 * 
	 */
	OpenIntProtocol getOpenProtocol(int target, SInt closed, OInt open);
	
}
