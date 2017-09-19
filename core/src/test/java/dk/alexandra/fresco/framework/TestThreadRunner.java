/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestThreadRunner {

  private final static Logger logger = LoggerFactory.getLogger(TestThreadRunner.class);

  private static final long MAX_WAIT_FOR_THREAD = 6000000;

  public abstract static class TestThread<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder>
      extends Thread {

    private boolean finished = false;

    protected TestThreadConfiguration<ResourcePoolT, Builder> conf;

    Throwable setupException;

    Throwable testException;

    Throwable teardownException;

    protected SecureComputationEngine<ResourcePoolT, Builder> secureComputationEngine;

    void setConfiguration(TestThreadConfiguration<ResourcePoolT, Builder> conf) {
      this.conf = conf;
    }

    protected <OutputT> OutputT runApplication(Application<OutputT, Builder> app)
        throws IOException {
      conf.resourcePool.getNetwork().connect(10000);
      return secureComputationEngine.runApplication(app, conf.resourcePool);
    }

    @Override
    public String toString() {
      return "TestThread(" + this.conf.netConf.getMyId() + ")";
    }

    @Override
    public void run() {
      try {
        if (conf.sceConf != null) {
          ProtocolSuite<ResourcePoolT, Builder> suite = conf.sceConf.getSuite();
          secureComputationEngine =
              SCEFactory.getSCEFromConfiguration(suite, conf.sceConf.getEvaluator());
        }
        setUp();
        runTest();
      } catch (Throwable e) {
        logger.error("" + this + " threw exception: ", e);
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
        logger.error("" + this + " threw exception during test:", e);
        Thread.currentThread().interrupt();
      } catch (AssertionError e) {
        this.testException = e;
        logger.error("Test assertion failed in " + this + ": ", e);
        Thread.currentThread().interrupt();
      }
    }

    private void runTearDown() {
      try {
        if (secureComputationEngine != null) {
          // Shut down SCE resources - does not include the resource pool.
          secureComputationEngine.shutdownSCE();
        }
        tearDown();
        finished = true;
      } catch (Exception e) {
        logger.error("" + this + " threw exception during tear down:", e);
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

  }


  /**
   * Container for all the configuration that one thread should have.
   */
  public static class TestThreadConfiguration<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

    public final NetworkConfiguration netConf;
    public final TestSCEConfiguration<ResourcePoolT, Builder> sceConf;
    public final ResourcePoolT resourcePool;

    public int getMyId() {
      return this.netConf.getMyId();
    }

    public TestThreadConfiguration(NetworkConfiguration netConf,
        TestSCEConfiguration<ResourcePoolT, Builder> sceConf, ResourcePoolT resourcePool) {
      super();
      this.netConf = netConf;
      this.sceConf = sceConf;
      this.resourcePool = resourcePool;
    }
  }


  public abstract static class TestThreadFactory<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

    public abstract TestThread<ResourcePoolT, Builder> next();
  }

  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> void run(
      TestThreadFactory<ResourcePoolT, Builder> f,
      Map<Integer, TestThreadConfiguration<ResourcePoolT, Builder>> confs) {
    int randSeed = 3457878;
    run(f, confs, randSeed);
  }

  private static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> void run(
      TestThreadFactory<ResourcePoolT, Builder> f,
      Map<Integer, TestThreadConfiguration<ResourcePoolT, Builder>> confs, int randSeed)
          throws TestFrameworkException {
    final Set<TestThread<ResourcePoolT, Builder>> threads = new HashSet<>();

    for (TestThreadConfiguration<ResourcePoolT, Builder> c : confs.values()) {
      TestThread<ResourcePoolT, Builder> t = f.next();
      t.setConfiguration(c);
      threads.add(t);
    }

    for (Thread t : threads) {
      t.start();
    }

    try {
      for (TestThread<ResourcePoolT, Builder> t : threads) {
        try {
          t.join(MAX_WAIT_FOR_THREAD);
        } catch (InterruptedException e) {
          throw new TestFrameworkException("Test was interrupted");
        }
        if (!t.finished) {
          logger.error("" + t + " timed out");
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
    } catch (Exception e) {
      // propagate up
      throw e;
    } finally {
      closeNetworks(confs);
    }
  }

  private static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> void closeNetworks(
      Map<Integer, TestThreadConfiguration<ResourcePoolT, Builder>> confs) {
    // Cleanup - shut down network in manually. All tests should use the NetworkCreator
    // in order for this to work, or manage the network themselves.

    for (int id : confs.keySet()) {
      ResourcePoolT rp = confs.get(id).resourcePool;
      if (rp != null) {
        Network network = rp.getNetwork();
        try {
          network.close();
        } catch (IOException e) {
          // Cannot do anything about this.
        }
      }
    }
  }
}
