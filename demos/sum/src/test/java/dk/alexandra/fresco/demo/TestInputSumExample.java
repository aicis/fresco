package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.Test;

public class TestInputSumExample {

  @SuppressWarnings("unchecked")
  private static <ResourcePoolT extends ResourcePool> void runTest(
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> test, boolean dummy, int n) {
    List<Integer> ports = NetworkUtil.getFreePorts(n);
    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int i : netConf.keySet()) {
      ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> suite;

      Supplier<ResourcePoolT> resourcePool;
      if (dummy) {
        BigInteger mod = new BigInteger(
            "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
        suite =
            (ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric>) new DummyArithmeticProtocolSuite(
                mod, 150, 16);
        resourcePool = () -> (ResourcePoolT) new DummyArithmeticResourcePoolImpl(i, n,
            mod, BigInt::fromBytes);
      } else {
        suite = (ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric>) new SpdzProtocolSuite(150);
        resourcePool = () -> {
          try {
            return (ResourcePoolT) new SpdzResourcePoolImpl(i, n, new SpdzOpenedValueStoreImpl(),
                new SpdzDummyDataSupplier(i, n), new AesCtrDrbg(new byte[32]));
          } catch (Exception e) {
            throw new RuntimeException("Your system does not support the necessary hash function.",
                e);
          }
        };
      }
      TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<>(
              new SecureComputationEngineImpl<>(suite,
                  new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), suite)),
              resourcePool, () -> createNetwork(netConf.get(i)));
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);
  }

  private static Network createNetwork(
      NetworkConfiguration networkConfiguration) {
    return new AsyncNetwork(networkConfiguration);
  }

  @Test
  public <ResourcePoolT extends ResourcePool> void testInput() {
    final TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> f =
        new TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
              @Override
              public void test() {
                new InputSumExample()
                    .runApplication(conf.sce, conf.getResourcePool(), conf.getNetwork());
              }
            };
          }
        };
    runTest(f, false, 3);
  }

  @Test
  public <ResourcePoolT extends ResourcePool> void testInput_dummy() {
    final TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> f =
        new TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
              @Override
              public void test() {
                new InputSumExample()
                    .runApplication(conf.sce, conf.getResourcePool(), conf.getNetwork());
              }
            };
          }
        };
    runTest(f, true, 3);
  }

  @Test
  public void testInputCmdLine() throws Exception {
    Runnable p1 = () -> {
      try {
        InputSumExample.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic"});
      } catch (IOException e) {
        System.exit(-1);
      }
    };

    Runnable p2 = () -> {
      try {
        InputSumExample.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic"});
      } catch (IOException e) {
        System.exit(-1);
      }
    };
    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
