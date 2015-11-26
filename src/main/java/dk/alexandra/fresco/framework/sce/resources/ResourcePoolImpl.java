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
package dk.alexandra.fresco.framework.sce.resources;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.threads.ProtocolThreadPool;
import dk.alexandra.fresco.framework.sce.resources.threads.VMThreadPool;

/**
 * Container for resources needed by runtimes (protocol suites).
 * 
 * @author Kasper Damgaard
 *
 */
public class ResourcePoolImpl implements SCEResourcePool {

	private int myId; 
	private int noOfPlayers;
	protected Network network;
	protected Storage storage;
	protected Random random;
	protected SecureRandom secRand;
	protected final ProtocolThreadPool protocolThreadPool;
	protected final VMThreadPool vmThreadPool;

	public ResourcePoolImpl(int myId, int noOfPlayers, Network network,
			Storage storage, Random random, SecureRandom secRand,
			ProtocolThreadPool threadPool, VMThreadPool vmThreadPool) {
		this.myId = myId;
		this.noOfPlayers = noOfPlayers;
		this.network = network;
		this.storage = storage;
		this.random = random;
		this.secRand = secRand;
		this.protocolThreadPool = threadPool;
		this.vmThreadPool = vmThreadPool;
	}

	/**
	 * Assumes all resources are needed, and initializes all of them.
	 */
	public void initializeAll() throws IOException {
		initializeNetwork();
		initilizeStorage();
		initializeThreadPool();
		initializeRandom();
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * After calling this method, a runtime can expect the network channels are
	 * all connected and ready to send/receive.
	 */
	public void initializeNetwork() throws IOException {
		// TODO: Should maybe have this configuration somewhere
		network.connect(10000);
	}

	public void shutdownNetwork() throws IOException {
		network.close();
	}

	public void initilizeStorage() {

	}

	public void initializeRandom() {

	}

	public void initializeThreadPool() {
	}

	public ProtocolThreadPool getThreadPool() {
		return protocolThreadPool;
	}

	public Network getNetwork() {
		return this.network;
	}

	public Storage getStorage() {
		return this.storage;
	}

	public Random getRandom() {
		return this.random;
	}

	public SecureRandom getSecureRandom() {
		return this.secRand;
	}

	@Override
	public int getMyId() {
		return this.myId;
	}

	@Override
	public int getNoOfParties() {
		return this.noOfPlayers;
	}

	@Override
	public VMThreadPool getVMThreadPool() {
		return this.vmThreadPool;
	}

}
