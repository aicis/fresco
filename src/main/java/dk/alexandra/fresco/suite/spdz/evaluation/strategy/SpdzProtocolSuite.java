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
package dk.alexandra.fresco.suite.spdz.evaluation.strategy;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.gates.SpdzCommitProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOpenCommitProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDummyImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class SpdzProtocolSuite implements ProtocolSuite {

	private static Map<Integer, SpdzProtocolSuite> instances;

	private Network network;
	private Random rand;
	private SpdzStorage[] store;
	private ResourcePool rp;
	private int gatesEvaluated = 0;
	private int macCheckThreshold = 100000;
	private BigInteger keyShare, p;
	private MessageDigest[] digs;
	private SpdzConfiguration spdzConf;

	public SpdzProtocolSuite() {
	}

	public synchronized static SpdzProtocolSuite getInstance(int id) {
		if (instances == null) {
			instances = new HashMap<Integer, SpdzProtocolSuite>();
		}
		if (instances.get(id) == null) {
			instances.put(id, new SpdzProtocolSuite());
		}
		return instances.get(id);
	}

	public SpdzStorage getStore(int i) {
		return store[i];
	}

	public BigInteger getKeyShare() {
		return keyShare;
	}

	public BigInteger getModulus() {
		return p;
	}

	public SpdzConfiguration getConf() {
		return this.spdzConf;
	}

	public MessageDigest getMessageDigest(int threadId) {
		return this.digs[threadId];
	}

	@Override
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf) {
		spdzConf = (SpdzConfiguration) conf;
		this.network = resourcePool.getNetwork();
		int noOfThreads = resourcePool.getVMThreadCount();
		this.store = new SpdzStorage[noOfThreads];
		for (int i = 0; i < noOfThreads; i++) {
			switch(spdzConf.getPreprocessingStrategy()) {
			case DUMMY:
				store[i] = new SpdzStorageDummyImpl(resourcePool.getMyId(), resourcePool.getNoOfParties());
				break;
			case STATIC:
				store[i] = new SpdzStorageImpl(resourcePool, i);
				break;
			case FUELSTATION:
				store[i] = new SpdzStorageImpl(resourcePool, i, true, spdzConf.fuelStationBaseUrl());
			}			
		}
		this.rand = resourcePool.getSecureRandom();
		this.rp = resourcePool;

		try {
			this.digs = new MessageDigest[noOfThreads];
			for (int i = 0; i < this.digs.length; i++) {
				this.digs[i] = MessageDigest.getInstance("SHA-256");
			}
		} catch (NoSuchAlgorithmException e) {
			Reporter.warn("SHA-256 not supported as digest on this system. Might not influence "
					+ "computation if your chosen SCPS does not depend on a hash function.");
		}

		try {
			this.store[0].getSSK();
		} catch (MPCException e) {
			throw new MPCException("No preprocessed data found for SPDZ - aborting.", e);
		}

		// Initialize various fields global to the computation.
		this.keyShare = store[0].getSSK();
		this.p = store[0].getSupplier().getModulus();
		Util.setModulus(this.p);		
	}

	@Override
	public void synchronize(int gatesEvaluated) throws MPCException {
		this.gatesEvaluated += gatesEvaluated;
		if (this.gatesEvaluated > macCheckThreshold) {
			try {
				for (int i = 1; i < store.length; i++) {
					store[0].getOpenedValues().addAll(store[i].getOpenedValues());
					store[0].getClosedValues().addAll(store[i].getClosedValues());
					store[i].reset();
				}
				MACCheck();
			} catch (IOException e) {
				throw new MPCException("Could not complete MACCheck.", e);
			}
			this.gatesEvaluated = 0;
		}
	}

	@Override
	public void finishedEval() {
		try {
			MACCheck();
			this.gatesEvaluated = 0;
		} catch (IOException e) {
			throw new MPCException("Could not complete MACCheck.", e);
		}
	}

	private void MACCheck() throws IOException {
		// TODO: This is not truly random
		BigInteger s = new BigInteger(Util.getModulus().bitLength(), rand).mod(Util.getModulus());
		SpdzCommitment commitment = new SpdzCommitment(this.digs[0], s, rand);
		Map<Integer, BigInteger> comms = new HashMap<Integer, BigInteger>();
		SpdzCommitProtocol comm = new SpdzCommitProtocol(commitment, comms);
		Map<Integer, BigInteger> ss = new HashMap<Integer, BigInteger>();
		SpdzOpenCommitProtocol open = new SpdzOpenCommitProtocol(commitment, comms, ss);

		SCENetworkImpl protocolNetwork = new SCENetworkImpl(this.rp.getNoOfParties(), 0);

		EvaluationStatus status;
		int i = 0;
		do {
			status = comm.evaluate(i, this.rp, protocolNetwork);
			i++;
			// send phase
			Map<Integer, Queue<Serializable>> output = protocolNetwork.getOutputFromThisRound();
			for (int pId : output.keySet()) {
				// send array since queue is not serializable
				network.send("0", pId, output.get(pId).toArray(new Serializable[0]));
			}

			// receive phase
			Map<Integer, Queue<Serializable>> inputForThisRound = new HashMap<Integer, Queue<Serializable>>();
			for (int pId : protocolNetwork.getExpectedInputForNextRound()) {
				Serializable[] messages = network.receive("0", pId);
				Queue<Serializable> q = new LinkedBlockingQueue<Serializable>();
				// convert back from array to queue.
				for (Serializable message : messages) {
					q.offer(message);
				}
				inputForThisRound.put(pId, q);
			}
			protocolNetwork.setInput(inputForThisRound);
			protocolNetwork.nextRound();
		} while (status != EvaluationStatus.IS_DONE);

		i = 0;
		do {
			status = open.evaluate(i, this.rp, protocolNetwork);
			i++;
			// send phase
			Map<Integer, Queue<Serializable>> output = protocolNetwork.getOutputFromThisRound();
			for (int pId : output.keySet()) {
				// send array since queue is not serializable
				network.send("0", pId, output.get(pId).toArray(new Serializable[0]));
			}

			// receive phase
			Map<Integer, Queue<Serializable>> inputForThisRound = new HashMap<Integer, Queue<Serializable>>();
			for (int pId : protocolNetwork.getExpectedInputForNextRound()) {
				Serializable[] messages = network.receive("0", pId);
				Queue<Serializable> q = new LinkedBlockingQueue<Serializable>();
				// convert back from array to queue.
				for (Serializable message : messages) {
					q.offer(message);
				}
				inputForThisRound.put(pId, q);
			}
			protocolNetwork.setInput(inputForThisRound);
			protocolNetwork.nextRound();
		} while (status != EvaluationStatus.IS_DONE);

		// Add all s's to get the common random value:
		s = BigInteger.ZERO;
		for (BigInteger otherS : ss.values()) {
			s = s.add(otherS);
		}

		// TODO: Need to gather all values from all stores in a smart manner
		// that makes the check good.
		List<BigInteger> as = this.store[0].getOpenedValues();
		int t = as.size();

		BigInteger[] rs = new BigInteger[t];
		MessageDigest H = new Util().getHashFunction();
		BigInteger r_temp = s;
		for (i = 0; i < t; i++) {
			r_temp = new BigInteger(H.digest(r_temp.toByteArray())).mod(Util.getModulus());
			rs[i] = r_temp;
		}
		BigInteger a = BigInteger.ZERO;
		int index = 0;
		for (BigInteger aa : as) {
			a = a.add(aa.multiply(rs[index++])).mod(Util.getModulus());
		}
		// compute gamma_i as the sum of all MAC's on the opened values times
		// r_j.
		List<SpdzElement> closedValues = store[0].getClosedValues();
		if (closedValues.size() != t) {
			throw new MPCException(
					"Amount of closed values does not equal the amount of partially opened values. Aborting!");
		}
		BigInteger gamma = BigInteger.ZERO;
		index = 0;
		for (SpdzElement c : closedValues) {
			gamma = gamma.add(rs[index++].multiply(c.getMac())).mod(Util.getModulus());
		}

		// compute delta_i as: gamma_i - alpha_i*a
		BigInteger delta = gamma.subtract(store[0].getSSK().multiply(a)).mod(Util.getModulus());
		// Commit to delta and open it afterwards
		commitment = new SpdzCommitment(this.digs[0], delta, rand);
		comms = new HashMap<Integer, BigInteger>();
		comm = new SpdzCommitProtocol(commitment, comms);
		ss = new HashMap<Integer, BigInteger>();
		open = new SpdzOpenCommitProtocol(commitment, comms, ss);

		status = null;
		i = 0;
		do {
			status = comm.evaluate(i, this.rp, protocolNetwork);
			i++;
			// send phase
			Map<Integer, Queue<Serializable>> output = protocolNetwork.getOutputFromThisRound();
			for (int pId : output.keySet()) {
				// send array since queue is not serializable
				network.send("0", pId, output.get(pId).toArray(new Serializable[0]));
			}

			// receive phase
			Map<Integer, Queue<Serializable>> inputForThisRound = new HashMap<Integer, Queue<Serializable>>();
			for (int pId : protocolNetwork.getExpectedInputForNextRound()) {
				Serializable[] messages = network.receive("0", pId);
				Queue<Serializable> q = new LinkedBlockingQueue<Serializable>();
				// convert back from array to queue.
				for (Serializable message : messages) {
					q.offer(message);
				}
				inputForThisRound.put(pId, q);
			}
			protocolNetwork.setInput(inputForThisRound);
			protocolNetwork.nextRound();
		} while (status != EvaluationStatus.IS_DONE);

		i = 0;
		do {
			status = open.evaluate(i, this.rp, protocolNetwork);
			i++;
			// send phase
			Map<Integer, Queue<Serializable>> output = protocolNetwork.getOutputFromThisRound();
			for (int pId : output.keySet()) {
				// send array since queue is not serializable
				network.send("0", pId, output.get(pId).toArray(new Serializable[0]));
			}

			// receive phase
			Map<Integer, Queue<Serializable>> inputForThisRound = new HashMap<Integer, Queue<Serializable>>();
			for (int pId : protocolNetwork.getExpectedInputForNextRound()) {
				Serializable[] messages = network.receive("0", pId);
				Queue<Serializable> q = new LinkedBlockingQueue<Serializable>();
				// convert back from array to queue.
				for (Serializable message : messages) {
					q.offer(message);
				}
				inputForThisRound.put(pId, q);
			}
			protocolNetwork.setInput(inputForThisRound);
			protocolNetwork.nextRound();
		} while (status != EvaluationStatus.IS_DONE);

		BigInteger deltaSum = BigInteger.ZERO;
		for (BigInteger d : ss.values()) {
			deltaSum = deltaSum.add(d);
		}
		deltaSum = deltaSum.mod(Util.getModulus());
		if (!deltaSum.equals(BigInteger.ZERO)) {			
			throw new MPCException("The sum of delta's was not 0. Someone was corrupting something amongst " + t
					+ " macs. Sum was " + deltaSum.toString() + " Aborting!");
		}
		// clean up store before returning to evaluating such that we only
		// evaluate the next macs, not those we already checked.
		this.store[0].reset();
	}

	@Override
	public void destroy() {
		for (SpdzStorage store : this.store) {
			store.shutdown();
		}
	}
}
