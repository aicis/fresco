/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
import java.util.LinkedList;
import java.util.List;

public class SpdzStorageDummyImpl implements SpdzStorage{
	
	private List<BigInteger> opened_values;
	private List<SpdzElement> closed_values;
	
	private DataSupplier supplier;
	
	public SpdzStorageDummyImpl(int myId, int numberOfParties) {		
		opened_values = new LinkedList<BigInteger>();
		closed_values = new LinkedList<SpdzElement>();
		
		supplier = new DummyDataSupplierImpl(myId, numberOfParties);
	}
	
	@Override
	public void shutdown() {
		// Does nothing..
	}

	@Override
	public void reset() {
		opened_values.clear();
		closed_values.clear();
	}

	@Override
	public DataSupplier getSupplier() {
		return this.supplier;
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
		return this.supplier.getSSK();
	}

}
