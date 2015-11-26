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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.suite.spdz.storage.Storage;
import dk.alexandra.fresco.suite.spdz.storage.d142.NewDataSupplier;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import dk.alexandra.fresco.suite.spdzparallel.ParRetrieverCallable.RetrieverType;


/**
 * 
 * An experimental storage handler used for the first attempt at parrallelized evaluation.
 * 
 * @author psn
 *
 */
public class ExpParStorageHandler {
	
	private SimpleDataRetriever retriever;
	private Storage[] stores;
	private SimpleDataSupplier[] suppliers;
	private ExecutorService executor;
	private Future<Object>[] futures;

	public ExpParStorageHandler(NetworkConfiguration conf, int noOfThreads) {
		retriever = new SimpleDataRetriever(conf);
		BigInteger ssk = retriever.getSSK();
		BigInteger mod = retriever.getModulus();
		stores = new Storage[noOfThreads];
		suppliers = new SimpleDataSupplier[noOfThreads];
		for (int i = 0; i < suppliers.length; i++) {
			suppliers[i] = new SimpleDataSupplier(conf.getMyId(), ssk, mod);
		}
		RetrieverType[] types = RetrieverType.values();
		List<ParRetrieverCallable> retrievers = new LinkedList<ParRetrieverCallable>();
		for (int i = 0; i < types.length; i++) {
			retrievers.add(new ParRetrieverCallable(retriever, suppliers, types[i]));
		}
		for (int i = 0; i < stores.length; i++) {
			stores[i] = new SimpleStorage(suppliers[i], null);	
		}				
		Util.setModulus(mod);
		byte[] bytes = mod.toByteArray();
		if(bytes[0] == 0){
			Util.size = mod.toByteArray().length-1;		
		}else{
			Util.size = mod.toByteArray().length;
		}
		executor = Executors.newFixedThreadPool(types.length);
		try {
			executor.invokeAll(retrievers);
		} catch (InterruptedException e) {
			throw new MPCException(e.getMessage(), e);
		}
	}
	
	public void refill() {
		RetrieverType[] types = RetrieverType.values();
		if (futures == null) {
			futures = new Future[types.length];
			for (int i = 0; i < types.length; i++) {				
				futures[i] = executor.submit(new ParRetrieverCallable(retriever, suppliers, types[i]));
			}			
		} else {
			for (int i = 0; i < types.length; i++) {
				if (futures[i].isDone()) {
					futures[i] = executor.submit(new ParRetrieverCallable(retriever, suppliers, types[i]));
				}
			}
		}
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
