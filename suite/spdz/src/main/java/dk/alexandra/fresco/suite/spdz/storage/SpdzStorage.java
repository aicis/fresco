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

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import java.math.BigInteger;
import java.util.List;


/**
 * Manages the storage associated with the online phase of SPDZ. This includes all the preprocessed data and the opened and 
 * closed accumulated during the online phase
 *
 */
public interface SpdzStorage{

	/**
	 * Attempts to shutdown the storage nicely
	 */
	public abstract void shutdown();

	/**
	 * Resets the opened and closed values
	 */
	public abstract void reset();

	/**
	 * Gets a data supplier suppling preprocessed data values
	 * @return a data supplier
	 */
	public abstract DataSupplier getSupplier();

	/**
	 * Adds an opened value
	 * @param val a value to be added
	 */
	public abstract void addOpenedValue(BigInteger val);

	/**
	 * Adds a closed values
	 * @param elem a element to add
	 */
	public abstract void addClosedValue(SpdzElement elem);

	/**
	 * Get the current opened values
	 * @return a list of opened values
	 */
	public abstract List<BigInteger> getOpenedValues();

	/**
	 * Get the current closed values
	 * @return a list of closed values
	 */
	public abstract List<SpdzElement> getClosedValues();

	/**
	 * Returns the players share of the Secret Shared Key (alpha). 
	 * @return alpha_i
	 */
	public abstract BigInteger getSSK();

}