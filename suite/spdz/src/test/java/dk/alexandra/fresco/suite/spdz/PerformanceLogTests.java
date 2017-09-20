package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.PerformanceLogger.Flag;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.List;
import org.junit.Test;

public class PerformanceLogTests extends AbstractSpdzTest {

  @Test
  public void test_log_network() throws Exception {
    List<PerformanceLogger> pls =
        runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
            NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2, Flag.ALL_OPTS);
    for (PerformanceLogger pl : pls) {
      pl.printPerformanceLog();
    }
  }
}
