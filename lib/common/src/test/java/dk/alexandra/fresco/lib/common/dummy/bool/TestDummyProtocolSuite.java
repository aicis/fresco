package dk.alexandra.fresco.lib.common.dummy.bool;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.common.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.common.collections.sort.CollectionsSortingTests;
import dk.alexandra.fresco.lib.common.collections.sort.CollectionsSortingTests.TestOddEvenMerge;
import dk.alexandra.fresco.lib.common.compare.CompareTests;
import dk.alexandra.fresco.lib.common.math.bool.add.AddTests;
import dk.alexandra.fresco.lib.common.math.bool.log.LogTests;
import dk.alexandra.fresco.lib.common.math.bool.mult.MultTests;
import dk.alexandra.fresco.logging.binary.BinaryLoggingDecorator;
import org.junit.Test;

/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestDummyProtocolSuite extends AbstractDummyBooleanTest {

  // Basic tests for boolean suites
  @Test
  public void test_basic_logic() {
    runTest(new BasicBooleanTests.TestInput<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestInputDifferentSender<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        true, 2);
    runTest(new BasicBooleanTests.TestXOR<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestAND<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestNOT<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestRandomBit<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
        true);

    assertThat(performanceLoggers.get(1).get(2).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_XOR), is((long) 4));
    assertThat(performanceLoggers.get(1).get(3).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_AND), is((long) 4));
    assertThat(performanceLoggers.get(1).get(5).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_RANDOM), is((long) 1));
  }

  // lib.math.bool
  @Test
  public void test_One_Bit_Half_Adder() {
    runTest(new AddTests.TestOnebitHalfAdder<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_One_Bit_Full_Adder() {
    runTest(new AddTests.TestOnebitFullAdder<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Adder() {
    runTest(new AddTests.TestFullAdder<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_BitIncrementAdder() {
    runTest(new AddTests.TestBitIncrement<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Mult() {
    runTest(new MultTests.TestBinaryMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_basic_logic_all_in_one() {
    runTest(new BasicBooleanTests.TestBasicProtocols<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_comparison() {
    runTest(new ComparisonBooleanTests.TestGreaterThan<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_comparison_unequal_length() {
    runTest(new ComparisonBooleanTests.TestGreaterThanUnequalLength<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_equality() {
    runTest(new ComparisonBooleanTests.TestEquality<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  // collections.sort

  @Test
  public void test_Uneven_Odd_Even_Merge_2_parties() {
    runTest(new TestOddEvenMerge<>(false),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Uneven_Odd_Even_Merge_presorted_2_parties() {
    runTest(new TestOddEvenMerge<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Keyed_Compare_And_Swap_2_parties() {
    runTest(new CollectionsSortingTests.TestKeyedCompareAndSwap<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Compare_And_Swap() {
    runTest(new CompareTests.CompareAndSwapTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Log_Nice() {
    runTest(new LogTests.TestLogNice<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }
}
