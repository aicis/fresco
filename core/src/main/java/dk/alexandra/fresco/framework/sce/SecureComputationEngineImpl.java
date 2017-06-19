/*
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
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.network.ScapiNetworkImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Secure Computation Engine - responsible for having the overview of things and
 * setting everything up, e.g., based on properties.
 *
 * @author Kasper Damgaard (.. and others)
 */
public class SecureComputationEngineImpl implements SecureComputationEngine {

  private ProtocolEvaluator evaluator;
  private SCEConfiguration sceConf;
  private ProtocolSuiteConfiguration protocolSuiteConfiguration;
  private ExecutorService executorService = Executors.newCachedThreadPool();

  private boolean setup;
  private ProtocolSuite protocolSuite;

  public SecureComputationEngineImpl(SCEConfiguration sceConf,
      ProtocolSuiteConfiguration protocolSuite) {
    this.sceConf = sceConf;
    this.protocolSuiteConfiguration = protocolSuite;

    this.setup = false;

    //setup the basic stuff, but do not initialize anything yet
    Reporter.init(sceConf.getLogLevel());
    if (sceConf.getParties().isEmpty()) {
      throw new IllegalArgumentException(
          "Properties file should contain at least one party of the form 'party1=192.168.0.1,8000'");
    }

    this.evaluator = this.sceConf.getEvaluator();
    this.evaluator.setMaxBatchSize(sceConf.getMaxBatchSize());

  }

  private static Network getNetworkFromConfiguration(SCEConfiguration sceConf,
      int myId, Map<Integer, Party> parties) {
    int channelAmount = 1;
    NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties);
    return buildNetwork(conf, channelAmount, sceConf.getNetworkStrategy());
  }

  private static Network buildNetwork(NetworkConfiguration conf,
      int channelAmount, NetworkingStrategy networkStrat) {
    Network network;
    switch (networkStrat) {
      case KRYONET:
        // TODO[PSN]
        // This might work on mac?
        network = new KryoNetNetwork();
        //network = new ScapiNetworkImpl();
        break;
      case SCAPI:
        network = new ScapiNetworkImpl();
        break;
      default:
        throw new ConfigurationException("Unknown networking strategy " + networkStrat);
    }
    network.init(conf, channelAmount);
    return network;
  }

  public static ResourcePoolImpl createResourcePool(SCEConfiguration sceConf) throws IOException {
    int myId = sceConf.getMyId();
    Map<Integer, Party> parties = sceConf.getParties();

    StreamedStorage streamedStorage = sceConf.getStreamedStorage();
    // Secure random by default.
    Random rand = new Random(0);
    SecureRandom secRand = new SecureRandom();

    Network network = getNetworkFromConfiguration(sceConf, myId, parties);
    network.connect(10000);

    ResourcePoolImpl resourcePool =
        new ResourcePoolImpl(myId, parties.size(),
            network, streamedStorage, rand, secRand);

    return resourcePool;

  }

  @Override
  public synchronized void setup() throws IOException {
    if (this.setup) {
      return;
    }
    this.protocolSuite = this.protocolSuiteConfiguration.createProtocolSuite(sceConf.getMyId());
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
  public void runApplication(Application application, ResourcePoolImpl sceNetwork) {
    try {
      startApplication(application, sceNetwork).get(10, TimeUnit.MINUTES);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException("Internal error in waiting", e);
    }
  }

  public Future<?> startApplication(Application application, ResourcePoolImpl resourcePool) {
    prepareEvaluator();
    ProtocolFactory protocolFactory = this.protocolSuite.init(resourcePool);
    ProtocolProducer prod = application.prepareApplication(protocolFactory);
    String appName = application.getClass().getName();
    Reporter.info("Running application: " + appName + " using protocol suite: "
        + this.protocolSuite);

    return executorService.submit(() -> evalApplication(prod, appName, resourcePool));
  }

  private void prepareEvaluator() {
    try {
      Reporter.init(this.sceConf.getLogLevel());
      setup();
      this.evaluator.setProtocolInvocation(this.protocolSuite);
    } catch (IOException e) {
      throw new MPCException(
          "Could not run application due to errors during setup: " + e.getMessage(), e);
    }
  }

  private void evalApplication(ProtocolProducer prod, String appName,
      ResourcePoolImpl resourcePool) {
    try {
      if (prod != null) {
        Reporter.info("Using the configuration: " + this.sceConf);
        long then = System.currentTimeMillis();
        this.evaluator.eval(prod,
            resourcePool);
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
    this.executorService.shutdown();
    if (this.protocolSuite != null) {
      this.protocolSuite.destroy();
    }
    this.setup = false;
  }

}
