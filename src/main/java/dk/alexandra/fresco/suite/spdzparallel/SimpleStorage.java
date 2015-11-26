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
package dk.alexandra.fresco.suite.spdzparallel;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.storage.RetrieverThread;
import dk.alexandra.fresco.suite.spdz.storage.Storage;
import dk.alexandra.fresco.suite.spdz.storage.d142.NewDataSupplier;

/**
 * A very simple storage implementation.  
 *  
 * @author psn
 *
 */
public class SimpleStorage implements Storage {
	
	private List<BigInteger> opened_values;
	private List<SpdzElement> closed_values;
	private final RetrieverThread[] retrieverThreads;
	private final NewDataSupplier supplier; 
	private final BigInteger ssk; //my share of the shared secret key alpha
	
	public SimpleStorage(NewDataSupplier supplier, RetrieverThread[] threads) {
		this.ssk = supplier.getSSK();
		this.supplier = supplier;
		this.retrieverThreads = threads;
		this.opened_values = new LinkedList<BigInteger>();
		this.closed_values = new LinkedList<SpdzElement>();
	}

	@Override
	public void shutdown() {
		for (RetrieverThread thread : retrieverThreads) {
			thread.stopRetrieve();
		}
		reset();
	}

	@Override
	public void reset() {
		opened_values.clear();
		closed_values.clear();
	}

	@Override
	public NewDataSupplier getSupplier() {
		return supplier;
	}

	@Override
	public void addOpenedValue(BigInteger val) {
		opened_values.add(val);
	}

	@Override
	public void addClosedValue(SpdzElement elem) {
		closed_values.add(elem);
	}

	@Override
	public List<BigInteger> getOpenedValues() {
		return opened_values;
	}

	@Override
	public List<SpdzElement> getClosedValues() {
		return closed_values;
	}

	@Override
	public BigInteger getSSK() {
		return ssk;
	}
}
