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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

/**
 * Data supplier which supplies the SPDZ protocol suite with preprocessed data.
 * It fetches data from the native storage object within FRESCO and assumes that
 * something else put it there already. See e.g. @NewDataRetriever for a way to
 * do so.
 * 
 * @author Kasper Damgaard
 *
 */
public class DataSupplierImpl implements DataSupplier {

	private StreamedStorage storage;
	private String storageName;

	private int tripleCounter = 0;
	private int expPipeCounter = 0;
	private int[] inputMaskCounters;
	private int bitCounter = 0;

	private BigInteger ssk;
	private BigInteger mod;
	
	/**
	 * Creates a new supplier which takes preprocessed data from the native
	 * storage object of FRESCO.
	 * 
	 * @param storage
	 *            The FRESCO native storage object
	 * @param storageName
	 *            The name of the 'database' we should use (e.g. the full filename).
	 * @param noOfParties
	 *            The number of parties in the computation.
	 */
	public DataSupplierImpl(StreamedStorage storage, String storageName,
			int noOfParties) {
		this.storage = storage;
		this.storageName = storageName;
		this.inputMaskCounters = new int[noOfParties];
	}

	@Override
	public SpdzTriple getNextTriple() {
		SpdzTriple trip = this.storage.getNext(storageName+
				SpdzStorageConstants.TRIPLE_STORAGE);
		if(trip == null) {
			throw new MPCException("Triple no. "+tripleCounter+" was not present in the storage "+ storageName);
		}
		tripleCounter ++;
		return trip;
	}

	@Override
	public SpdzSInt[] getNextExpPipe() {
		SpdzSInt[] expPipe = this.storage.getNext(storageName+SpdzStorageConstants.EXP_PIPE_STORAGE);
		if(expPipe == null) {
			throw new MPCException("expPipe no. "+expPipeCounter+" was not present in the storage" + storageName);
		}
		expPipeCounter ++;
		return expPipe;
	}

	@Override
	public SpdzInputMask getNextInputMask(int towardPlayerID) {
		SpdzInputMask mask = this.storage.getNext(storageName +
				SpdzStorageConstants.INPUT_STORAGE + towardPlayerID);
		inputMaskCounters[towardPlayerID-1]++;
		if(mask == null) {
			throw new MPCException("Mask no. "+inputMaskCounters[towardPlayerID-1]+" towards player "+towardPlayerID+" was not present in the storage " + storageName);
		}
		return mask;
	}

	@Override
	public SpdzSInt getNextBit() {
		SpdzSInt bit = this.storage.getNext(storageName + 
				SpdzStorageConstants.BIT_STORAGE);
		if(bit == null) {
			throw new MPCException("Bit no. "+bitCounter+" was not present in the storage "+ storageName);
		}
		bitCounter++;
		return bit;
	}

	@Override
	public BigInteger getModulus() {
		if(this.mod != null) {
			return this.mod;
		}
		this.mod = this.storage.getNext(storageName +
				SpdzStorageConstants.MODULUS_KEY);
		if(this.mod == null) {
			throw new MPCException("Modulus was not present in the storage "+ storageName);
		}
		return this.mod;
	}

	@Override
	public BigInteger getSSK() {
		if(this.ssk != null) {
			return this.ssk;
		}
		this.ssk = this.storage.getNext(storageName+
				SpdzStorageConstants.SSK_KEY);
		if(this.ssk == null) {
			throw new MPCException("SSK was not present in the storage "+ storageName);
		}
		return this.ssk;
	}
}
