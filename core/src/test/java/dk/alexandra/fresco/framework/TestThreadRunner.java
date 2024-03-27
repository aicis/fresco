package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestThreadRunner {

  private final static Logger logger = LoggerFactory.getLogger(TestThreadRunner.class);

  private static final long MAX_WAIT_FOR_THREAD = 6000000;

  public abstract static class TestThread<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder>
      extends Thread {

    protected TestThreadConfiguration<ResourcePoolT, Builder> conf;

    Throwable setupException;

    Throwable testException;

    Throwable teardownException;

    void setConfiguration(TestThreadConfiguration<ResourcePoolT, Builder> conf) {
      this.conf = conf;
    }

    protected FieldDefinition getFieldDefinition() {
      ResourcePoolT resourcePool = conf.getResourcePool();
      if (resourcePool instanceof NumericResourcePool) {
        return ((NumericResourcePool) resourcePool).getFieldDefinition();
      }
      return null;
    }

    protected <OutputT> OutputT runApplication(Application<OutputT, Builder> app) {
      return conf.sce.runApplication(app, conf.getResourcePool(), conf.getNetwork());
    }

    @Override
    public String toString() {
      return "TestThread(" + this.conf.resourcePool + ")";
    }

    @Override
    public void run() {
      try {
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
        if (conf.network != null) {
          if (conf.network instanceof Closeable) {
            try {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ignored) {
              }
              ((Closeable) conf.network).close();
            } catch (IOException ignored) {
              // Cannot do anything about this.
            }
          }
        }
      } catch (AssertionError e) {
        this.testException = e;
        logger.error("Test assertion failed in " + this + ": ", e);
      }
    }

    private void runTearDown() {
      try {
        // Shut down SCE resources - does not include the resource pool.
        if (conf.sce != null) {
          conf.sce.close();
        }
        tearDown();
      } catch (Exception e) {
        logger.error("" + this + " threw exception during tear down:", e);
        this.teardownException = e;
        Thread.currentThread().interrupt();
      }
    }

    public void setUp() {
      // Override this if test fixture setup needed.
    }

    public void tearDown() {
      // Override this if actions needed to tear down test fixture.
    }

    public abstract void test() throws Exception;

  }


  /**
   * Container for all the configuration that one thread should have.
   */
  public static class TestThreadConfiguration<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

    public final SecureComputationEngine<ResourcePoolT, Builder> sce;
    private final Supplier<ResourcePoolT> resourcePoolSupplier;
    private final Supplier<Network> networkSupplier;
    private ResourcePoolT resourcePool;
    private Network network;

    public int getMyId() {
      return this.getResourcePool().getMyId();
    }

    public ResourcePoolT getResourcePool() {
      if (resourcePool == null) {
        resourcePool = resourcePoolSupplier.get();
      }
      return resourcePool;
    }

    public TestThreadConfiguration(SecureComputationEngine<ResourcePoolT, Builder> sce,
        Supplier<ResourcePoolT> resourcePoolSupplier,
        Supplier<Network> networkSupplier) {
      super();
      this.sce = sce;
      this.resourcePoolSupplier = resourcePoolSupplier;
      this.networkSupplier = networkSupplier;
    }

    public Network getNetwork() {
      if (network == null) {
        network = networkSupplier.get();
      }
      return network;
    }
  }


  public abstract static class TestThreadFactory<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

    public abstract TestThread<ResourcePoolT, Builder> next();
  }

  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> void run(
      TestThreadFactory<ResourcePoolT, Builder> f,
      Map<Integer, TestThreadConfiguration<ResourcePoolT, Builder>> confs) {
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
      long iteration = 0;
      while (!threads.isEmpty() && iteration * 1000 < MAX_WAIT_FOR_THREAD) {
        for (Iterator<TestThread<ResourcePoolT, Builder>> iterator = threads.iterator();
            iterator.hasNext(); ) {
          TestThread<ResourcePoolT, Builder> t = iterator.next();
          try {
            iteration++;
            t.join(1000);
            if (!t.isAlive()) {
              iterator.remove();
            }
          } catch (InterruptedException e) {
            throw new TestFrameworkException("Test was interrupted", e);
          }
          if (t.setupException != null) {
            throw new TestFrameworkException(t + " threw exception in setup", t.setupException);
          } else if (t.testException != null) {
            throw new TestFrameworkException(t + " threw exception in test",
                t.testException);
          } else if (t.teardownException != null) {
            throw new TestFrameworkException(t + " threw exception in teardown", t.setupException);
          }
        }
      }
      if (!threads.isEmpty()) {
        logger.error(f + ": Test timed out");
        throw new TestFrameworkException(f + ": Test timed out");
      }

    } finally {
      closeNetworks(confs);
    }
  }

  private static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> void closeNetworks(
      Map<Integer, TestThreadConfiguration<ResourcePoolT, Builder>> confs) {
    // Cleanup - shut down network in manually. All tests should use the NetworkCreator
    // in order for this to work, or manage the network themselves.

    for (int id : confs.keySet()) {
      Network network = confs.get(id).network;
      if (network != null) {
        if (network instanceof Closeable) {
          try {
            ((Closeable) network).close();
          } catch (IOException e) {
            // Cannot do anything about this.
          }
        }
      }
    }
  }
}
