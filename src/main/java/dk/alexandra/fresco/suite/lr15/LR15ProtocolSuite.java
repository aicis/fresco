/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.lr15;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;


/**
 * A protocol suite based on Yao's garbled circuit approach.
 * 
 * This implementation uses the Yao stuff in SCAPI.
 * 
 * No streaming (as streaming is not supported by SCAPI yao).
 * 
 * This is an example of a suite with evaluational dependence (?).
 * 
 * We simply build the SCAPI circuit data structure (BooleanCircuit) in its entirety
 * in memory during FRESCO eval.
 * 
 * TODO: Perhaps garbling is done during FRESCO eval?
 * 
 * Then, at finishedEval() we launch the SCAPI protocol.
 * 
 * 
 */
public class LR15ProtocolSuite implements ProtocolSuite {

	@Override
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RoundSynchronization createRoundSynchronization() {
		return new DummyRoundSynchronization();
	}

	@Override
	public void finishedEval(ResourcePool resourcePool, SCENetwork sceNetwork) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	

}
