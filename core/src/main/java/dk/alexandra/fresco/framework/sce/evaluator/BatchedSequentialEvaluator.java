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
package dk.alexandra.fresco.framework.sce.evaluator;

import java.io.IOException;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.SCEResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class BatchedSequentialEvaluator implements ProtocolEvaluator {

	private static final int DEFAULT_THREAD_ID = 0;

	private static final int DEFAULT_CHANNEL = 0;

	private int maxBatchSize;

	private SCEResourcePool resourcePool;
	private ProtocolSuite protocolSuite;

	private SCENetworkImpl sceNetwork;

	public BatchedSequentialEvaluator() {
		this.maxBatchSize = 4096;
	}

	@Override
	public void setResourcePool(SCEResourcePool resourcePool) {
		this.resourcePool = resourcePool;
		this.sceNetwork = new SCENetworkImpl(this.resourcePool.getNoOfParties(), DEFAULT_THREAD_ID);		
	}

	public ProtocolSuite getProtocolInvocation() {
		return protocolSuite;
	}

	@Override
	public void setProtocolInvocation(ProtocolSuite pii) {
		this.protocolSuite = pii;
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	/**
	 * Sets the maximum amount of gates evaluated in each batch.
	 * 
	 * @param maxBatchSize
	 *            the maximum batch size.
	 */
	@Override
	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

	public void eval(ProtocolProducer c) throws IOException {
		do {
			NativeProtocol[] nextProtocols = new NativeProtocol[maxBatchSize];
			int numOfProtocolsInBatch = c.getNextProtocols(nextProtocols, 0);
			BatchedStrategy.processBatch(nextProtocols, numOfProtocolsInBatch, sceNetwork, DEFAULT_CHANNEL,
					resourcePool);
			this.protocolSuite.synchronize(numOfProtocolsInBatch);
		} while (c.hasNextProtocols());

		this.protocolSuite.finishedEval();
	}
}