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

import java.math.BigInteger;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

public interface DataSupplier {

	/**
	 * Supplies the next triple
	 * @return the next new triple
	 */
	public abstract SpdzTriple getNextTriple();

	/**
	 * Supplies the next exp pipe
	 * @return the next new exp pipe 
	 */
	public abstract SpdzSInt[] getNextExpPipe();

	/**
	 * Supplies the next inputmask for a given input player
	 * @param towardPlayerID the id of the input player
	 * @return the appropriate input mask
	 */
	public abstract SpdzInputMask getNextInputMask(int towardPlayerID);

	/**
	 * Supplies the next bit (i.e. a SpdzSInt representing a value in {0, 1})
	 * @return the next new bit
	 */
	public abstract SpdzSInt getNextBit();

	/**
	 * The modulus used for this instance of SPDZ
	 * @return a modulus
	 */
	public abstract BigInteger getModulus();

	/**
	 * Returns the Players share of the Shared Secret Key (alpha).
	 * This is never to be send to anyone else!
	 * @return a share of the key
	 */
	public abstract BigInteger getSSK();

	/**
	 * Returns the next random field element
	 * @return A SpdzSInt representing a random secret shared field element.
	 */
	public abstract SpdzSInt getNextRandomFieldElement();

	/**
	 * Kills any resources/threads used.
	 */
	public abstract void shutdown();

}