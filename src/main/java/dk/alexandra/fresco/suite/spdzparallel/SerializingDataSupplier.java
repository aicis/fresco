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

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.d142.NewDataSupplier;

public class SerializingDataSupplier implements DataSupplier {
	
	private NewDataSupplier[] suppliers;
	private int tripIndex;
	private int expIndex;
	private int inputIndex;
	private int bitIndex;
	
	public SerializingDataSupplier(NewDataSupplier[] suppliers) {
		this.suppliers = suppliers;
		this.tripIndex = 0;
		this.inputIndex = 0;
		this.bitIndex = 0;
	}

	@Override
	public SpdzTriple getNextTriple() {
		SpdzTriple trip = suppliers[tripIndex].getNextTriple();
		tripIndex = (tripIndex + 1) % suppliers.length;
		return trip;
	}

	@Override
	public SpdzSInt[] getNextExpPipe() {
		SpdzSInt[] exp = suppliers[expIndex].getNextExpPipe();
		expIndex = (expIndex + 1) % suppliers.length;
		return exp;
	}

	@Override
	public SpdzInputMask getNextInputMask(int towardPlayerID) {
		SpdzInputMask mask = suppliers[inputIndex].getNextInputMask(towardPlayerID);
		inputIndex = (inputIndex + 1) % suppliers.length;
		return mask;
	}

	@Override
	public SpdzSInt getNextBit() {
		SpdzSInt bit = suppliers[bitIndex].getNextBit();
		bitIndex = (bitIndex + 1) % suppliers.length;
		return bit;
	}

	@Override
	public BigInteger getModulus() {
		return suppliers[0].getModulus();
	}

	@Override
	public BigInteger getSSK() {
		return suppliers[0].getSSK();
	}

	@Override
	public void queueExpPipe(SpdzSInt[] exp) throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueBit(SpdzSInt bit) throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueOtherInput(SpdzInputMask inputMask)
			throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueMyInput(SpdzInputMask inputMask)
			throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueTriple(SpdzTriple triple) throws InterruptedException {
		// TODO Auto-generated method stub

	}

}
