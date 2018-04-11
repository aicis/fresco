package dk.alexandra.fresco.suite.spdz.batchednative;

import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.spdz.AbstractSpdzTest;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.Arrays;
import java.util.List;

public class BatchedNativeSpdzTest extends AbstractSpdzTest {

  private final List<Integer> partiesToRunFor = Arrays.asList(2, 3);

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f) {
    for (Integer noOfParties : partiesToRunFor) {
      runTest(f,
          EvaluationStrategy.NATIVE_BATCHED,
          PreprocessingStrategy.DUMMY,
          noOfParties,
          false);

    }
  }

}
