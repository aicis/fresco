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
import java.util.Queue;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplier;

/**
 * Supplies data from a set of fixed data queues. I.e. the queues are given a the time the Storage is constructed 
 * and should not be expected to be refilled.
 *  
 * @author psn
 *
 */
public class FixedDataSupplier implements DataSupplier {
	
	private Queue<SpdzTriple> triples;
	private Queue<SpdzSInt[]> exps;
	private Queue<SpdzInputMask>[] inputmasks;
	private Queue<SpdzSInt> bits;
	private BigInteger modulus;
	private BigInteger ssk;
	
	public FixedDataSupplier(
			Queue<SpdzTriple> triples, 
			Queue<SpdzSInt[]> exps,
			Queue<SpdzInputMask>[] inputmasks,
			Queue<SpdzSInt> bits,
			BigInteger modulus,
			BigInteger ssk) {
		this.triples = triples;
		this.exps = exps;
		this.inputmasks = inputmasks;
		this.bits = bits; 
		this.modulus = modulus;
		this.ssk = ssk;
	}

	@Override
	public SpdzTriple getNextTriple() {
		return triples.poll();
	}

	@Override
	public SpdzSInt[] getNextExpPipe() {
		return exps.poll();
	}

	@Override
	public SpdzInputMask getNextInputMask(int towardPlayerID) {
		return inputmasks[towardPlayerID  - 1].poll();
	}

	@Override
	public SpdzSInt getNextBit() {
		return bits.poll();
	}

	@Override
	public BigInteger getModulus() {
		return modulus;
	}

	@Override
	public BigInteger getSSK() {
		return ssk;
	}

	@Override
	public void queueExpPipe(SpdzSInt[] exp) throws InterruptedException {
		this.exps.add(exp);
	}

	@Override
	public void queueBit(SpdzSInt bit) throws InterruptedException {
		this.bits.add(bit);
	}

	public void queueInput(SpdzInputMask inputMask, int id)
			throws InterruptedException {
		this.inputmasks[id - 1].add(inputMask);
	}

	@Override
	public void queueTriple(SpdzTriple triple) throws InterruptedException {
		this.triples.add(triple);
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

}
