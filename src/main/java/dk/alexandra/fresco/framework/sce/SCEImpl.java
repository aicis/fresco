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
package dk.alexandra.fresco.framework.sce;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.ScapiNetworkImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedParallelEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.ParallelEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.SCEResourcePool;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.resources.threads.ThreadPoolImpl;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.bgw.BgwFactory;
import dk.alexandra.fresco.suite.bgw.BgwProtocolSuite;
import dk.alexandra.fresco.suite.bgw.configuration.BgwConfiguration;
import dk.alexandra.fresco.suite.bgw.configuration.BgwConfigurationFromProperties;
import dk.alexandra.fresco.suite.dummy.DummyConfiguration;
import dk.alexandra.fresco.suite.dummy.DummyFactory;
import dk.alexandra.fresco.suite.dummy.DummyProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfigurationFromProperties;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;

/**
 * Secure Computation Engine - responsible for having the overview of things and
 * setting everything up, e.g., based on properties.
 * 
 * @author Kasper Damgaard (.. and others)
 * 
 */
public class SCEImpl implements SCE {

	private ProtocolEvaluator evaluator;
	private ThreadPoolImpl threadPool;
	private SCEResourcePool resourcePool;
	private ProtocolFactory protocolFactory;
	private SCEConfiguration sceConf;
	private ProtocolSuite protocolSuite;
	private ProtocolSuiteConfiguration psConf;

	private boolean setup = false;

	protected SCEImpl(SCEConfiguration sceConf) {
		this.sceConf = sceConf;		
	}

	protected SCEImpl(SCEConfiguration sceConf, ProtocolSuiteConfiguration psConf) {
		this.sceConf = sceConf;
		this.psConf = psConf;		
	}

	@Override
	public SCEConfiguration getSCEConfiguration() {
		return this.sceConf;
	}

	@Override
	public synchronized void setup() throws IOException {		
		if (this.setup) {
			return;
		}
		int myId = sceConf.getMyId();
		Map<Integer, Party> parties = sceConf.getParties();
		Level logLevel = sceConf.getLogLevel();
		Reporter.init(logLevel);
		if (parties.isEmpty()) {
			throw new IllegalArgumentException(
					"Properties file should contain at least one party of the form 'party1=192.168.0.1,8000'");
		}
		int noOfThreads = sceConf.getNoOfThreads();
		int noOfvmThreads = sceConf.getNoOfVMThreads();
		NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties, logLevel);

		ThreadPoolImpl threadPool = null;
		Storage storage = sceConf.getStorage();
		StreamedStorage streamedStorage = sceConf.getStreamedStorage();
		// Secure random by default.
		Random rand = new Random(0);
		SecureRandom secRand = new SecureRandom();

		this.evaluator = this.sceConf.getEvaluator();
		this.evaluator.setMaxBatchSize(sceConf.getMaxBatchSize());
		int channelAmount = 1;
		// If the evaluator is of a parallel sort,
		// we need the same amount of channels as the number of VM threads we
		// use.
		if (this.evaluator instanceof ParallelEvaluator || this.evaluator instanceof BatchedParallelEvaluator) {
			channelAmount = noOfvmThreads;
		}
		ScapiNetworkImpl network = new ScapiNetworkImpl(conf, channelAmount);

		if (noOfvmThreads == -1) {
			// default to 1 allowed VM thread only - otherwise certain
			// strategies are going to outright fail.
			noOfvmThreads = 1;
		}
		if (noOfThreads != -1) {
			threadPool = new ThreadPoolImpl(noOfvmThreads, noOfThreads);
		} else {
			threadPool = new ThreadPoolImpl(noOfvmThreads, 0);
		}

		this.resourcePool = new ResourcePoolImpl(sceConf.getMyId(), parties.size(), network, storage, streamedStorage,
				rand, secRand, threadPool, threadPool);

		this.resourcePool.initializeRandom();
		this.resourcePool.initializeThreadPool();
		this.resourcePool.initilizeStorage();
		this.resourcePool.initializeNetwork();		

		String runtime = sceConf.getProtocolSuiteName();
		switch (runtime.toLowerCase()) {
		case "spdz":
			this.protocolSuite = SpdzProtocolSuite.getInstance(this.resourcePool.getMyId());
			if (psConf == null) {
				psConf = new SpdzConfigurationFromProperties();
			}			
			this.protocolSuite.init(this.resourcePool, psConf);
			// TODO: Fix this storage crap - not optimal to have the '0' put
			// there. Need to make the factories decoupled from the storage.
			dk.alexandra.fresco.suite.spdz.storage.SpdzStorage spdzStorage = ((SpdzProtocolSuite) this.protocolSuite)
					.getStore(0);
			int maxBitLength = ((SpdzConfiguration) psConf).getMaxBitLength();
			this.protocolFactory = new SpdzFactory(spdzStorage, this.resourcePool.getMyId(), maxBitLength);
			break;
		case "bgw":
			this.protocolSuite = BgwProtocolSuite.getInstance();
			if (psConf == null) {
				psConf = new BgwConfigurationFromProperties();
			}
			this.protocolSuite.init(this.resourcePool, psConf);
			int threshold = ((BgwConfiguration) psConf).getThreshold();
			BigInteger modulus = ((BgwConfiguration) psConf).getModulus();
			this.protocolFactory = new BgwFactory(this.resourcePool.getMyId(), this.resourcePool.getNoOfParties(),
					threshold, modulus);
			break;
		case "dummy":
			this.protocolSuite = new DummyProtocolSuite();
			if (psConf == null) {
				psConf = new DummyConfiguration();
			}
			this.protocolSuite.init(this.resourcePool, psConf);
			this.protocolFactory = new DummyFactory();
			break;
		default:
			throw new IllegalArgumentException(
					"Could not understand the specified runtime. This framework currently supports:\n\t-spdz\n\t-bgw\n\t-dummy");
		}

		this.setup = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.alexandra.fresco.framework.sce.SCE#runApplication(dk.alexandra.fresco
	 * .framework.Application)
	 */
	@Override
	public void runApplication(Application application) {
		try {
			Reporter.init(this.getSCEConfiguration().getLogLevel());			
			setup();
			Reporter.info("Running application: " + application.getClass().getSimpleName() + " using protocol suite: "
					+ this.getSCEConfiguration().getProtocolSuiteName());
			Reporter.info("Using the configuration: " + this.getSCEConfiguration());			
			this.evaluator.setResourcePool(this.resourcePool);
			this.evaluator.setProtocolInvocation(this.protocolSuite);
		} catch (IOException e) {
			throw new MPCException("Could not run application due to errors during setup: " + e.getMessage(), e);
		}
		evalApplication(application);
	}

	private void evalApplication(Application app) {
		try {
			ProtocolProducer prod = app.prepareApplication(this.protocolFactory);
			if(prod != null) {
				this.evaluator.eval(prod);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdownSCE() {
		if (!setup) {
			return;
		}
		this.evaluator = null;
		try {
			if (this.resourcePool != null) {
				this.resourcePool.shutdownNetwork();
				this.resourcePool = null;
			}
		} catch (IOException e) {
			// Do nothing about it..
		}
		if (this.threadPool != null) {
			// shuts down both vm and protocol threadpools
			this.threadPool.shutdown();
			this.threadPool = null;
		}
		if (this.protocolSuite != null) {
			this.protocolSuite.destroy();
			this.protocolSuite = null;
		}
		this.resourcePool = null;
		this.protocolFactory = null;
	}

}
