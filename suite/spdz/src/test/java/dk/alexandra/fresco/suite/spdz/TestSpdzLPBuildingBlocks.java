package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.lp.LPBuildingBlockTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzLPBuildingBlocks extends AbstractSpdzTest {

  @Test
  public void test_entering_variable_sequential() throws Exception {
    runTest(new LPBuildingBlockTests.TestEnteringVariable<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
    runTest(new LPBuildingBlockTests.TestBlandEnteringVariable<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

}

