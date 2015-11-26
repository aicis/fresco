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
package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

/**
 * DataRetriever's retrieves various forms of preprocessed data used in the SPDZ online phase, from some underlying source.
 * For example data is usually retrieved from files on disk.
 * 
 * @author psn
 *
 */
public interface DataRetriever {

	/**
	 * Retrieves a spdz triple.
	 * @param a newly retrieved triple
	 */
	public abstract SpdzTriple retrieveTriple();

	/**
	 * Retrieves a preprocessed exp pipe i.e. a series of SInt's representing the numbers 
	 * R^-1, R, R^2, R^3, ..., R^200, for some random R.
	 * @return an array of the numbers described above
	 */
	public abstract SpdzSInt[] retrieveExpPipe();

	/**
	 * Retreives an input mask for input given by a specified player, not this id indexes from 1 (not 0)
	 * @param towardPlayerID the id of the played to to give input using the returned mask
	 * @return a newly retrieved input mask
	 */
	public abstract SpdzInputMask retrieveInputMask(int towardPlayerID);

	/**
	 * Retrieves an SInt representing a random bit value, i.e., an SInt representing the number 0 or 1
	 * @return an SInt representing a bit
	 */
	public abstract SpdzSInt retrieveBit();
	
	
	/**
	 * The party id of the party using this retriever minus one, i.e., indexing players from 0. 
	 * @return a party id
	 */
	public abstract int getpID();
}