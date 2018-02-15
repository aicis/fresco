package dk.alexandra.fresco.fixedpoint.basic;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinearAlgebraTests {

  public static class TestCloseFixedMatrix<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestCloseFixedMatrix(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          // input
          Matrix<BigDecimal> input = new Matrix<>(0, 0, new ArrayList<>());
          // functionality to be tested
          Application<Matrix<SReal>, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            RealNumeric fixed = provider.apply(root);
            DRes<Matrix<DRes<SReal>>> mat = fixed.linalg().input(input, 1);

            // unwrap and return result
            return () -> new MatrixUtils().unwrapMatrix(mat);
          };
          Matrix<SReal> output = runApplication(testApplication);
          assertTrue(output.getRows().isEmpty());
        }
      };
    }
  }

  public static class TestCloseAndOpenMatrix<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestCloseAndOpenMatrix(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define input and output
          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(1.1));
          rowOne.add(BigDecimal.valueOf(2.2));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(3.3));
          rowTwo.add(BigDecimal.valueOf(4.4));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(5.5));
          rowTwo.add(BigDecimal.valueOf(6.6));
          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> input = new Matrix<>(3, 2, mat);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            RealNumeric fixed = provider.apply(root);

            DRes<Matrix<DRes<SReal>>> closed = fixed.linalg().input(input, 1);

            DRes<Matrix<DRes<BigDecimal>>> opened = fixed.linalg().open(closed);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input.getHeight(); i++) {
            assertTrue(TestUtils.isEqual(output.getRow(i), input.getRow(i)));
          }
        }
      };
    }
  }

  public static class TestMatrixAddition<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestMatrixAddition(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          ArrayList<BigDecimal> aRow1 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.1), BigDecimal.valueOf(2.2)));
          ArrayList<BigDecimal> aRow2 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.3), BigDecimal.valueOf(4.2)));
          Matrix<BigDecimal> a = new Matrix<>(2, 2, new ArrayList<>(Arrays.asList(aRow1, aRow2)));

          ArrayList<BigDecimal> bRow1 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.9), BigDecimal.valueOf(2.9)));
          ArrayList<BigDecimal> bRow2 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.9), BigDecimal.valueOf(4.8)));
          Matrix<BigDecimal> b = new Matrix<>(2, 2, new ArrayList<>(Arrays.asList(bRow1, bRow2)));

          ArrayList<BigDecimal> cRow1 = new ArrayList<>(
              Arrays.asList(aRow1.get(0).add(bRow1.get(0)), aRow1.get(1).add(bRow1.get(1))));
          ArrayList<BigDecimal> cRow2 = new ArrayList<>(
              Arrays.asList(aRow2.get(0).add(bRow2.get(0)), aRow2.get(1).add(bRow2.get(1))));
          Matrix<BigDecimal> expected =
              new Matrix<>(2, 2, new ArrayList<>(Arrays.asList(cRow1, cRow2)));

          // define functionality to be tested
          Application<List<Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            RealNumeric fixed = provider.apply(root);

            DRes<Matrix<DRes<SReal>>> closedA = fixed.linalg().input(a, 1);
            DRes<Matrix<DRes<SReal>>> closedB = fixed.linalg().input(b, 1);

            DRes<Matrix<DRes<SReal>>> res1 = fixed.linalg().add(closedA, closedB);
            DRes<Matrix<DRes<SReal>>> res2 = fixed.linalg().add(a, closedB);
            DRes<Matrix<DRes<SReal>>> res3 = fixed.linalg().add(b, closedA);

            DRes<Matrix<DRes<BigDecimal>>> open1 = fixed.linalg().open(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = fixed.linalg().open(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = fixed.linalg().open(res3);

            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };

          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < a.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              assertTrue(TestUtils.isEqual(expected.getRow(i), output.get(j).getRow(i)));
            }
          }
        }
      };
    }
  }

  public static class TestMatrixScale<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestMatrixScale(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define input and output
          ArrayList<ArrayList<BigDecimal>> a = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.0), BigDecimal.valueOf(2.0))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.0), BigDecimal.valueOf(4.0)))));
          Matrix<BigDecimal> matrix = new Matrix<>(2, 2, a);

          BigDecimal s = BigDecimal.valueOf(0.1);

          ArrayList<ArrayList<BigDecimal>> c = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.4)))));
          Matrix<BigDecimal> expected = new Matrix<>(2, 2, c);

          // define functionality to be tested
          Application<List<Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {

            RealNumeric fixed = provider.apply(root);

            DRes<Matrix<DRes<SReal>>> closedMatrix = fixed.linalg().input(matrix, 1);
            DRes<SReal> closedScalar = fixed.numeric().input(s, 1);

            DRes<Matrix<DRes<SReal>>> res1 = fixed.linalg().scale(closedScalar, closedMatrix);
            DRes<Matrix<DRes<SReal>>> res2 = fixed.linalg().scale(s, closedMatrix);
            DRes<Matrix<DRes<SReal>>> res3 = fixed.linalg().scale(closedScalar, matrix);

            DRes<Matrix<DRes<BigDecimal>>> open1 = fixed.linalg().open(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = fixed.linalg().open(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = fixed.linalg().open(res3);

            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };

          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              assertTrue(TestUtils.isEqual(expected.getRow(i), output.get(j).getRow(i)));
            }
          }
        }
      };
    }
  }

  public static class TestMatrixMultiplication<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestMatrixMultiplication(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        final int n = 50;
        final int precision = 4;

        @Override
        public void test() throws Exception {
          // Matrix
          ArrayList<ArrayList<BigDecimal>> a = new ArrayList<>(n);
          for (int i = 0; i < n; i++) {
            ArrayList<BigDecimal> row = new ArrayList<>(n);
            for (int j = 0; j < n; j++) {
              row.add(BigDecimal.ONE.setScale(precision));
            }
            a.add(row);
          }
          Matrix<BigDecimal> matrix = new Matrix<>(n, n, a);

          // Vector
          ArrayList<ArrayList<BigDecimal>> b = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            ArrayList<BigDecimal> row = new ArrayList<>(n);
            row.add(BigDecimal.ONE.setScale(precision));
            b.add(row);
          }
          Matrix<BigDecimal> vector = new Matrix<>(n, 1, b);

          // Expected output
          ArrayList<ArrayList<BigDecimal>> e = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            ArrayList<BigDecimal> row = new ArrayList<>(n);
            row.add(BigDecimal.valueOf(n).setScale(1));
            e.add(row);
          }
          Matrix<BigDecimal> expected = new Matrix<>(n, 1, e);

          Application<List<Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            RealNumeric fixed = provider.apply(root);

            DRes<Matrix<DRes<SReal>>> closedMatrix = fixed.linalg().input(matrix, 1);
            DRes<Matrix<DRes<SReal>>> closedVector = fixed.linalg().input(vector, 1);

            DRes<Matrix<DRes<SReal>>> res1 = fixed.linalg().mult(closedMatrix, closedVector);
            DRes<Matrix<DRes<SReal>>> res2 = fixed.linalg().mult(matrix, closedVector);
            DRes<Matrix<DRes<SReal>>> res3 = fixed.linalg().mult(closedMatrix, vector);

            DRes<Matrix<DRes<BigDecimal>>> open1 = fixed.linalg().open(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = fixed.linalg().open(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = fixed.linalg().open(res3);

            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };

          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              assertTrue(TestUtils.isEqual(expected.getRow(i), output.get(j).getRow(i)));
            }
          }
        }
      };
    }
  }
}
