package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.lp.LpBuildingBlockTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzLPBuildingBlocks extends AbstractSpdzTest {

  @Test
  public void test_entering_variable_sequential() {
    runTest(new LpBuildingBlockTests.TestEnteringVariable<>(),
        PreprocessingStrategy.DUMMY, 2);
    runTest(new LpBuildingBlockTests.TestBlandEnteringVariable<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

}

