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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplier;
import dk.alexandra.fresco.suite.spdz.utils.SpdzBenchmarkData;

/**
 * A very simple data supplier that essentially just wraps a Queue for each type of preprocessed data. 
 * @author psn
 *
 */
public class SimpleDataSupplier implements DataSupplier {
	
	private BlockingQueue<SpdzInputMask> concInputsMe;
	//TODO: If this is run in a scenario for more than 2 party, this will fail. Has to redo this.
	private BlockingQueue<SpdzInputMask> concInputsOther;
	private BlockingQueue<SpdzTriple> concTriples;
	private BlockingQueue<SpdzSInt> concBits; 
	private BlockingQueue<SpdzSInt[]> concExpPipes;
	private int pID;
	private BigInteger modulus;
	private BigInteger ssk;

	/**
	 * Constructs a new empty SimpleDataSupplier. One can use a RetrieverThread to periodically fill its Queues
	 * @param conf the configuration to use
	 * @param ssk the share of the key
	 * @param modulus the modulus
	 */
	public SimpleDataSupplier(int myId, BigInteger ssk, BigInteger modulus) {
		this.pID = myId - 1;
		this.concInputsMe = new LinkedBlockingQueue<SpdzInputMask>(50);
		//TODO: If this is run in a scenario for more than 2 party, this will fail. Has to redo this.
		this.concInputsOther = new LinkedBlockingQueue<SpdzInputMask>(50);		
		this.concTriples = new LinkedBlockingQueue<SpdzTriple>(50);
		this.concBits = new LinkedBlockingQueue<SpdzSInt>(100);
		this.concExpPipes = new LinkedBlockingQueue<SpdzSInt[]>(1);
		this.ssk = ssk;
		this.modulus = modulus;
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataSupplier#getNextTriple()
	 */
	@Override
	public SpdzTriple getNextTriple(){
		SpdzTriple triple;
		try {
			triple = this.concTriples.take();
		} catch (InterruptedException e) {
			throw new MPCException("The triples thread was interrupted", e);
		}
		SpdzBenchmarkData.tripleUsed(this.pID+1);
		return triple; 
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataSupplier#getNextExpPipe()
	 */
	@Override
	public SpdzSInt[] getNextExpPipe(){
		SpdzSInt[] expPipe;
		try {
			expPipe = this.concExpPipes.take();
		} catch (InterruptedException e) {
			throw new MPCException("Thread got interrupted!", e);
		}			
		SpdzBenchmarkData.exponentiationPipeUsed(this.pID+1);
		return expPipe;
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataSupplier#getNextInputMask(int)
	 */
	@Override
	public SpdzInputMask getNextInputMask(int towardPlayerID){
		int otherID = towardPlayerID-1;
		SpdzInputMask mask;
		try {
			if(otherID == pID){
				mask = this.concInputsMe.take();
			}else{
				mask = this.concInputsOther.take();
			}
		} catch (InterruptedException e) {
			throw new MPCException("Thread got interrupted!", e);
		}	
		SpdzBenchmarkData.inputUsed(this.pID+1);
		return mask;
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataSupplier#getNextBit()
	 */
	@Override
	public SpdzSInt getNextBit(){
		SpdzSInt bit;
		try {
			bit = this.concBits.take();
		} catch (InterruptedException e) {
			throw new MPCException("Thread got interrupted!", e);
		}	
		SpdzBenchmarkData.bitUsed(this.pID+1);
		return bit;
	}
			
	@Override
	public void queueExpPipe(SpdzSInt[] exp) throws InterruptedException {
		while(!concExpPipes.offer(exp, 30, TimeUnit.MINUTES)) {
			System.out.println("Had to give up queuing exp");
		}
	}

	@Override
	public void queueBit(SpdzSInt bit) throws InterruptedException {
		while(!concBits.offer(bit, 30, TimeUnit.MINUTES)) {
			System.out.println("Had to give up queuing bits");
		}
	}

	@Override
	public void queueOtherInput(SpdzInputMask inputMask) throws InterruptedException {
		while(!concInputsOther.offer(inputMask, 30, TimeUnit.MINUTES)) {
			System.out.println("Had to give up queuing Input for opposing party");
		}
	}

	@Override
	public void queueMyInput(SpdzInputMask inputMask) throws InterruptedException {
		while (!concInputsMe.offer(inputMask, 30, TimeUnit.MINUTES)) {
			System.out.println("Had to give up queuing Input for me");
		}
	}

	@Override
	public void queueTriple(SpdzTriple triple) throws InterruptedException {
		while (!concTriples.offer(triple, 30, TimeUnit.MINUTES)) {
			System.out.println("Had to give up queuing triples");
		}
	}

	@Override
	public BigInteger getModulus() {
		return modulus;
	}

	@Override
	public BigInteger getSSK() {
		return ssk;
	}
}
