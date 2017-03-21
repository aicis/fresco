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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.SCEResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;

/**
 * Generic Evaluator for doing simple gate-by-gate (or protocol by protocol in
 * Practice terms). It is super sequential since each gate is evaluated
 * completely (including network communication).
 * 
 * @author Kasper Damgaard
 *
 */
public class SequentialEvaluator implements ProtocolEvaluator {

	private static final int DEFAULT_THREAD_ID = 0;

	private static final int DEFAULT_CHANNEL = 0;

	/**
	 * Quit if more than this amount of empty batches are returned in a row from
	 * the protocol producer.
	 * 
	 * This is just to avoid an infinite loop if there is an error in the
	 * protocol producer.
	 * 
	 */
	private static final int MAX_EMPTY_BATCHES_IN_A_ROW = 10;

	private int maxBatchSize;

	private SCEResourcePool resourcePool;
	private ProtocolSuite protocolSuite;
	private Network network;
	
	public SequentialEvaluator() {
		maxBatchSize = 4096;
	}

	@Override
	public void setResourcePool(SCEResourcePool resourcePool) {
		this.resourcePool = resourcePool;
		this.network = resourcePool.getNetwork();
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
	
	
	private int doOneRound(ProtocolProducer c) throws IOException {
		NativeProtocol[] nextProtocols = new NativeProtocol[maxBatchSize];
		int numOfProtocolsInBatch = c.getNextProtocols(nextProtocols, 0);
		processBatch(nextProtocols, numOfProtocolsInBatch);
		this.protocolSuite.synchronize(numOfProtocolsInBatch);
		return numOfProtocolsInBatch;
	}
	
	public void eval(ProtocolProducer c) throws IOException {
		int batch = 0;
		int totalProtocols = 0;
		int totalBatches = 0;
		int zeroBatches = 0;
		do {
			int numOfProtocolsInBatch = doOneRound(c);
			Reporter.finest("Done evaluating batch: " + batch++ + " with " + numOfProtocolsInBatch + " native protocols");
			if (numOfProtocolsInBatch == 0) {
				Reporter.finest("Batch " + batch + " is empty");
			}
			totalProtocols += numOfProtocolsInBatch;
			totalBatches += 1;
			if (numOfProtocolsInBatch == 0) {
				zeroBatches++;
			} else {
				zeroBatches = 0;
			}
			if (zeroBatches > MAX_EMPTY_BATCHES_IN_A_ROW) {
				throw new MPCException(
						"Number of empty batches in a row reached " + MAX_EMPTY_BATCHES_IN_A_ROW + "; probably there is a bug in your protocol producer.");
			}
		} while (c.hasNextProtocols());
		this.protocolSuite.finishedEval();
		Reporter.fine("Sequential evaluator done. Evaluated a total of " + totalProtocols + " native protocols in " + totalBatches + " batches.");
	}

	/*
	 * As soon as this method finishes, it may be called again with a new batch
	 * -- ie to process more than one batch at a time, simply return before the
	 * first one is finished
	 */
	public void processBatch(NativeProtocol[] protocols, int numOfProtocols) throws IOException {	
		SCENetworkImpl sceNetwork = new SCENetworkImpl(this.resourcePool.getNoOfParties(), DEFAULT_THREAD_ID);
		for (int i=0; i<numOfProtocols; i++) {
			int round = 0;
			EvaluationStatus status;
			do {
				status = protocols[i].evaluate(round, this.resourcePool, sceNetwork);				
				//send phase
				Map<Integer, byte[]> output = sceNetwork.getOutputFromThisRound();				
				for(int pId : output.keySet()) {
					//send array since queue is not serializable
					this.network.send(DEFAULT_CHANNEL, pId, output.get(pId));					
				}
				
				//receive phase
				Map<Integer, ByteBuffer> inputForThisRound = new HashMap<Integer, ByteBuffer>();
				for(int pId : sceNetwork.getExpectedInputForNextRound()) {
					byte[] messages = this.network.receive(DEFAULT_CHANNEL, pId);					
					inputForThisRound.put(pId, ByteBuffer.wrap(messages));
				}
				sceNetwork.setInput(inputForThisRound);				
				sceNetwork.nextRound();
				round++;
			} while (status.equals(EvaluationStatus.HAS_MORE_ROUNDS));
			
			// Just a sanity check:
			// System.out.println("Sequential evaluator completely done with protocol: " + protocols[i]);
			//for (Value v : protocols[i].getOutputValues()) {
			//	//System.out.println("" + i + ": ---> Value is ready: " + v + "; hash " + v.hashCode());
			//	if (!v.isReady())
			//		throw new MPCException(
			//				"For some reason the protocol: " + protocols[i] + " says its not ready, though it returned EvaluationStatus.IS_DONE");
			//}
			
		}		
	}
}
