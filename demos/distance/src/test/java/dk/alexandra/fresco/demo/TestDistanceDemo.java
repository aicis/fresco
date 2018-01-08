package dk.alexandra.fresco.demo;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.DummyDataSupplierImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class TestDistanceDemo {

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) throws Exception {
    // Since SCAPI currently does not work with ports > 9999 we use fixed
    // ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      SpdzProtocolSuite protocolSuite = new SpdzProtocolSuite(150);

      ProtocolEvaluator<SpdzResourcePool, ProtocolBuilderNumeric> evaluator =
          new BatchedProtocolEvaluator<>(evalStrategy.getStrategy(), protocolSuite);
      
      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(
              new SecureComputationEngineImpl<>(protocolSuite, evaluator),
              () -> createResourcePool(playerId, noOfParties),
              () -> new KryoNetNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private SpdzResourcePool createResourcePool(int myId, int size) {
    SpdzStorage store;
    store = new SpdzStorageImpl(new DummyDataSupplierImpl(myId, size));
    try {
      return new SpdzResourcePoolImpl(myId, size, new HmacDrbg(), store);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Your system does not support the necessary hash function.", e);
    }
  }

  @Test
  public void testDistance() throws Exception {
    final TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f =
        new TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<SpdzResourcePool, ProtocolBuilderNumeric> next() {
            return new TestThread<SpdzResourcePool, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                int x, y;
                if (conf.getMyId() == 1) {
                  x = 10;
                  y = 10;
                } else {
                  x = 20;
                  y = 15;
                }
                System.out.println("Running with x: " + x + ", y: " + y);
                DistanceDemo distDemo = new DistanceDemo(conf.getMyId(), x, y);
                BigInteger bigInteger = runApplication(distDemo);
                double distance = bigInteger.doubleValue();
                distance = Math.sqrt(distance);
                Assert.assertEquals(11.1803, distance, 0.0001);
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
        DistanceDemo.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic", "-x", "10", "-y", "10"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };

    Runnable p2 = () -> {
      try {
        DistanceDemo.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyArithmetic", "-x", "20", "-y", "15"});
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
        DistanceDemo.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "10", "-y", "10"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };

    Runnable p2 = () -> {
      try {
        DistanceDemo.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082",
                "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "20", "-y", "15"});
      } catch (IOException e) {
        throw new RuntimeException("Communication error");
      }
    };
    
    Runnable p3 = () -> {
      try {
        DistanceDemo.main(
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
  
  @Test(expected=IllegalArgumentException.class)
  public void testDistanceCmdLine3PartyWithInput() throws Exception {
    DistanceDemo.main(
        new String[]{"-i", "3", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "20", "-y", "15"});
    fail();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testDistanceCmdLine3PartyWithInputX() throws Exception {
    DistanceDemo.main(
        new String[]{"-i", "3", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "20"});
    fail();
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testDistanceCmdLine3PartyWithInputY() throws Exception {
    DistanceDemo.main(
        new String[]{"-i", "3", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-y", "15"});
    fail();
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testDistanceCmdLine2PartyWithNoInputX() throws Exception {
    DistanceDemo.main(
        new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-y", "15"});
    fail();
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testDistanceCmdLine2PartyWithNoInputY() throws Exception {
    DistanceDemo.main(
        new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-p", "3:localhost:8083", "-s", "dummyArithmetic", "-x", "20"});
    fail();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testDistanceCmdLine2PartyWithNoInput() throws Exception {
    DistanceDemo.main(
        new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", 
            "-p", "3:localhost:8083", "-s", "dummyArithmetic"});
    fail();
  }
  
}
