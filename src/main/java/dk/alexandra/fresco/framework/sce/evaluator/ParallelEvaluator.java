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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.SCEResourcePool;
import dk.alexandra.fresco.framework.sce.resources.threads.VMThreadPool;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class ParallelEvaluator implements ProtocolEvaluator {

	private int maxBatchSize, threads;
	private SCEResourcePool rp;
	private ProtocolSuite pii;

	public ParallelEvaluator() {
		this.maxBatchSize = 4096;	
	}

	@Override
	public void eval(ProtocolProducer c) {
		do {
			NativeProtocol[] nextGates = new NativeProtocol[maxBatchSize];
			int numOfGatesInBatch = c.getNextProtocols(nextGates, 0);
			processBatch(nextGates, numOfGatesInBatch);
		} while (c.hasNextProtocols());
		this.pii.finishedEval();
	}

	/*
	 * As soon as this method finishes, it may be called again with a new batch
	 * -- ie to process more than one batch at a time, simply return before the
	 * first one is finished
	 */
	public void processBatch(NativeProtocol[] gates, int numOfGates) {
		int jobs = 1;
		if (numOfGates > 15) {
			jobs = (numOfGates > threads) ? threads : numOfGates;
		}
		ArrayList<BatchTask> tasks = new ArrayList<BatchTask>(threads);
		for (int i = 0; i < jobs; i++) {
			tasks.add(new BatchTask(gates, i, jobs, numOfGates, rp));
		}
		VMThreadPool es = rp.getVMThreadPool();
		try {
			List<Future<Object>> futures = es.submitVMTasks(tasks);
			for (Future<Object> f: futures) {
				@SuppressWarnings("unused")
				Object o = f.get();
			}
		} catch (InterruptedException e) {
			Reporter.severe("Evaluation was interrupted.", e);
		} catch (ExecutionException e) {
			Reporter.severe("Exception during evaluation.", e);
		}
		pii.synchronize(numOfGates);
	}

	@Override
	public void setProtocolInvocation(ProtocolSuite pii) {
		this.pii = pii;
	}
	
	@Override
	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}
	
	@Override
	public void setResourcePool(SCEResourcePool resourcePool) {
		this.rp = resourcePool;
		this.threads = resourcePool.getVMThreadPool().getVMThreadCount();
	}
	
	private class BatchTask implements Callable<Object> {

		NativeProtocol[] gates;
		int offset, interval, totalGates;
		int channel;
		ResourcePool rp;

		public BatchTask(NativeProtocol[] gates, int offset, int interval, int totalGates, ResourcePool rp) {
			this.offset = offset;
			this.channel = offset;
			this.interval = interval;
			this.gates = gates;
			this.rp = rp;
			this.totalGates = totalGates;
		}

		@Override
		public Object call() throws Exception {
			SCENetworkImpl protocolNetwork = new SCENetworkImpl(this.rp.getNoOfParties(), offset);			
			Network network = rp.getNetwork();
			for (int i=offset; i< totalGates; i+=interval) {
				int round = 0;
				EvaluationStatus status;
				do {					
					status = gates[i].evaluate(round, this.rp, protocolNetwork);
					//send phase
					Map<Integer, byte[]> output = protocolNetwork.getOutputFromThisRound();				
					for(int pId : output.keySet()) {
						//send array since queue is not serializable
						network.send(channel, pId, output.get(pId));					
					}
					
					//receive phase
					Map<Integer, ByteBuffer> inputForThisRound = new HashMap<Integer, ByteBuffer>();
					for(int pId : protocolNetwork.getExpectedInputForNextRound()) {					
						byte[] messages = network.receive(channel, pId);						
						inputForThisRound.put(pId, ByteBuffer.wrap(messages));
					}
					protocolNetwork.setInput(inputForThisRound);
					protocolNetwork.nextRound();
					round++;
				} while (status.equals(EvaluationStatus.HAS_MORE_ROUNDS));
			}			
			return null;
		}
	}
}
