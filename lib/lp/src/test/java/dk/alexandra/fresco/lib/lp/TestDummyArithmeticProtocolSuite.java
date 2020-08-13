package dk.alexandra.fresco.lib.lp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.logging.arithmetic.ComparisonLoggerDecorator;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  // lib.lp
  @Test
  public void test_LpSolverEntering() {
    runTest(new LpBuildingBlockTests.TestEnteringVariable<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverBlandEntering() {
    runTest(new LpBuildingBlockTests.TestBlandEnteringVariable<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_LpTableauDebug() {
    runTest(new LpBuildingBlockTests.TestLpTableuDebug<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverDanzig() {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.DANZIG),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverDanzigTooManyIterations() {
    runTest(new LpBuildingBlockTests.TestLpSolverTooManyIterations<>(LPSolver.PivotRule.DANZIG),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverDanzigSmallerMod() {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.DANZIG),
        new TestParameters()
            .numParties(2)
            .field(new BigIntegerFieldDefinition(ModulusFinder.findSuitableModulus(128)))
            .maxBitLength(30)
            .fixedPointPrecesion(8)
            .performanceLogging(false));
  }


  @Test
  public void test_LpSolverBland() {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.BLAND),
        new TestParameters().numParties(2).performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_EQ), is((long) 33));
  }

  @Test
  public void test_LpSolverDebug() {
    runTest(new LpBuildingBlockTests.TestLpSolverDebug<>(), new TestParameters().numParties(2));
  }

}
