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
package dk.alexandra.fresco.framework.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class SCENetworkImpl implements SCENetwork, SCENetworkSupplier{

	private int noOfParties;
	//TODO: Remove when possible - also from interface.
	private int threadId;
	
	private Map<Integer, Queue<Serializable>> input;
	private Map<Integer, Queue<Serializable>> output;
	private Set<Integer> expectedInputForNextRound;		
	
	public SCENetworkImpl(int noOfParties, int threadId) {
		this.noOfParties = noOfParties;
		this.threadId = threadId;
		this.output = new HashMap<Integer, Queue<Serializable>>();
		this.expectedInputForNextRound = new HashSet<Integer>();
	}
	
	//ProtocolNetwork
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T receive(int id) {
		return (T) this.input.get(id).poll();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> List<T> receiveFromAll() {
		List<T> res = new ArrayList<T>();
		for(int i = 1; i <= noOfParties; i++) {
			res.add((T) this.input.get(i).poll()); 
		}
		return res;
	}
	
	@Override
	public void send(int id, Serializable o) {
		if(id < 1) {
			throw new IllegalArgumentException("Cannot send to an Id smaller than 1");
		}
		Queue<Serializable> q = this.output.get(id);
		if(q == null) {
			q = new LinkedBlockingQueue<Serializable>();
			this.output.put(id, q);
		}
		q.offer(o);		
	}
	
	@Override
	public void sendToAll(Serializable o) {
		for(int i = 1; i <= noOfParties; i++) {
			send(i, o);
		}
	}
	
	@Override
	public void sendSharesToAll(Serializable[] o) {
		for(int i = 1; i <= noOfParties; i++) {
			send(i, o[i-1]);
		}
	}
	
	@Override
	public void expectInputFromPlayer(int id) {
		if(id < 1) {
			throw new IllegalArgumentException("Cannot send to an Id smaller than 1");
		}
		this.expectedInputForNextRound.add(id);
	}
	@Override
	public void expectInputFromAll() {
		for(int i = 1; i <= noOfParties; i++) {
			this.expectedInputForNextRound.add(i);
		}
	}

	@Override
	public int getThreadId() {
		return threadId;
	}
	
	//ProtocolNetworkSupplier
	
	@Override
	public void setInput(Map<Integer, Queue<Serializable>> inputForThisRound) {
		this.input = inputForThisRound;
	}

	@Override
	public Map<Integer, Queue<Serializable>> getOutputFromThisRound() {
		return this.output;
	}

	@Override
	public Set<Integer> getExpectedInputForNextRound() {
		return this.expectedInputForNextRound;
	}

	@Override
	public void nextRound() {
		this.output.clear();
		this.expectedInputForNextRound.clear();
	}
}
