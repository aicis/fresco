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
package dk.alexandra.fresco.lib.helper.sequential;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * This implementation is lazy in the sense that it only invokes hasMoreGates()
 * and getNextGates() on a circuit when needed.
 * 
 **/
class SequentialHelper implements ProtocolProducer {

	private GateProducerList producerList;
	private ProtocolProducer currentProducer = null;
	
	protected SequentialHelper(GateProducerList cl) {
		this.producerList = cl;
	}
		
	/*
	 * If prune returns false, we are done and no more gates can be produced.
	 * If prune returns true, currentCircuit is a circuit with gates to evaluate.
	 */
	private boolean prune() {
		// Bootstrapping (for when currentCircuit is initially null) 
		if (currentProducer == null) {
			if (producerList.hasNextInLine()) {
				currentProducer = producerList.getNextInLine();
			} else {
				return false;
			}
		}
		// Roll until a circuit has gates or the end is reached
		while (!currentProducer.hasNextProtocols()) {
			if (producerList.hasNextInLine()) {
				currentProducer = producerList.getNextInLine();
			} else {
				/* NOTE: we could set currentCircuit to null here 
				 * to release it to the GC - it would not break the method 
				 */
				currentProducer = null;
				return false;
			}
		}		
		return true;
	}
	
	@Override
	public boolean hasNextProtocols() {
		return prune();
	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (prune()) 
			return currentProducer.getNextProtocols(gates, pos);
		else
			return pos;
	}
}
