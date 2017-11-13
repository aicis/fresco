package dk.alexandra.fresco.lib.conditional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.RowPairD;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionalSwapRowsTests {

  private static class TestSwapGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final BigInteger swapperOpen;
    final Pair<List<BigInteger>, List<BigInteger>> input;
    final Pair<List<BigInteger>, List<BigInteger>> expected;

    private TestSwapGeneric(BigInteger selectorOpen,
        Pair<List<BigInteger>, List<BigInteger>> expected,
        Pair<List<BigInteger>, List<BigInteger>> input) {
      this.swapperOpen = selectorOpen;
      this.expected = expected;
      this.input = input;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Pair<List<BigInteger>, List<BigInteger>>, ProtocolBuilderNumeric> testApplication =
              root -> {
                Collections collections = root.collections();
                DRes<List<DRes<SInt>>> closedLeft = collections.closeList(input.getFirst(), 1);
                DRes<List<DRes<SInt>>> closedRight = collections.closeList(input.getSecond(), 1);
                DRes<SInt> swapper = root.numeric().input(swapperOpen, 1);
                DRes<RowPairD<SInt, SInt>> swapped =
                    collections.swapIf(swapper, closedLeft, closedRight);
                DRes<RowPairD<BigInteger, BigInteger>> openSwapped =
                    collections.openRowPair(swapped);
                return () -> {
                  RowPairD<BigInteger, BigInteger> openSwappedOut = openSwapped.out();
                  List<BigInteger> leftRes = openSwappedOut.getFirst().out().stream().map(DRes::out)
                      .collect(Collectors.toList());
                  List<BigInteger> rightRes = openSwappedOut.getSecond().out().stream()
                      .map(DRes::out).collect(Collectors.toList());
                  return new Pair<>(leftRes, rightRes);
                };
              };
          Pair<List<BigInteger>, List<BigInteger>> output = runApplication(testApplication);
          assertThat(output, is(expected));
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestSwapGeneric<ResourcePoolT> testSwapYes() {
    Matrix<BigInteger> mat = new MatrixTestUtils().getInputMatrix(2, 3);
    Pair<List<BigInteger>, List<BigInteger>> input = new Pair<>(mat.getRow(0), mat.getRow(1));
    Pair<List<BigInteger>, List<BigInteger>> expected = new Pair<>(mat.getRow(1), mat.getRow(0));
    return new TestSwapGeneric<>(BigInteger.valueOf(1), expected, input);
  }

  public static <ResourcePoolT extends ResourcePool> TestSwapGeneric<ResourcePoolT> testSwapNo() {
    Matrix<BigInteger> mat = new MatrixTestUtils().getInputMatrix(2, 3);
    Pair<List<BigInteger>, List<BigInteger>> input = new Pair<>(mat.getRow(0), mat.getRow(1));
    Pair<List<BigInteger>, List<BigInteger>> expected = new Pair<>(mat.getRow(0), mat.getRow(1));
    return new TestSwapGeneric<>(BigInteger.valueOf(0), expected, input);
  }
}
