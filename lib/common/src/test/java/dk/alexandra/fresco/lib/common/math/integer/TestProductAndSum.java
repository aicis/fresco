package dk.alexandra.fresco.lib.common.math.integer;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/** Test of {@link ProductSIntList} and {@link SumSIntList}. */
public class TestProductAndSum {

  private static final class TestCase {
    public final BigInteger expectedOutput;
    public final List<BigInteger> inputs;

    public TestCase(long expectedOutput, long... inputs) {
      this.expectedOutput = BigInteger.valueOf(expectedOutput);
      this.inputs =
          Arrays.stream(inputs)
              .mapToObj(BigInteger::valueOf)
              .collect(Collectors.toUnmodifiableList());
    }
  }

  private static final List<TestCase> TEST_CASES_SUM =
      List.of(
          new TestCase(0),
          new TestCase(123, 123),
          new TestCase(2, 1, 1),
          new TestCase(4, 2, 2),
          new TestCase(6, 3, 3),
          new TestCase(15, 1, 2, 4, 8),
          new TestCase(55, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

  private static final List<TestCase> TEST_CASES_PRODUCT =
      List.of(
          new TestCase(1),
          new TestCase(123, 123),
          new TestCase(1, 1, 1),
          new TestCase(4, 2, 2),
          new TestCase(9, 3, 3),
          new TestCase(64, 1, 2, 4, 8),
          new TestCase(3628800, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

  /** Test of {@link ProductSIntList}. */
  public static final class TestProduct<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          for (final TestCase testCase : TEST_CASES_PRODUCT) {
            // define functionality to be tested
            Application<BigInteger, ProtocolBuilderNumeric> testApplication =
                root -> {
                  List<DRes<SInt>> closed =
                      testCase.inputs.stream()
                          .map(root.numeric()::known)
                          .collect(Collectors.toUnmodifiableList());
                  DRes<SInt> result = AdvancedNumeric.using(root).product(closed);
                  DRes<BigInteger> open = root.numeric().open(result);
                  return () -> open.out();
                };
            BigInteger output = runApplication(testApplication);
            assertEquals(testCase.expectedOutput, output);
          }
        }
      };
    }
  }

  /** Test of {@link SumSIntList}. */
  public static final class TestSum<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          for (final TestCase testCase : TEST_CASES_SUM) {
            // define functionality to be tested
            Application<BigInteger, ProtocolBuilderNumeric> testApplication =
                root -> {
                  List<DRes<SInt>> closed =
                      testCase.inputs.stream()
                          .map(root.numeric()::known)
                          .collect(Collectors.toUnmodifiableList());
                  DRes<SInt> result = AdvancedNumeric.using(root).sum(closed);
                  DRes<BigInteger> open = root.numeric().open(result);
                  return () -> open.out();
                };
            BigInteger output = runApplication(testApplication);
            assertEquals(testCase.expectedOutput, output);
          }
        }
      };
    }
  }
}
