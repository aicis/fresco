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
package dk.alexandra.fresco.suite.spdz.utils;

import java.math.BigInteger;


public interface LPInputReader extends InputReader {
	
	/**
	 * Gets the id of the player who is to receive output. An output of 0 is taken to indicate that all 
	 * players should receive output.
	 * @return The id of the output player
	 */
	public int getOutputId();
	
	/**
	 * Gets the coefficients of all constraints
	 * @return the constraints values
	 */
	public BigInteger[][] getConstraintValues();
	
	/**
	 * Gets the input pattern of all constraints
	 * @return the constraints pattern
	 */
	public int[][] getConstraintPattern();
	
	/**
	 * Gets the coefficients of the cost function
	 * @return the cost function values
	 */
	public BigInteger[] getCostValues();
	
	/**
	 * Gets the input pattern of the cost function
	 * @return the cost function input pattern
	 */
	public int[] getCostPattern();
	
	/**
	 * Gets the B-vector values, i.e., the right hand side of the constraints
	 * @return the B-vector values
	 */
	public BigInteger[] getBValues();
	
	/**
	 * Gets the B-vector input pattern
	 * @return the B-vector input pattern
	 */
	public int[] getBPattern();
		
	/**
	 * The F-vector values, i.e., the negated coefficients of the cost function
	 * @return the F-vector values
	 */
	public BigInteger[] getFValues();
	
	/**
	 * The F-vector input pattern, (this is the same as the cost functions input pattern)
	 * @return the F-vector input pattern
	 */
	public int[] getFPattern();
	
	/**
	 * The C matrix values, i.e., the left hand side of the constraints
	 * @return the C matrix values
	 */
	public BigInteger[][] getCValues();
	
	/**
	 * The C matrix input pattern, i.e., the left hand side of the constraints
	 * @return the C matrix input pattern
	 */
	public int[][] getCPattern();
	
}
