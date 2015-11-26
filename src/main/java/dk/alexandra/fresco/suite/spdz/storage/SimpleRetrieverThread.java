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

/**
 * Continually reads one type of preprocessed data using a DataRetriever and queues the data in a DataSupplier.
 * 
 * @author psn
 *
 */
public class SimpleRetrieverThread extends RetrieverThread {

	private DataRetriever retriever;
	private DataSupplier supplier;
	private boolean doRun = true;
	private RetrieverType type;
	
	public SimpleRetrieverThread(DataRetriever retriever, DataSupplier supplier, RetrieverType type){
		this.setName("Retriever Type: " + type);
		this.retriever = retriever;
		this.supplier = supplier;
		this.type = type;
	}
	
	@Override
	public void run(){
		try{
			while(doRun){			
				switch(this.type){
					case TRIPLE:
						SpdzTriple trip = retriever.retrieveTriple();
						supplier.queueTriple(trip);											
						break;
					case BIT:
						SpdzSInt bit = retriever.retrieveBit();
						supplier.queueBit(bit);
						break;
					case EXP:
						SpdzSInt[] exp = retriever.retrieveExpPipe();
						supplier.queueExpPipe(exp);
						break;
					case INPUTME:
						SpdzInputMask mask = retriever.retrieveInputMask(retriever.getpID() + 1);
						supplier.queueMyInput(mask);
						break;
					case INPUTOTHER:
						SpdzInputMask otherMask = retriever.retrieveInputMask(2-retriever.getpID());
						supplier.queueOtherInput(otherMask);
						break;
				}
			}
		} catch(InterruptedException e) {
			doRun = false;
		} catch(Exception e){
			e.printStackTrace();
			doRun = false;
		}
	}
		
	@Override
	public void stopRetrieve(){
		this.interrupt();
	}

	@Override
	public RetrieverType getType() {
		return this.type;
	}
}
