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

public interface SBoolFactory {

	/**
	 * Creates an empty container.
	 * 
	 */
	public SBool getSBool();

	/**
	 * Creates 'amount' of SBools.
	 * 
	 * @amount the amount of SBools you want back
	 * @return
	 */
	public SBool[] getSBools(int amount);

	/**
	 * Creates a container with a preloaded value.
	 * 
	 * This may be implemented by simply selecting between two fixed and known
	 * SBools and hence should NOT be used to load secret values since the value
	 * may become known to the other players.
	 * 
	 */
	public SBool getKnownConstantSBool(boolean b);

	/**
	 * Generates an array of SBools the size of the input array containing the
	 * representations given in the array bools.
	 * 
	 * @param bools
	 * @return
	 */
	public SBool[] getKnownConstantSBools(boolean[] bools);
	
	
}
