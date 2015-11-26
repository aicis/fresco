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
package dk.alexandra.fresco.suite.spdz.storage.d142;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
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
public class NewDataSupplierImpl implements NewDataSupplier {

	private Storage storage;
	private String storageName;

	private int tripleCounter;
	private int expPipeCounter;
	private int[] inputMaskCounters;
	private int bitCounter;

	private int deltaJump;

	/**
	 * Creates a new supplier which takes preprocessed data from the native
	 * storage object of FRESCO.
	 * 
	 * @param storage
	 *            The FRESCO native storage object
	 * @param storageName
	 *            The name of the 'database' we should use.
	 * @param storageId
	 *            The id of the thread that will use this supplier
	 * @param NoOfThreadsUsed
	 *            Number of threads used. This will be the delta which we
	 *            increase the counters with. e.g. after reading triple no. 2,
	 *            we increase the counter to 2+noOfThreadsUsed.
	 * @param noOfParties
	 *            The number of parties in the computation.
	 */
	public NewDataSupplierImpl(Storage storage, String storageName,
			int storageId, int NoOfThreadsUsed, int noOfParties) {
		this.storage = storage;
		this.storageName = storageName;
		this.deltaJump = NoOfThreadsUsed;
		this.inputMaskCounters = new int[noOfParties];

		initCounters(storageId);
	}

	private void initCounters(int storageId) {
		tripleCounter = storageId;
		expPipeCounter = storageId;
		for (int i = 0; i < inputMaskCounters.length; i++) {
			inputMaskCounters[i] = storageId;
		}
		bitCounter = storageId;
	}

	@Override
	public SpdzTriple getNextTriple() {
		SpdzTriple trip = this.storage.getObject(storageName,
				NewSpdzStorageConstants.TRIPLE_KEY_PREFIX + tripleCounter);
		if(trip == null) {
			throw new MPCException("Triple no. "+tripleCounter+" was not present in the storage");
		}
		tripleCounter += deltaJump;
		return trip;
	}

	@Override
	public SpdzSInt[] getNextExpPipe() {
		SpdzSInt[] expPipe = this.storage.getObject(storageName,
				NewSpdzStorageConstants.EXP_PIPE_KEY_PREFIX + expPipeCounter);
		if(expPipe == null) {
			throw new MPCException("expPipe no. "+expPipeCounter+" was not present in the storage");
		}
		expPipeCounter += deltaJump;
		return expPipe;
	}

	@Override
	public SpdzInputMask getNextInputMask(int towardPlayerID) {
		SpdzInputMask mask = this.storage.getObject(storageName,
				NewSpdzStorageConstants.INPUT_KEY_PREFIX + towardPlayerID + "_"
						+ inputMaskCounters[towardPlayerID-1]);
		inputMaskCounters[towardPlayerID-1] += deltaJump;
		if(mask == null) {
			throw new MPCException("Mask no. "+inputMaskCounters[towardPlayerID-1]+" towards player "+towardPlayerID+" was not present in the storage");
		}
		return mask;
	}

	@Override
	public SpdzSInt getNextBit() {
		SpdzSInt bit = this.storage.getObject(storageName,
				NewSpdzStorageConstants.BIT_KEY_PREFIX + bitCounter);
		if(bit == null) {
			throw new MPCException("Bit no. "+bitCounter+" was not present in the storage");
		}
		bitCounter += deltaJump;
		return bit;
	}

	@Override
	public BigInteger getModulus() {
		BigInteger mod = this.storage.getObject(storageName,
				NewSpdzStorageConstants.MODULUS_KEY);
		if(mod == null) {
			throw new MPCException("Modulus was not present in the storage");
		}
		return mod;
	}

	@Override
	public BigInteger getSSK() {
		BigInteger ssk = this.storage.getObject(storageName,
				NewSpdzStorageConstants.SSK_KEY);
		if(ssk == null) {
			throw new MPCException("SSK was not present in the storage");
		}
		return ssk;
	}
}
