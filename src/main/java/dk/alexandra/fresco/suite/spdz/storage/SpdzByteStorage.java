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
import java.util.LinkedList;
import java.util.List;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.storage.RetrieverThread.RetrieverType;
import dk.alexandra.fresco.suite.spdz.storage.d142.NewDataSupplier;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import dk.alexandra.fresco.suite.spdzparallel.SimpleDataSupplier;


/**
 * Manages a storage implemented as a set of byte files, including starting the retriever threads
 *
 */
public class SpdzByteStorage implements Storage {
	
	private BigInteger ssk; //my share of the shared secret key alpha
	private List<BigInteger> opened_values;
	private List<SpdzElement> closed_values;
	private SpdzByteDataRetriever retriever;
	private DataSupplier supplier;
	private RetrieverThread[] retrieverThreads;

	public SpdzByteStorage(int myId, int noOfParties, String triplepath){
		ssk = null;
		opened_values = new LinkedList<BigInteger>();
		closed_values = new LinkedList<SpdzElement>();
		
		retriever = new SpdzByteDataRetriever(myId, noOfParties, triplepath);
		BigInteger mod = retriever.getModulus();
		Util.setModulus(mod);
		byte[] bytes = mod.toByteArray();
		if(bytes[0] == 0){
			Util.size = mod.toByteArray().length - 1;		
		}else{
			Util.size = mod.toByteArray().length;
		}		
		supplier = new SimpleDataSupplier(myId, retriever.getSSK(), retriever.getModulus());
		RetrieverType[] types = RetrieverType.values();
		retrieverThreads = new RetrieverThread[types.length];
		for (int i = 0; i < types.length; i++) {
			retrieverThreads[i] = new SimpleRetrieverThread(retriever, supplier, types[i]);
			retrieverThreads[i].start();
		}		
	}
	
	public void stopRetriever(RetrieverType type) {
		for (int i = 0; i < retrieverThreads.length; i++ ) {
			RetrieverThread rt = retrieverThreads[i];
			if (rt != null && rt.getType() == type) {
				rt.stopRetrieve();
			}			
		}
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#shutdown()
	 */
	@Override
	public void shutdown(){
		for (int i = 0; i < retrieverThreads.length; i++) {
			if (retrieverThreads[i] != null) {
				retrieverThreads[i].stopRetrieve();
			}
		}
		retrieverThreads = null;
		retriever.shutdown();
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#reset()
	 */
	@Override
	public void reset() {
		opened_values.clear();
		closed_values.clear();
	}	
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#getSupplier()
	 */
	@Override
	public NewDataSupplier getSupplier(){
		return supplier;
	}
		
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#addOpenedValue(java.math.BigInteger)
	 */
	@Override
	public void addOpenedValue(BigInteger val){
		opened_values.add(val);
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#addClosedValue(dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement)
	 */
	@Override
	public void addClosedValue(SpdzElement elem){
		closed_values.add(elem);
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#getOpenedValues()
	 */
	@Override
	public List<BigInteger> getOpenedValues(){
		return opened_values;
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#getClosedValues()
	 */
	@Override
	public List<SpdzElement> getClosedValues(){
		return closed_values;
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.Storage#getSSK()
	 */
	@Override
	public BigInteger getSSK(){
		if(ssk != null)
			return ssk;
		ssk = retriever.getSSK();
		return ssk;
	}
}