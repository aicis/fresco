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
import dk.alexandra.fresco.suite.spdz.storage.d142.NewDataSupplier;

/**
 * Supplies preprocessed data to the online phase system. Note while Datasuppliers have a similar role to DataRetrievers, 
 * the idea here is decouple the way we retrieve the data from whatever source (most probably from disk) and the way we serve
 * the data to the online system.
 * 
 * Datasuppliers also provide methods to queue up data to be supplied later. This is usefull when working with RetrieverThreads.
 *   
 * @author psn
 */
public interface DataSupplier extends NewDataSupplier {

	/**
	 * Queues up an exp pipe to be supplied later
	 * @param exp an exp pipe
	 * @throws InterruptedException
	 */
	public abstract void queueExpPipe(SpdzSInt[] exp)
			throws InterruptedException;

	/**
	 * Queues up a bit to be supplied later
	 * @param bit a bit
	 * @throws InterruptedException
	 */
	public abstract void queueBit(SpdzSInt bit) throws InterruptedException;

	/**
	 * Queues up an input mask for the opposing player to be supplied later
	 * @param inputMask an input mask
	 * @throws InterruptedException
	 */
	public abstract void queueOtherInput(SpdzInputMask inputMask)
			throws InterruptedException;

	/**
	 * Queues up an input mask for this player to be supplied later
	 * @param inputMask an input mask
	 * @throws InterruptedException
	 */
	public abstract void queueMyInput(SpdzInputMask inputMask)
			throws InterruptedException;

	/**
	 * Queues up a triple to be supplied later
	 * @param triple a triple
	 * @throws InterruptedException
	 */
	public abstract void queueTriple(SpdzTriple triple)
			throws InterruptedException;
}