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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * The LPPrefix models that the SInts used as input to the LPSolver should be prepared in some way. 
 * E.g. they may be the result of some prior secure computation. An LPPrefix thus both 
 * supplies the SInts for the LPSolver and a ProtocolProducer representing a protocol to be evaluated in order
 * to prepare the SInts for use in the LPSolver.
 * @author psn
 *
 */
public interface LPPrefix {

	/**
	 * Gives a ProtocolProducer preparing inputs to an LPSolver. 
	 * @param tableau output - the tableau as it should be prepared before the first iteration 
	 * @param lpfactory input - an LP factory
	 * @param numericfactory input - an LP factory
	 * @return a ProtocolProducer representing the modification of inputs 
	 */
	public ProtocolProducer getPrefix();
	
	/**
	 * @return the SInts that after running the prefix protocol should hold the initial tableau
	 */
	public LPTableau getTableau();
	
	/**
	 * @return the SInts that after running the prefix protocol should hold the initial update matrix
	 */
	public Matrix<SInt> getUpdateMatrix();
	
	
	/**
	 * @return the SInt that after running the prefix protocol should hold the initial pivot
	 */
	public SInt getPivot();
}
