package dk.alexandra.fresco.demo;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.TinyTablesDummyOtAdapter;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproResourcePool;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import dk.alexandra.fresco.tools.ot.base.Ot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;


public class TestPrivateSetDemo {
  private static final int COMPUTATIONAL_SECURITY = 128;
  private static final int STATISTICAL_SECURITY = 40;
  private final int noPlayers = 2;

  @Test
  public void dummyTest() {
    // Generic configuration
    List<Integer> ports = new ArrayList<>(noPlayers);
    for (int i = 1; i <= noPlayers; i++) {
      ports.add(9000 + i * 10);
    }
    Map<Integer, NetworkConfiguration> netConf =
        Network.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {

      // Protocol specific configuration
      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite =
          new DummyBooleanProtocolSuite();

      // The rest is generic configuration as well
      ProtocolEvaluator<ResourcePoolImpl> evaluator =
          new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), suite);
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadConfiguration<>(
              new SecureComputationEngineImpl<>(suite, evaluator),
              () -> new ResourcePoolImpl(playerId, noPlayers),
              () -> new AsyncNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    String[] result = this.setIntersectionDemo(conf);
    Assert.assertTrue(verifyResult(result));
  }


  /**
   * TinyTables requires a preprocessing phase as well as the actual computation phase.
   */
  @SuppressWarnings("unchecked")
  @Category(IntegrationTest.class)
  @Test
  public void tinyTablesTest() {
    // Generic configuration
    List<Integer> ports = Network.getFreePorts(2 * noPlayers);
    Map<Integer, NetworkConfiguration> netConf = Network.getNetworkConfigurations(
        noPlayers, ports.subList(0, noPlayers));
    Map<Integer, NetworkConfiguration> otNetConf = Network.getNetworkConfigurations(
        noPlayers, ports.subList(noPlayers, ports.size()));
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();

    for (int playerId : netConf.keySet()) {
      // Protocol specific configuration + suite
      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite =
          (ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary>) getTinyTablesPreproProtocolSuite();

      // More generic configuration
      ProtocolEvaluator<ResourcePoolImpl> evaluator =
          new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), suite);
      Ot baseOt = new TinyTablesDummyOtAdapter(Util.otherPlayerId(playerId), () -> new AsyncNetwork(
          otNetConf.get(playerId)));
      Drbg random = new AesCtrDrbg();
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadConfiguration<>(
              new SecureComputationEngineImpl<>(suite, evaluator),
              () -> new TinyTablesPreproResourcePool(playerId, noPlayers, baseOt, random,
                  COMPUTATIONAL_SECURITY, STATISTICAL_SECURITY, getTinyTablesFile(playerId)),
              () -> new AsyncNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }

    // We run the preprocessing and save the resulting tinytables to disk
    this.setIntersectionDemo(conf);

    {
      // Preprocessing is complete, now we configure a new instance of the
      // computation and run it
      Map<Integer, NetworkConfiguration> secondConf = Network
          .getNetworkConfigurations(noPlayers, ports.subList(0, noPlayers));
      conf = new HashMap<>();
      for (int playerId : secondConf.keySet()) {
        // These 2 lines are protocol specific, the rest is generic configuration
        ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite =
            (ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary>) getTinyTablesProtocolSuite(
                playerId);

        ProtocolEvaluator<ResourcePoolImpl> evaluator =
            new BatchedProtocolEvaluator<>(new BatchedStrategy<>(), suite);
        TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
            new TestThreadConfiguration<>(
                new SecureComputationEngineImpl<>(suite, evaluator),
                () -> new ResourcePoolImpl(playerId, noPlayers),
                () -> new AsyncNetwork(secondConf.get(playerId)));
        conf.put(playerId, ttc);
      }

      // Finally we run the processing phase and verify the result
      String[] result = this.setIntersectionDemo(conf);
      Assert.assertTrue(verifyResult(result));
    }
  }

  // ensure that test files are removed after the test ends.
  @After
  public void cleanup() {
    for (int i = 0; i < 2; i++) {
      File f = getTinyTablesFile(i);
      if (f.exists()) {
        f.delete();
      }
    }
  }


  private boolean verifyResult(String[] result) {
    // Expected ciphers
    String[] expected = {"c5cf1e6421d3302430b4c1e1258e23dc", "2f512cbe2004159f2a9f432aa23074fe",
        "a5bb0723dd40d10189b8e7e1ab383aa1", "687114568afa5846470e5a5e553c639d",
        "1f4e1f637a388bcb9984cf3d16c9243e", "a5bb0723dd40d10189b8e7e1ab383aa1",
        "52cd1dbeeb5f1dce0742aebf285e1472", "687114568afa5846470e5a5e553c639d"};

    for (int j = 0; j < expected.length; j++) {
      if (!expected[j].equals(result[j])) {
        return false;
      }
    }
    return true;
  }


  public String[] setIntersectionDemo(
      Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf) {
    String[] result = new String[8];
    TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderBinary> next() {
            return new TestThread<ResourcePoolImpl, ProtocolBuilderBinary>() {
              @Override
              public void test() throws Exception {
                Boolean[] key = null;
                int[] inputList = null;
                if (conf.getMyId() == 2) {
                  key = ByteAndBitConverter
                      .toBoolean("00112233445566778899aabbccddeeff"); // 128-bit key
                  inputList = new int[]{2, 66, 112, 1123};
                } else if (conf.getMyId() == 1) {
                  key = ByteAndBitConverter
                      .toBoolean("000102030405060708090a0b0c0d0e0f"); // 128-bit key
                  inputList = new int[]{1, 3, 66, 1123};
                }

                PrivateSetDemo app = new PrivateSetDemo(conf.getMyId(), key, inputList);

                List<List<Boolean>> psiResult = runApplication(app);
                System.out.println(
                    "Result Dimentions: " + psiResult.size() + ", " + psiResult.get(0).size());
                boolean[][] actualBoolean = new boolean[psiResult.size()][psiResult.get(0).size()];

                for (int j = 0; j < psiResult.size(); j++) {
                  for (int i = 0; i < psiResult.get(0).size(); i++) {
                    actualBoolean[j][i] = psiResult.get(j).get(i);
                  }
                  String actual = ByteAndBitConverter.toHex(actualBoolean[j]);
                  result[j] = actual;
                }
              }
            };
          }
        };
    TestThreadRunner.run(f, conf);
    return result;
  }

  private ProtocolSuite<?, ?> getTinyTablesPreproProtocolSuite() {
    TinyTablesPreproProtocolSuite config =
        new TinyTablesPreproProtocolSuite();
    return config;
  }

  private File getTinyTablesFile(int playerId) {
    String filename = "TinyTables_SetIntersection_" + playerId;
    return new File(filename);
  }

  private ProtocolSuite<?, ?> getTinyTablesProtocolSuite(int playerId) {
    TinyTablesProtocolSuite config =
        new TinyTablesProtocolSuite(playerId, getTinyTablesFile(playerId));
    return config;
  }

  @Test
  public void testPSICmdLine() throws Exception {
    Runnable p1 = () -> {
      try {
        PrivateSetDemo.main(
            new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyBool", "-in", "2,3,4,5,8,9,14", "-key",
                "abc123abc123abc123abc123abc123ab"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };

    Runnable p2 = () -> {
      try {
        PrivateSetDemo.main(
            new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
                "dummyBool", "-in", "2,3,4,6,7,12,14", "-key",
                "abc123abc123abc123abc123abc123ab"});
      } catch (IOException e) {
        throw new RuntimeException("Error", e);
      }
    };
    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPSICmdLine3Party() throws Exception {
    PrivateSetDemo.main(new String[]{
        "-i", "3", "-p", "1:localhost:8081",
        "-p", "2:localhost:8082", "-p", "3:localhost:8083", "-s", "dummyBool"});
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPSICmdLineBadKeyLength() throws Exception {
    PrivateSetDemo
        .main(new String[]{
            "-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
            "dummyBool", "-in", "2,3,4,6,7,12,14", "-key",
            "abc123abc123abc123abc123abc123"});
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPSICmdLineNoKey() throws Exception {
    PrivateSetDemo.main(new String[]{
        "-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s",
        "dummyBool", "-in", "2,3,4,6,7,12,14"});
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPSICmdLineNoInput() throws Exception {
    PrivateSetDemo.main(new String[]{
        "-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s", "dummyBool"});
    fail();
  }

}
