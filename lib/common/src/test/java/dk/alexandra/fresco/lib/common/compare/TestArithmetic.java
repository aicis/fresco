package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestArithmetic extends AbstractDummyArithmeticTest {

  @Test
  public void test_compareLt_Sequential() {
    runTest(new CompareTests.TestCompareLT<>(), new TestParameters());
  }

  @Test
  public void testCompareLtEdgeCasesSequential() {
    runTest(new CompareTests.TestCompareLTEdgeCases<>(), new TestParameters());
  }

  @Test
  public void test_compareEQ_Sequential() {
    runTest(new CompareTests.TestCompareEQ<>(), new TestParameters());
  }

  @Test
  public void test_compareFracEQ_Sequential() {
    runTest(new CompareTests.TestCompareFracEQ<>(), new TestParameters());
  }

  @Test
  public void testCompareEqEdgeCasesSequential() {
    runTest(new CompareTests.TestCompareEQEdgeCases<>(), new TestParameters());
  }
}
