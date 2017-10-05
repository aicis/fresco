package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.demo.helpers.ResourcePoolHelper;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
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


public class SetIntersectionDemo {

  private int noPlayers = 2;

  @Test
  public void dummyTest() throws Exception {
    // Generic configuration
    List<Integer> ports = new ArrayList<Integer>(noPlayers);
    for (int i = 1; i <= noPlayers; i++) {
      ports.add(9000 + i * 10);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>();
      ttc.netConf = netConf.get(playerId);

      // Protocol specific configuration
      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite =
          new DummyBooleanProtocolSuite();

      // The rest is generic configuration as well
      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new SequentialEvaluator<ResourcePoolImpl, ProtocolBuilderBinary>();
      ttc.sceConf = new TestSCEConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>(suite,
          NetworkingStrategy.KRYONET, evaluator, ttc.netConf, true);
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
  public void tinyTablesTest() throws Exception {
    // Generic configuration
    List<Integer> ports = new ArrayList<Integer>(noPlayers);
    for (int i = 1; i <= noPlayers; i++) {
      ports.add(9000 + i);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>();
      ttc.netConf = netConf.get(playerId);

      // Protocol specific configuration + suite
      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite =
          (ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary>) getTinyTablesPreproProtocolSuite(
              9000 + ttc.netConf.getMyId(), playerId);

      // More generic configuration
      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new SequentialEvaluator<ResourcePoolImpl, ProtocolBuilderBinary>();
      ttc.sceConf = new TestSCEConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>(suite,
          NetworkingStrategy.KRYONET, evaluator, ttc.netConf, true);
      conf.put(playerId, ttc);
    }

    // We run the preprocessing and save the resulting tinytables to disk
    this.setIntersectionDemo(conf);

    // Preprocessing is complete, now we configure a new instance of the
    // computation and run it
    netConf = TestConfiguration.getNetworkConfigurations(noPlayers, ports);
    conf = new HashMap<>();
    for (int playerId : netConf.keySet()) {
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>();
      ttc.netConf = netConf.get(playerId);

      // These 2 lines are protocol specific, the rest is generic configuration
      ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> suite =
          (ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary>) getTinyTablesProtocolSuite(
              ttc.netConf.getMyId());

      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new SequentialEvaluator<ResourcePoolImpl, ProtocolBuilderBinary>();
      ttc.sceConf = new TestSCEConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>(suite,
          NetworkingStrategy.KRYONET, evaluator, ttc.netConf, true);
      conf.put(playerId, ttc);
    }

    // Finally we run the processing phase and verify the result
    String[] result = this.setIntersectionDemo(conf);
    Assert.assertTrue(verifyResult(result));
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
      Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf)
          throws Exception {
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
                if (conf.netConf.getMyId() == 2) {
                  key = ByteArithmetic.toBoolean("00112233445566778899aabbccddeeff"); // 128-bit key
                  inputList = new int[] {2, 66, 112, 1123};
                } else if (conf.netConf.getMyId() == 1) {
                  key = ByteArithmetic.toBoolean("000102030405060708090a0b0c0d0e0f"); // 128-bit key
                  inputList = new int[] {1, 3, 66, 1123};
                }

                PrivateSetDemo app = new PrivateSetDemo(conf.netConf.getMyId(), key, inputList);

                List<List<Boolean>> psiResult = secureComputationEngine.runApplication(app,
                    ResourcePoolCreator.createResourcePool(conf.sceConf));
                System.out.println(
                    "Result Dimentions: " + psiResult.size() + ", " + psiResult.get(0).size());
                boolean[][] actualBoolean = new boolean[psiResult.size()][psiResult.get(0).size()];

                for (int j = 0; j < psiResult.size(); j++) {
                  for (int i = 0; i < psiResult.get(0).size(); i++) {
                    actualBoolean[j][i] = psiResult.get(j).get(i);
                  }
                  String actual = ByteArithmetic.toHex(actualBoolean[j]);
                  result[j] = actual;
                }
              }
            };
          }
        };
    TestThreadRunner.run(f, conf);
    ResourcePoolHelper.shutdown();
    return result;
  }

  private ProtocolSuite<?, ?> getTinyTablesPreproProtocolSuite(int myPort, int playerId) {
    TinyTablesPreproProtocolSuite config =
        new TinyTablesPreproProtocolSuite(playerId, getTinyTablesFile(playerId));
    return config;
  }

  private File getTinyTablesFile(int playerId) {
    String filename = "TinyTables_SetIntersection_" + playerId;
    return new File(filename);
  }

  private ProtocolSuite<?, ?> getTinyTablesProtocolSuite(int playerId)
      throws ClassNotFoundException, IOException {
    TinyTablesProtocolSuite config =
        new TinyTablesProtocolSuite(playerId, getTinyTablesFile(playerId));
    return config;
  }

}
