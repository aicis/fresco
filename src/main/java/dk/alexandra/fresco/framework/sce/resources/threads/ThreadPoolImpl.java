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
package dk.alexandra.fresco.framework.sce.resources.threads;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The thread pool of an SCE. Manages two independent pools, one for the VM and
 * one for the protocol suites. This models two possible layers of parallelism:
 * 
 * At the VM layer we can parallelize the calls to protocols of the underlying
 * protocol suite. Independent calls can thus been run in parallel.
 * 
 * At the protocol suite layer we may want to parallelize the evaluation of
 * single protocols. Since the VM does not know the internals of the protocol
 * suite and its protocols, this must be done by the protocol suite itself.
 * 
 * This design allows for both types of parallelism. By configuration the number
 * of threads in each pool the parallelism can be assigned to either of the two,
 * or be mixed over both layers.
 * 
 * 
 * 
 * @author psn
 * 
 */
public class ThreadPoolImpl implements ProtocolThreadPool, VMThreadPool {

	private int protocolThreads;
	private int vmThreads;
	private ExecutorService vmPool;
	private ExecutorService protocolPool;

	/**
	 * Constructs a new threadpool with specified number of threads.
	 * 
	 * @param vmThreads
	 *            number of threads for the vm
	 * @param protocolThreads
	 *            number of threads for the protocol suite
	 */
	public ThreadPoolImpl(int vmThreads, int protocolThreads) {
		this.vmThreads = vmThreads;
		this.protocolThreads = protocolThreads;
		this.vmPool = Executors.newFixedThreadPool(vmThreads);
		this.protocolPool = Executors.newFixedThreadPool(protocolThreads);
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.framework.sce.resources.threads.ThreadPool#getProtocolThreadCount()
	 */
	@Override
	public int getThreadCount() {
		return protocolThreads;
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.framework.sce.resources.threads.VMThreadPool#getVMThreadCount()
	 */
	@Override
	public int getVMThreadCount() {
		return vmThreads;
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.framework.sce.resources.threads.VMThreadPool#submitVMTask(java.util.concurrent.Callable)
	 */
	@Override
	public <T> Future<T> submitVMTask(Callable<T> task) {
		return vmPool.submit(task);
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.framework.sce.resources.threads.ThreadPool#submitProtocolTask(java.util.concurrent.Callable)
	 */
	@Override
	public <T> Future<T> submitTask(Callable<T> task) {
		return protocolPool.submit(task);
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.framework.sce.resources.threads.VMThreadPool#shutdownVMPool()
	 */
	@Override
	public void shutdownVMPool() {
		vmPool.shutdown();
	}

	public void shutdown() {
		shutdownVMPool();
		protocolPool.shutdown();
	}

	@Override
	public <T> List<Future<T>> submitVMTasks(
			Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.vmPool.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> submitTasks(
			Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.protocolPool.invokeAll(tasks);
	}
}
