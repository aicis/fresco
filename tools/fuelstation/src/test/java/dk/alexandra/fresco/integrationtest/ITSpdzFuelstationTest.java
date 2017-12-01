package dk.alexandra.fresco.integrationtest;

import dk.alexandra.fresco.Application;
import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.arithmetic.MiMCTests;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.statistics.DeaSolver;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import dk.alexandra.fresco.suite.spdz.storage.rest.DataRestSupplierImpl;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {Application.class})
public class ITSpdzFuelstationTest {

  @LocalServerPort
  private int port;

  private void runTest(TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) throws Exception {

    // Since SCAPI currently does not work with ports > 9999 we use fixed
    // ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i);
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> suite = new SpdzProtocolSuite(150);

      ProtocolEvaluator<SpdzResourcePool, ProtocolBuilderNumeric> evaluator =
          new BatchedProtocolEvaluator<>(evalStrategy.getStrategy(), suite);
      DataSupplier supplier = new DataRestSupplierImpl(playerId, noOfParties, "http://localhost:" + port, 0);
      SpdzStorage store = new SpdzStorageImpl(supplier);
      TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<>(
              new SecureComputationEngineImpl<>(suite, evaluator),
              () -> {
                try {
                  return new SpdzResourcePoolImpl(playerId, noOfParties,
                    new Random(), new SecureRandom(), store);
                } catch (Exception e) {
                  throw new RuntimeException("Your system does not support the necessary hash function.", e);
                }
              },
              () -> new KryoNetNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_mimc_same_enc() throws Exception {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_division() throws Exception {
    runTest(new DivisionTests.TestSecretSharedDivision<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_dea() throws Exception {
    runTest(new RandomDataDeaTest<>(2, 1, 5, 1, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_mult_single() throws Exception {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

}
