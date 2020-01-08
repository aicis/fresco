package dk.alexandra.fresco.demo.equals;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class TestEqualsDemo {

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) {
    List<Integer> ports = NetworkUtil.getFreePorts(noOfParties);
    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      SpdzProtocolSuite protocolSuite = new SpdzProtocolSuite(150);

      ProtocolEvaluator<SpdzResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(evalStrategy.getStrategy(), protocolSuite);

      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(
              new SecureComputationEngineImpl<>(protocolSuite, evaluator),
              () -> createResourcePool(playerId, noOfParties),
              () -> new SocketNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private SpdzResourcePool createResourcePool(int myId, int size) {
    BigInteger modulus = ModulusFinder.findSuitableModulus(512);
    return new SpdzResourcePoolImpl(myId, size, new SpdzOpenedValueStoreImpl(),
        new SpdzDummyDataSupplier(myId, size,
            new BigIntegerFieldDefinition(modulus), modulus),
        AesCtrDrbg::new);
  }

  @Test
  public void testEquals() {
    final TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f =
        new TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<SpdzResourcePool, ProtocolBuilderNumeric> next() {
            return new TestThread<SpdzResourcePool, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                int x;
                if (conf.getMyId() == 1) {
                  x = 11;
                } else {
                  x = 11;
                }
                System.out.println("Running with x: " + x);
                EqualsDemo equalsDemo = new EqualsDemo(conf.getMyId(), x);
                BigInteger equals = runApplication(equalsDemo);
                Assert.assertEquals(equals, BigInteger.ONE);
              }
            };
          }
        };
    runTest(f, EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testNotEquals() {
    final TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f =
            new TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric>() {
              @Override
              public TestThread<SpdzResourcePool, ProtocolBuilderNumeric> next() {
                return new TestThread<SpdzResourcePool, ProtocolBuilderNumeric>() {
                  @Override
                  public void test() throws Exception {
                    int x;
                    if (conf.getMyId() == 1) {
                      x = 10;
                    } else {
                      x = 20;
                    }
                    System.out.println("Running with x: " + x);
                    EqualsDemo equalsDemo = new EqualsDemo(conf.getMyId(), x);
                    BigInteger equals = runApplication(equalsDemo);
                    Assert.assertEquals(equals, BigInteger.ZERO);
                  }
                };
              }
            };
    runTest(f, EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testDistanceFromCmdLine() throws Exception {
    Runnable p1 = () -> {
      try {
        EqualsDemo.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic", "-x", "10"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };

    Runnable p2 = () -> {
      try {
        EqualsDemo.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic", "-x", "20"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };
    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }

  @Test
  public void testDistanceFromCmdLineWithMascot() throws Exception {
    Runnable p1 = () -> {
      try {
        EqualsDemo.main(
                new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                        "spdz", "-D", "spdz.preprocessingStrategy=MASCOT", "-x", "10"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };

    Runnable p2 = () -> {
      try {
        EqualsDemo.main(
                new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                        "spdz", "-D", "spdz.preprocessingStrategy=MASCOT", "-x", "10"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };
    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }

  @Test
  public void testDistanceFromCmdLine3Party() throws Exception {
    Runnable p1 = () -> {
      try {
        EqualsDemo.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "10"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };

    Runnable p2 = () -> {
      try {
        EqualsDemo.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "20"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };

    Runnable p3 = () -> {
      try {
        EqualsDemo.main(
            new String[]{"-i", "3", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyArithmetic"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };

    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    Thread t3 = new Thread(p3);
    t1.start();
    t2.start();
    t3.start();
    t1.join();
    t2.join();
    t3.join();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEqualsCmdLine3PartyWithInput() throws Exception {
    EqualsDemo.main(
        new String[]{"-i", "3", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
            "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "20"});
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEqualsCmdLine2PartyWithNoInput() throws Exception {
    EqualsDemo.main(
        new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
            "-p", "3:localhost:8083", "-s", "dummyArithmetic"});
    fail();
  }

}
