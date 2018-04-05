package dk.alexandra.fresco.lib.real;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import java.util.Vector;
import java.util.stream.Collectors;

public class LinearAlgebraTests {

  public static class TestCloseFixedMatrix<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

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
            DRes<Matrix<DRes<SReal>>> mat = root.realLinAlg().input(input, 1);
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
          rowThree.add(BigDecimal.valueOf(5.5));
          rowThree.add(BigDecimal.valueOf(6.6));
          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> input = new Matrix<>(3, 2, mat);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<Matrix<DRes<SReal>>> closed = root.realLinAlg().input(input, 1);
            DRes<Matrix<DRes<BigDecimal>>> opened = root.realLinAlg().openMatrix(closed);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };
          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input.getHeight(); i++) {
            RealTestUtils.assertEqual(output.getRow(i), input.getRow(i), 15);
          }
        }
      };
    }
  }

  public static class TestMatrixAddition<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          ArrayList<BigDecimal> rowA1 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.1), BigDecimal.valueOf(2.2)));
          ArrayList<BigDecimal> rowA2 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.3), BigDecimal.valueOf(4.2)));
          Matrix<BigDecimal> a = new Matrix<>(2, 2, new ArrayList<>(Arrays.asList(rowA1, rowA2)));
          ArrayList<BigDecimal> rowB1 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.9), BigDecimal.valueOf(2.9)));
          ArrayList<BigDecimal> rowB2 =
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.9), BigDecimal.valueOf(4.8)));
          Matrix<BigDecimal> b = new Matrix<>(2, 2, new ArrayList<>(Arrays.asList(rowB1, rowB2)));
          ArrayList<BigDecimal> rowC1 = new ArrayList<>(
              Arrays.asList(rowA1.get(0).add(rowB1.get(0)), rowA1.get(1).add(rowB1.get(1))));
          ArrayList<BigDecimal> rowC2 = new ArrayList<>(
              Arrays.asList(rowA2.get(0).add(rowB2.get(0)), rowA2.get(1).add(rowB2.get(1))));
          Matrix<BigDecimal> expected =
              new Matrix<>(2, 2, new ArrayList<>(Arrays.asList(rowC1, rowC2)));
          // define functionality to be tested
          Application<List<Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<Matrix<DRes<SReal>>> closedA = root.realLinAlg().input(a, 1);
            DRes<Matrix<DRes<SReal>>> closedB = root.realLinAlg().input(b, 1);
            DRes<Matrix<DRes<SReal>>> res1 = root.realLinAlg().add(closedA, closedB);
            DRes<Matrix<DRes<SReal>>> res2 = root.realLinAlg().add(a, closedB);
            DRes<Matrix<DRes<SReal>>> res3 = root.realLinAlg().add(b, closedA);
            DRes<Matrix<DRes<BigDecimal>>> open1 = root.realLinAlg().openMatrix(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = root.realLinAlg().openMatrix(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = root.realLinAlg().openMatrix(res3);
            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };

          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < a.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              RealTestUtils.assertEqual(expected.getRow(i), output.get(j).getRow(i), 15);
            }
          }
        }
      };
    }
  }

  public static class TestMatrixScale<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

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
            DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix, 1);
            DRes<SReal> closedScalar = root.realNumeric().input(s, 1);
            DRes<Matrix<DRes<SReal>>> res1 = root.realLinAlg().scale(closedScalar, closedMatrix);
            DRes<Matrix<DRes<SReal>>> res2 = root.realLinAlg().scale(s, closedMatrix);
            DRes<Matrix<DRes<SReal>>> res3 = root.realLinAlg().scale(closedScalar, matrix);
            DRes<Matrix<DRes<BigDecimal>>> open1 = root.realLinAlg().openMatrix(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = root.realLinAlg().openMatrix(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = root.realLinAlg().openMatrix(res3);
            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };
          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              RealTestUtils.assertEqual(expected.getRow(i), output.get(j).getRow(i), 15);
            }
          }
        }
      };
    }
  }

  public static class TestMatrixMultiplication<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        final int dimension = 50;
        final int precision = 4;

        @Override
        public void test() throws Exception {
          Matrix<BigDecimal> matrix = allOneMatrix(dimension, dimension, precision);
          Matrix<BigDecimal> vector = allOneMatrix(dimension, 1, precision);
          // Expected output
          ArrayList<ArrayList<BigDecimal>> e = new ArrayList<>();
          for (int i = 0; i < dimension; i++) {
            ArrayList<BigDecimal> row = new ArrayList<>(dimension);
            row.add(BigDecimal.valueOf(dimension).setScale(1));
            e.add(row);
          }
          Matrix<BigDecimal> expected = new Matrix<>(dimension, 1, e);
          Application<List<Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix, 1);
            DRes<Matrix<DRes<SReal>>> closedVector = root.realLinAlg().input(vector, 1);
            DRes<Matrix<DRes<SReal>>> res1 = root.realLinAlg().mult(closedMatrix, closedVector);
            DRes<Matrix<DRes<SReal>>> res2 = root.realLinAlg().mult(matrix, closedVector);
            DRes<Matrix<DRes<SReal>>> res3 = root.realLinAlg().mult(closedMatrix, vector);
            DRes<Matrix<DRes<BigDecimal>>> open1 = root.realLinAlg().openMatrix(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = root.realLinAlg().openMatrix(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = root.realLinAlg().openMatrix(res3);
            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };
          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              RealTestUtils.assertEqual(expected.getRow(i), output.get(j).getRow(i), 15);
            }
          }
        }
      };
    }
  }

  public static class TestMatrixOperate<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        final int dimension = 50;
        final int precision = 4;

        @Override
        public void test() throws Exception {
          Matrix<BigDecimal> matrix = allOneMatrix(dimension, dimension, precision);
          Vector<BigDecimal> vector = allOneVector(dimension, precision);
          Vector<BigDecimal> expected = new Vector<>(dimension);
          for (int i = 0; i < dimension; i++) {
            expected.add(BigDecimal.valueOf(dimension).setScale(1));
          }
          Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {

            DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix, 1);
            DRes<Vector<DRes<SReal>>> closedVector = root.realLinAlg().input(vector, 1);
            DRes<Vector<DRes<SReal>>> res1 =
                root.realLinAlg().vectorMult(closedMatrix, closedVector);
            DRes<Vector<DRes<SReal>>> res2 = root.realLinAlg().vectorMult(matrix, closedVector);
            DRes<Vector<DRes<SReal>>> res3 = root.realLinAlg().vectorMult(closedMatrix, vector);
            DRes<Vector<DRes<BigDecimal>>> open1 = root.realLinAlg().openVector(res1);
            DRes<Vector<DRes<BigDecimal>>> open2 = root.realLinAlg().openVector(res2);
            DRes<Vector<DRes<BigDecimal>>> open3 = root.realLinAlg().openVector(res3);
            return () -> Arrays.asList(
                open1.out().stream().map(x -> x.out())
                    .collect(Collectors.toCollection(Vector::new)),
                open2.out().stream().map(x -> x.out())
                    .collect(Collectors.toCollection(Vector::new)),
                open3.out().stream().map(x -> x.out())
                    .collect(Collectors.toCollection(Vector::new)));
          };
          List<Vector<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              RealTestUtils.assertEqual(expected.get(i), output.get(j).get(i), 15);
            }
          }
        }
      };
    }
  }

  public static class TestVectorMultUnmatchedDimensions<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        final int dimension = 10;
        final int precision = 4;

        @Override
        public void test() throws Exception {
          Matrix<BigDecimal> matrix = allOneMatrix(dimension, dimension, precision);
          Vector<BigDecimal> vector = allOneVector(dimension - 1, precision);
          Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<Vector<DRes<SReal>>> closedVector = root.realLinAlg().input(vector, 1);
            root.realLinAlg().vectorMult(matrix, closedVector);
            return () -> null;
          };
          try {
            runApplication(testApplication);
            fail("Expected IllegalArgumentException");
          } catch (RuntimeException e) {
            if (e.getCause().getClass() != IllegalArgumentException.class) {
              throw e;
            }
          }
        }
      };
    }
  }

  public static class TestAdditionUnmatchedDimensions<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        final int dimension = 10;
        final int precision = 4;

        @Override
        public void test() throws Exception {
          Matrix<BigDecimal> matrix1 = allOneMatrix(dimension, dimension, precision);
          Matrix<BigDecimal> matrix2 = allOneMatrix(dimension - 1, dimension, precision);
          Matrix<BigDecimal> matrix3 = allOneMatrix(dimension, dimension - 1, precision);
          Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric> testApplication1 = root -> {
            DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix1, 1);
            root.realLinAlg().add(matrix2, closedMatrix);
            return () -> null;
          };
          try {
            runApplication(testApplication1);
            fail("Expected IllegalArgumentException");
          } catch (RuntimeException e) {
            if (e.getCause().getClass() != IllegalArgumentException.class) {
              throw e;
            } else {
              // Success - Ignore the exception
            }
          }
          Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric> testApplication2 = root -> {
            DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix1, 1);
            root.realLinAlg().add(matrix3, closedMatrix);
            return () -> null;
          };
          try {
            runApplication(testApplication2);
            fail("Expected IllegalArgumentException");
          } catch (RuntimeException e) {
            if (e.getCause().getClass() != IllegalArgumentException.class) {
              throw e;
            } else {
              // Success - Ignore the exception
            }
          }
        }
      };
    }
  }

  public static class TestMatrixMultUnmatchedDimensions<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        final int dimension = 10;
        final int precision = 4;

        @Override
        public void test() throws Exception {
          Matrix<BigDecimal> matrix1 = allOneMatrix(dimension, dimension, precision);
          Matrix<BigDecimal> matrix2 = allOneMatrix(dimension, dimension - 1, precision);
          Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric> testApplication1 = root -> {
            DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix1, 1);
            root.realLinAlg().mult(matrix2, closedMatrix);
            return () -> null;
          };
          try {
            runApplication(testApplication1);
            fail("Expected IllegalArgumentException");
          } catch (RuntimeException e) {
            if (e.getCause().getClass() != IllegalArgumentException.class) {
              throw e;
            } else {
              // Success - Ignore the exception
            }
          }
        }
      };
    }
  }

  private static Vector<BigDecimal> allOneVector(int dimension, int precision) {
    Vector<BigDecimal> vector = new Vector<>(dimension);
    for (int i = 0; i < dimension; i++) {
      vector.add(BigDecimal.ONE.setScale(precision));
    }
    return vector;
  }


  private static Matrix<BigDecimal> allOneMatrix(int height, int width, int precision) {
    ArrayList<ArrayList<BigDecimal>> a = new ArrayList<>(height);
    for (int i = 0; i < height; i++) {
      ArrayList<BigDecimal> row = new ArrayList<>(width);
      for (int j = 0; j < width; j++) {
        row.add(BigDecimal.ONE.setScale(precision));
      }
      a.add(row);
    }
    Matrix<BigDecimal> matrix = new Matrix<>(height, width, a);
    return matrix;
  }

}
