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
package dk.alexandra.fresco.framework;

import com.esotericsoftware.minlog.Log;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

public class TestThreadRunner {

  private static final long MAX_WAIT_FOR_THREAD = 6000000;

  public abstract static class TestThread<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> extends
      Thread {

    private boolean finished = false;

    protected TestThreadConfiguration<ResourcePoolT, Builder> conf;

    // Randomness to use in test.
    Random rand;

    Throwable setupException;

    Throwable testException;

    Throwable teardownException;

    protected SecureComputationEngine<ResourcePoolT, Builder> secureComputationEngine;

    void setConfiguration(TestThreadConfiguration<ResourcePoolT, Builder> conf) {
      this.conf = conf;
    }

    @Override
    public String toString() {
      return "TestThread(" + this.conf.netConf.getMyId() + ")";
    }

    @Override
    public void run() {
      try {
        if (conf.sceConf != null) {
          ProtocolSuiteConfiguration<ResourcePoolT, Builder> suite = conf.sceConf.getSuite();
          secureComputationEngine =
              SCEFactory.getSCEFromConfiguration(conf.sceConf, suite);
        }
        KryoNetNetwork.setLogLevel(Log.LEVEL_WARN);
        setUp();
        runTest();
      } catch (Throwable e) {
        Reporter.severe("" + this + " threw exception: ", e);
        this.setupException = e;
        Thread.currentThread().interrupt();
      } finally {
        runTearDown();
      }
    }

    private void runTest() {
      try {
        test();
      } catch (Exception e) {
        this.testException = e;
        Reporter.severe("" + this + " threw exception during test:", e);
        Thread.currentThread().interrupt();
      } catch (AssertionError e) {
        this.testException = e;
        Reporter.severe("Test assertion failed in " + this + ": ", e);
        Thread.currentThread().interrupt();
      }
    }

    private void runTearDown() {
      try {
        if (secureComputationEngine != null) {
          //Shut down SCE resources - does not include the resource pool.
          secureComputationEngine.shutdownSCE();              
        }
        tearDown();
        finished = true;
      } catch (Exception e) {
        Reporter.severe("" + this + " threw exception during tear down:", e);
        this.teardownException = e;
        Thread.currentThread().interrupt();
      }
    }

    public void setUp() throws Exception {
      // Override this if test fixture setup needed.
    }

    public void tearDown() throws Exception {
      // Override this if actions needed to tear down test fixture.
    }

    public abstract void test() throws Exception;

    void setRandom(long nextLong) {
      this.rand = new Random(nextLong);

    }

  }


  /**
   * Container for all the configuration that one thread should have.
   */
  public static class TestThreadConfiguration<ResourcePoolT extends ResourcePool,
      Builder extends ProtocolBuilder> {

    public NetworkConfiguration netConf;
    public TestSCEConfiguration<ResourcePoolT, Builder> sceConf;

    public int getMyId() {
      return this.netConf.getMyId();
    }

    public int getNoOfParties() {
      return this.netConf.noOfParties();
    }

  }


  public abstract static class TestThreadFactory<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

    public abstract TestThread next(TestThreadConfiguration<ResourcePoolT, Builder> conf);
  }

  public static void run(TestThreadFactory f, int noOfPlayers) {
    int randSeed = 42;
    run(f, noOfPlayers, randSeed);
  }

  private static void run(TestThreadFactory f, int noOfPlayers, int randSeed) {
    Map<Integer, NetworkConfiguration> netConfs = TestConfiguration
        .getNetworkConfigurations(noOfPlayers, Level.FINE);

    Map<Integer, TestThreadConfiguration> confs = new HashMap<Integer, TestThreadConfiguration>();
    for (int i : netConfs.keySet()) {
      TestThreadConfiguration ttc = new TestThreadConfiguration();
      ttc.netConf = netConfs.get(i);
      confs.put(i, ttc);
    }
    run(f, confs, randSeed);
  }

  public static void run(TestThreadFactory f, Map<Integer, TestThreadConfiguration> confs) {
    int randSeed = 3457878;
    run(f, confs, randSeed);
  }

  private static void run(TestThreadFactory f, Map<Integer, TestThreadConfiguration> confs,
      int randSeed) throws TestFrameworkException {
    // TODO: Rather use thread container from util.concurrent?

    final Set<TestThread> threads = new HashSet<TestThread>();
    final int n = confs.size();

    Random r = new Random(randSeed);
    for (int i = 0; i < n; i++) {
      TestThreadConfiguration<?, ?> c = confs.get(i + 1);
      TestThread t = f.next(c);
      t.setConfiguration(c);
      t.setRandom(r.nextLong());
      threads.add(t);
    }

    for (Thread t : threads) {
      t.start();
    }

    for (TestThread t : threads) {
      try {
        t.join(MAX_WAIT_FOR_THREAD);
      } catch (InterruptedException e) {
        throw new TestFrameworkException("Test was interrupted");
      } 
      if (!t.finished) {
        Reporter.severe("" + t + " timed out");
        throw new TestFrameworkException(t + " timed out");
      }
      if (t.setupException != null) {
        throw new TestFrameworkException(t + " threw exception in setup (see stderr)");
      } else if (t.testException != null) {
        throw new TestFrameworkException(t + " threw exception in test (see stderr)",
            t.testException);
      } else if (t.teardownException != null) {
        throw new TestFrameworkException(t + " threw exception in teardown (see stderr)");
      }
    }

    //Cleanup - shut down network in manually. All tests should use the NetworkCreator 
    //in order for this to work, or manage the network themselves.
    Map<Integer, ResourcePool> rps = NetworkCreator.getCurrentResourcePools();
    for(int id: rps.keySet()) {
      Network network = rps.get(id).getNetwork();
      try {
        network.close();
      } catch (IOException e) {
        //Cannot do anything about this.
      }
    }           
    rps.clear();
    //allow the sockets to become available again. 
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
