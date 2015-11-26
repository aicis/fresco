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
import java.util.List;
import java.util.Queue;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.d142.NewDataSupplier;

/**
 * A type of supplier used for some early attemps at a parallel evaluator
 * 
 * @author psn
 *
 */
public class SurplusSupplier extends FixedDataSupplier {

	public SurplusSupplier(Queue<SpdzTriple> triples, Queue<SpdzSInt[]> exps,
			Queue<SpdzInputMask>[] inputmasks, Queue<SpdzSInt> bits,
			BigInteger modulus, BigInteger ssk) {
		super(triples, exps, inputmasks, bits, modulus, ssk);
	}

	public void addSurplus(List<DataSupplier> suppliers)
			throws InterruptedException {
		for (NewDataSupplier supplier : suppliers) {
			SpdzTriple triple = supplier.getNextTriple();
			while (triple != null) {
				this.queueTriple(triple);
				triple = supplier.getNextTriple();
			}
			SpdzSInt[] exp = supplier.getNextExpPipe();
			while (exp != null) {
				this.queueExpPipe(exp);
				exp = supplier.getNextExpPipe();
			}
			SpdzInputMask inputMask1 = supplier.getNextInputMask(1);
			while (inputMask1 != null) {
				this.queueInput(inputMask1, 1);
				inputMask1 = supplier.getNextInputMask(1);
			}
			SpdzInputMask inputMask2 = supplier.getNextInputMask(2);
			while (inputMask2 != null) {
				this.queueInput(inputMask2, 2);
				inputMask2 = supplier.getNextInputMask(2);
			}
			SpdzSInt bit = supplier.getNextBit();
			while (bit != null) {
				this.queueBit(bit);
				bit = supplier.getNextBit();
			}
		}
	}
}
