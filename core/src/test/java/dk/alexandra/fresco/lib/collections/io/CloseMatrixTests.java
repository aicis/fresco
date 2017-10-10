package dk.alexandra.fresco.lib.collections.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import java.math.BigInteger;
import java.util.ArrayList;

public class CloseMatrixTests {

  public static class TestCloseEmptyMatrix<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // input
          Matrix<BigInteger> input = new Matrix<>(0, 0, new ArrayList<>());
          // functionality to be tested
          Application<Matrix<SInt>, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            Collections collections = root.collections();
            DRes<Matrix<DRes<SInt>>> mat = collections.closeMatrix(input, 1);
            // unwrap and return result
            return () -> new MatrixUtils().unwrapMatrix(mat);
          };
          Matrix<SInt> output = runApplication(testApplication);
          assertTrue(output.getRows().isEmpty());
        }
      };
    }
  }

  public static class TestCloseAndOpenMatrix<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define input and output
          ArrayList<BigInteger> rowOne = new ArrayList<>();
          rowOne.add(BigInteger.valueOf(1));
          rowOne.add(BigInteger.valueOf(2));
          rowOne.add(BigInteger.valueOf(3));
          ArrayList<BigInteger> rowTwo = new ArrayList<>();
          rowTwo.add(BigInteger.valueOf(4));
          rowTwo.add(BigInteger.valueOf(5));
          rowTwo.add(BigInteger.valueOf(6));
          ArrayList<ArrayList<BigInteger>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          Matrix<BigInteger> input = new Matrix<>(2, 3, mat);

          // define functionality to be tested
          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = root -> {
            Collections collections = root.collections();
            DRes<Matrix<DRes<SInt>>> closed = collections.closeMatrix(input, 1);
            DRes<Matrix<DRes<BigInteger>>> opened = collections.openMatrix(closed);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigInteger> output = runApplication(testApplication);
          assertThat(output.getRows(), is(input.getRows()));
        }
      };
    }
  }
}
