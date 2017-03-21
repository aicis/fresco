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
package dk.alexandra.fresco.framework.sce.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class TestSCEConfiguration implements SCEConfiguration {

	private String protocolSuite;
	private NetworkingStrategy network;
	private Storage storage;
	private Map<Integer, Party> parties;
	private int myId;
	private int noOfThreads;
	private int noOfVmThreads;
	private ProtocolEvaluator evaluator;
	private int maxBatchSize;
	
	public TestSCEConfiguration(ProtocolSuite suite, NetworkingStrategy network, ProtocolEvaluator evaluator,
			int noOfThreads, int noOfvmThreads, NetworkConfiguration conf, Storage storage, boolean useSecureConn) {
		this(suite, network, evaluator, noOfThreads, noOfvmThreads, conf, storage, useSecureConn, 4096);
		
	}
	public TestSCEConfiguration(ProtocolSuite suite, NetworkingStrategy network, ProtocolEvaluator evaluator,
			int noOfThreads, int noOfvmThreads, NetworkConfiguration conf, Storage storage, boolean useSecureConn, int maxBatchSize) {
		this.protocolSuite = ProtocolSuite.protocolSuiteToString(suite);
		this.network = network;
		this.storage = storage;
		this.evaluator = evaluator;
		this.noOfThreads = noOfThreads;
		this.noOfVmThreads = noOfvmThreads;
		this.myId = conf.getMyId();
		parties = new HashMap<Integer, Party>();
		for (int i = 1; i <= conf.noOfParties(); i++) {
			if(useSecureConn) {
				Party p = conf.getParty(i);
				//Use the same hardcoded test 128 bit AES key for all connections
				p.setSecretSharedKey("w+1qn2ooNMCN7am9YmYQFQ==");
				parties.put(i, p);
			} else {
				parties.put(i, conf.getParty(i));
			}
		}
		this.maxBatchSize = maxBatchSize;
	}

	@Override
	public int getMyId() {
		return myId;
	}

	@Override
	public Map<Integer, Party> getParties() {
		return parties;
	}

	@Override
	public Level getLogLevel() {
		return Level.INFO;
	}

	@Override
	public String getProtocolSuiteName() {
		return protocolSuite;
	}

	@Override
	public int getNoOfThreads() {
		return this.noOfThreads;
	}

	@Override
	public ProtocolEvaluator getEvaluator() {
		return this.evaluator;
	}

	@Override
	public int getNoOfVMThreads() {
		return this.noOfVmThreads;
	}

	@Override
	public Storage getStorage() {
		return this.storage;
	}

	@Override
	public int getMaxBatchSize() {
		return this.maxBatchSize;
	}
	@Override
	public StreamedStorage getStreamedStorage() {
		if(this.storage instanceof StreamedStorage) {
			return (StreamedStorage)this.storage;
		} else {
			return null;
		}
	}

	@Override
	public NetworkingStrategy getNetwork() {
		return this.network;
	}
}
