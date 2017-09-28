package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.logging.PerformanceLogger.Flag;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class PerformanceLogTests extends AbstractSpdzTest {

  @Test
  public void test_log_network() throws Exception {
    runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2, Flag.ALL_OPTS);
  }
}
