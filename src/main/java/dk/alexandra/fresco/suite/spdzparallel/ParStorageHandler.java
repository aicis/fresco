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

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.suite.spdz.storage.RetrieverThread;
import dk.alexandra.fresco.suite.spdz.storage.RetrieverThread.RetrieverType;
import dk.alexandra.fresco.suite.spdz.storage.Storage;
import dk.alexandra.fresco.suite.spdz.storage.d142.NewDataSupplier;
import dk.alexandra.fresco.suite.spdz.utils.Util;

/**
 * Handles the storage for the first attempt at a parallelized evaluator
 * 
 * @author psn
 *
 */
public class ParStorageHandler {

	private SimpleDataRetriever retriever;
	private SimpleDataSupplier[] suppliers;
	private RetrieverThread[] retrieverThreads;
	private Storage[] stores;
	private Storage serial;
	
	public ParStorageHandler(NetworkConfiguration conf, int noOfThreads) {
		retriever = new SimpleDataRetriever(conf);
		BigInteger ssk = retriever.getSSK();
		BigInteger mod = retriever.getModulus();
		stores = new Storage[noOfThreads];
		suppliers = new SimpleDataSupplier[noOfThreads];
		for (int i = 0; i < suppliers.length; i++) {
			suppliers[i] = new SimpleDataSupplier(conf.getMyId(), ssk, mod);
		}
		RetrieverType[] types = RetrieverType.values();
		retrieverThreads = new RetrieverThread[types.length];
		for (int i = 0; i < types.length; i++) {
			retrieverThreads[i] = new ParRetrieverThread(retriever, suppliers, types[i]);
		}
		for (int i = 0; i < stores.length; i++) {
			stores[i] = new SimpleStorage(suppliers[i], retrieverThreads);	
		}				
		Util.setModulus(mod);
		byte[] bytes = mod.toByteArray();
		if(bytes[0] == 0){
			Util.size = mod.toByteArray().length-1;		
		}else{
			Util.size = mod.toByteArray().length;
		}	
		
		for (int i = 0; i < types.length; i++) {
			retrieverThreads[i].start();
		}
	}
	
	public Storage getSerialStorage() {
		if (serial == null) {
			serial = new SimpleStorage(getSerialSupplier(), retrieverThreads);
		}
		return serial;
	}
	
	public NewDataSupplier getSerialSupplier() {
		return new SerializingDataSupplier(getSuppliers());
	}
	
	public NewDataSupplier[] getSuppliers() {
		NewDataSupplier[] suppliers = new NewDataSupplier[stores.length];
		for (int i = 0; i < stores.length; i++) {
			suppliers[i] = stores[i].getSupplier();
		}
		return suppliers;
	}
		
	public Storage[] getStores() {
		return stores;
	}
	
	public void shutdown() {
		for (Storage store : stores) {
			store.shutdown();
		}
	}

	public void reset() {
		for (Storage store : stores) {
			store.reset();
		}
	}
}
