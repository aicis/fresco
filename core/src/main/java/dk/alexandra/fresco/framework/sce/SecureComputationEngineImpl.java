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
package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
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
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

/**
 * Secure Computation Engine - responsible for having the overview of things and
 * setting everything up, e.g., based on properties.
 *
 * @author Kasper Damgaard (.. and others)
 */
public class SecureComputationEngineImpl implements SecureComputationEngine {

  private ProtocolEvaluator evaluator;
  private ThreadPoolImpl threadPool;
  private SCEResourcePool resourcePool;
  private ProtocolFactory protocolFactory;
  private SCEConfiguration sceConf;
  private ProtocolSuiteConfiguration protocolSuiteConfiguration;

  private boolean setup;
  private ProtocolSuite protocolSuite;

  public SecureComputationEngineImpl(SCEConfiguration sceConf,
      ProtocolSuiteConfiguration protocolSuite) {
    this.sceConf = sceConf;
    this.protocolSuiteConfiguration = protocolSuite;

    this.setup = false;

    //setup the basic stuff, but do not initialize anything yet
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
    if (this.evaluator instanceof ParallelEvaluator
        || this.evaluator instanceof BatchedParallelEvaluator) {
      channelAmount = noOfvmThreads;
    }
    Network network = sceConf.getNetwork(conf, channelAmount);
    if (network == null) {
      NetworkingStrategy networkStrat = sceConf.getNetworkStrategy();
      switch (networkStrat) {
        case KRYONET:
          // TODO[PSN]
          // This might work on mac?
//          network = new KryoNetNetwork();
          network = new ScapiNetworkImpl();
          break;
        case SCAPI:
          network = new ScapiNetworkImpl();
          break;
        default:
          throw new ConfigurationException("Unknown networking strategy " + networkStrat);
      }
      network.init(conf, channelAmount);
    }

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

    this.resourcePool = new ResourcePoolImpl(sceConf.getMyId(), parties.size(), network, storage,
        streamedStorage,
        rand, secRand, threadPool, threadPool);
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

    this.resourcePool.initializeRandom();
    this.resourcePool.initializeThreadPool();
    this.resourcePool.initilizeStorage();
    this.resourcePool.initializeNetwork();

    this.protocolSuite = this.protocolSuiteConfiguration.createProtocolSuite(sceConf.getMyId());
    this.protocolFactory = this.protocolSuite.init(this.resourcePool);
    this.setup = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * dk.alexandra.fresco.framework.sce.SecureComputationEngine#runApplication(dk.alexandra.fresco
   * .framework.Application)
   */
  @Override
  public void runApplication(Application application) {
    prepareEvaluator();
    ProtocolProducer prod = application.prepareApplication(this.protocolFactory);
    String appName = application.getClass().getName();
    Reporter.info("Running application: " + appName + " using protocol suite: "
        + this.protocolSuite);
    evalApplication(prod, appName);
  }

  private void prepareEvaluator() {
    try {
      Reporter.init(this.getSCEConfiguration().getLogLevel());
      setup();
      this.evaluator.setResourcePool(this.resourcePool);
      this.evaluator.setProtocolInvocation(this.protocolSuite);
    } catch (IOException e) {
      throw new MPCException(
          "Could not run application due to errors during setup: " + e.getMessage(), e);
    }
  }

  private void evalApplication(ProtocolProducer prod, String appName) {
    try {
      if (prod != null) {
        Reporter.info("Using the configuration: " + this.getSCEConfiguration());
        long then = System.currentTimeMillis();
        this.evaluator.eval(prod);
        long now = System.currentTimeMillis();
        long timeSpend = now - then;
        Reporter.info("Running the application " + appName + " took " + timeSpend + " ms.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void shutdownSCE() {
    this.evaluator = null;
    try {
      if (this.resourcePool != null) {
        this.resourcePool.shutdownNetwork();
        if (this.resourcePool.getStreamedStorage() != null) {
          this.resourcePool.getStreamedStorage().shutdown();
        }
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
    this.setup = false;
  }

}
