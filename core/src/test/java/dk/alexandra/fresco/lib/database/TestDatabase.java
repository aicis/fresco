/**
 *
 */
package dk.alexandra.fresco.lib.database;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.suite.dummy.bool.DummyConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mortenvchristiansen
 */
public class TestDatabase {

  private static final int noOfParties = 2;

  private void runTest(TestThreadFactory f, EvaluationStrategy evalStrategy,
      StorageStrategy storageStrategy) throws Exception {
    Level logLevel = Level.FINE;
    Reporter.init(logLevel);

    // Since SCAPI currently does not work with ports > 9999 we use fixed
    // ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<Integer>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i);
    }

    Map<Integer, NetworkConfiguration> netConf = TestConfiguration
        .getNetworkConfigurations(noOfParties, ports, logLevel);
    Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
    for (int playerId : netConf.keySet()) {
      TestThreadConfiguration ttc = new TestThreadConfiguration();
      ttc.netConf = netConf.get(playerId);

      // This fixes parameters, e.g., security parameter 80 is always
      // used.
      // To run tests with varying parameters, do as in the BGW case with
      // different thresholds.
      boolean useSecureConnection = false; // No tests of secure
      // connection
      // here.
      int noOfVMThreads = 3;
      int noOfThreads = 3;
      ProtocolEvaluator evaluator = EvaluationStrategy
          .fromEnum(evalStrategy);
      dk.alexandra.fresco.framework.sce.resources.storage.Storage storage = null;
      switch (storageStrategy) {
        case IN_MEMORY:
          storage = inMemStore;
          break;
        default:
          throw new RuntimeException();
      }
      ttc.sceConf = new TestSCEConfiguration(new DummyConfiguration(), null, evaluator,
          noOfThreads, noOfVMThreads, ttc.netConf, storage,
          useSecureConnection);
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private static InMemoryStorage inMemStore = new InMemoryStorage();

  /**
   * Makes sure that the preprocessed data exists in the storage's used in
   * this test class.
   */
  public static void initStorage() {
    Reporter.init(Level.INFO);
    dk.alexandra.fresco.framework.sce.resources.storage.Storage[] storages = new dk.alexandra.fresco.framework.sce.resources.storage.Storage[]{
        inMemStore};
  }

  @Test
  @Ignore
  public void test_findDuplicatesOne() throws Exception {
    runTest(new EliminateDuplicatesTests.TestFindDuplicatesOne(),
        EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
  }


  @Test
  @Ignore
  public void test_findDuplicatesTwo() throws Exception {
    runTest(new EliminateDuplicatesTests.TestFindDuplicatesTwo(),
        EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
  }

  @Test
  @Ignore
  public void test_verticalJoin() throws Exception {
    runTest(new EliminateDuplicatesTests.TestVerticalJoin(),
        EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
  }

}
