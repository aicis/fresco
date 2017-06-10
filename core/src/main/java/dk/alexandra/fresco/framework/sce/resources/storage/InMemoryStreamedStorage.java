/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.framework.sce.resources.storage;

import dk.alexandra.fresco.framework.MPCException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class InMemoryStreamedStorage implements StreamedStorage {

	private Storage storage;
	private Map<String, Queue<Serializable>> queues;
	
	public InMemoryStreamedStorage(Storage storage) {
		this.storage = storage;
		this.queues = new HashMap<String, Queue<Serializable>>();
	}
	
	@Override
	public boolean putObject(String name, String key, Serializable o) {
		return this.storage.putObject(name, key, o);
	}

	@Override
	public <T extends Serializable> T getObject(String name, String key) {
		return this.storage.getObject(name, key);
	}

	@Override
	public <T extends Serializable> T getNext(String name) {
		Queue<Serializable> queue = this.queues.get(name);
		if(queue == null) {
			throw new MPCException("Could not find any store with name "+name);
		}
		Serializable res = queue.poll();
		if(res == null) {
			throw new MPCException("No more elements in store with name "+name);
		}
		return (T)res;
	}

	@Override
	public boolean putNext(String name, Serializable o) {
		if(this.queues.containsKey(name)) {
			this.queues.get(name).offer(o);
			return true;
		} else {
			Queue<Serializable> queue = new LinkedBlockingQueue<>();
			queue.offer(o);
			this.queues.put(name, queue);
			return true;
		}
	}

	@Override
	public void shutdown() {
		
	}

}
