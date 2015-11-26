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

import java.util.concurrent.Callable;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

public class ParRetrieverCallable implements Callable<Object> {

	private SimpleDataRetriever retriever;
	private SimpleDataSupplier[] suppliers; 
	private boolean doRun = true;
	private RetrieverType type;
	private int pID;
	private int index;
	
	
	public enum RetrieverType{
		TRIPLE,
		BIT, 
		EXP,
		INPUT
	}
	
	public ParRetrieverCallable(SimpleDataRetriever retriever, SimpleDataSupplier[] suppliers, RetrieverType type) {
		this.suppliers = suppliers;
		this.retriever = retriever;
		this.type = type;
		this.pID = retriever.getpID();
		this.index = 0;
	}
	
	@Override
	public Object call() {
		System.out.println("I'm retrieving " + type + " for " + suppliers.length + " suppliers");
		// TODO: Note the below strategy simply retrieves data for each supplier in a round robin fashion. 
		// This can be a problem if one supplier is not actively emptying its queue, while others are. If one queue is full
		// the thread will block on this queue while the other queues might get emptied, thus the threads drawing 
		// data from the other queues will block until those queues are filled, and we have a deadlock. 
		// I.e. This strategy really only works if all threads can be expected to use about an equal amount of data.
		// This might be made better by marking suppliers as "active"/"inactive" and only filling the active suppliers. 
		try {
			while(doRun){			
				switch(this.type) {
					case TRIPLE:
						SpdzTriple trip = retriever.retrieveTriple();
						suppliers[index % suppliers.length].queueTriple(trip);
						index++;
						if (index/suppliers.length == 2000) {doRun = false;}
						break;
					case BIT:
						SpdzSInt bit = retriever.retrieveBit();
						suppliers[index % suppliers.length].queueBit(bit);
						index++;
						if (index/suppliers.length == 0) {doRun = false;}
						break;
					case EXP:
						SpdzSInt[] exp = retriever.retrieveExpPipe();
						suppliers[index % suppliers.length].queueExpPipe(exp);
						index++;
						if (index/suppliers.length == 0) {doRun = false;}
						break;
					case INPUT:
						SpdzInputMask mask = retriever.retrieveInputMask(pID + 1);
						suppliers[index % suppliers.length].queueMyInput(mask);
						mask = retriever.retrieveInputMask(2-pID);
						suppliers[index % suppliers.length].queueOtherInput(mask);
						index++;
						if (index/suppliers.length == 500) {doRun = false;}
						break;
				}
			}
			System.out.println("I'm DONE retrieving " + type + " for " + suppliers.length + " suppliers");
		}catch(InterruptedException e){
			System.out.println("Interupted! - Setting doRun to false. Shutting down nicely");
			doRun = false;
		}catch(Exception e){
			System.out.println("Thread died due to exception. Shutting down nicely. Exception was:");
			e.printStackTrace();
			doRun = false;
		}
		return null;
	}
}