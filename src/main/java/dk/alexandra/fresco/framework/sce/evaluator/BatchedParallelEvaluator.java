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
package dk.alexandra.fresco.framework.sce.evaluator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.SCEResourcePool;
import dk.alexandra.fresco.framework.sce.resources.threads.VMThreadPool;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class BatchedParallelEvaluator implements ProtocolEvaluator {

	private int maxBatchSize, threads;
	private SCEResourcePool rp;
	private ProtocolSuite pii;

	public BatchedParallelEvaluator() {
		this.maxBatchSize = 4096; //default value
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
	
	@Override
	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

	/*
	 * As soon as this method finishes, it may be called again with a new batch
	 * -- ie to process more than one batch at a time, simply return before the
	 * first one is finished
	 */
	public void processBatch(NativeProtocol[] protocols, int numOfProtocols) {
		ArrayList<BatchTask> tasks = new ArrayList<BatchTask>(threads);
		int jobs = 1;
		if (numOfProtocols > 7) {
			jobs = (numOfProtocols > threads) ? threads : numOfProtocols;
		}
		for (int i = 0; i < jobs; i++) {
			int protocolsInBatch = numOfProtocols/jobs;
			int index = protocolsInBatch; 
			if(i == jobs-1 && numOfProtocols % jobs > 0) {
				protocolsInBatch+=numOfProtocols % jobs;
			}
			NativeProtocol[] protocolBatch = new NativeProtocol[protocolsInBatch];
			System.arraycopy(protocols, i*index, protocolBatch, 0, protocolBatch.length);
			//TODO: Currently thread 0 gets by far most of the requests - should loadbalance this. (because of e.g. SPDZ issues)
			//TODO: Also, loadbalance the last thread since this one actually gets too many. 
			//(i.e. if numOfProtocols=8, and threads=3, thread 0 and 1 gets 2 each, and thread 2 gets 4. 
			tasks.add(new BatchTask(protocolBatch, i, protocolsInBatch, rp));
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
		pii.synchronize(numOfProtocols);
	}

	@Override
	public void setProtocolInvocation(ProtocolSuite pii) {
		this.pii = pii;
	}

	@Override
	public void setResourcePool(SCEResourcePool resourcePool) {
		this.rp = resourcePool;
		this.threads = resourcePool.getVMThreadPool().getVMThreadCount();
	}
	
	private class BatchTask implements Callable<Object> {

		private NativeProtocol[] protocols;
		private int numOfProtocols;
		private String channel;
		private ResourcePool rp;
		private BatchedStrategy strategy;
		private Network network;
		private SCENetworkImpl[] sceNetworks;
		private Map<Integer, List<Serializable[]>> queues;

		public BatchTask(NativeProtocol[] protocols, int threadId, int numOfProtocols, ResourcePool rp) {
			this.channel = ""+threadId;
			this.protocols = protocols;
			this.rp = rp;
			this.network = rp.getNetwork();
			this.numOfProtocols = numOfProtocols;
			this.strategy = new BatchedStrategy();
			this.queues = new HashMap<Integer, List<Serializable[]>>();
			this.sceNetworks = new SCENetworkImpl[numOfProtocols];
			for (int i = 0; i < numOfProtocols; i++) {
				this.sceNetworks[i] = new SCENetworkImpl(
						this.rp.getNoOfParties(), threadId);
			}
			for (int pId = 1; pId <= this.rp.getNoOfParties(); pId++) {
				this.queues.put(pId, new ArrayList<Serializable[]>());
			}
		}

		@Override
		public Object call() throws Exception {		
			this.strategy.processBatch(protocols, numOfProtocols, sceNetworks, network, channel, rp);
			return null;
		}
	}

}
