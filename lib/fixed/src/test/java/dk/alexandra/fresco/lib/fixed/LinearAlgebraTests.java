package dk.alexandra.fresco.lib.fixed;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.collections.MatrixUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
          Application<Matrix<SFixed>, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            DRes<Matrix<DRes<SFixed>>> mat = FixedLinearAlgebra.using(root).input(input, 1);
            // unwrap and return result
            return () -> new MatrixUtils().unwrapMatrix(mat);
          };
          Matrix<SFixed> output = runApplication(testApplication);
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
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(root);
            DRes<Matrix<DRes<SFixed>>> closed = fixedLinearAlgebra.input(input, 1);
            DRes<Matrix<DRes<BigDecimal>>> opened = fixedLinearAlgebra.openMatrix(closed);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };
          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input.getHeight(); i++) {
            FixedTestUtils.assertEqual(output.getRow(i), input.getRow(i), 15);
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
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(root);
            DRes<Matrix<DRes<SFixed>>> closedA = fixedLinearAlgebra.input(a, 1);
            DRes<Matrix<DRes<SFixed>>> closedB = fixedLinearAlgebra.input(b, 1);
            DRes<Matrix<DRes<SFixed>>> res1 = fixedLinearAlgebra.add(closedA, closedB);
            DRes<Matrix<DRes<SFixed>>> res2 = fixedLinearAlgebra.add(a, closedB);
            DRes<Matrix<DRes<SFixed>>> res3 = fixedLinearAlgebra.add(b, closedA);
            DRes<Matrix<DRes<BigDecimal>>> open1 = fixedLinearAlgebra.openMatrix(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = fixedLinearAlgebra.openMatrix(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = fixedLinearAlgebra.openMatrix(res3);
            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };

          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < a.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              FixedTestUtils
                  .assertEqual(expected.getRow(i), output.get(j).getRow(i),
                      15);
            }
          }
        }
      };
    }
  }

  public static class TestMatrixSubtraction<ResourcePoolT extends ResourcePool>
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
          ArrayList<BigDecimal> rowC1 = new ArrayList<>(Arrays
              .asList(rowA1.get(0).subtract(rowB1.get(0)), rowA1.get(1).subtract(rowB1.get(1))));
          ArrayList<BigDecimal> rowC2 = new ArrayList<>(Arrays
              .asList(rowA2.get(0).subtract(rowB2.get(0)), rowA2.get(1).subtract(rowB2.get(1))));
          Matrix<BigDecimal> expected =
              new Matrix<>(2, 2, new ArrayList<>(Arrays.asList(rowC1, rowC2)));
          // define functionality to be tested
          Application<List<Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(root);
            DRes<Matrix<DRes<SFixed>>> closedA = fixedLinearAlgebra.input(a, 1);
            DRes<Matrix<DRes<SFixed>>> closedB = fixedLinearAlgebra.input(b, 1);
            DRes<Matrix<DRes<SFixed>>> res1 = fixedLinearAlgebra.sub(closedA, closedB);
            DRes<Matrix<DRes<SFixed>>> res2 = fixedLinearAlgebra.sub(a, closedB);
            DRes<Matrix<DRes<SFixed>>> res3 = fixedLinearAlgebra.sub(closedA, b);
            DRes<Matrix<DRes<BigDecimal>>> open1 = fixedLinearAlgebra.openMatrix(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = fixedLinearAlgebra.openMatrix(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = fixedLinearAlgebra.openMatrix(res3);
            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };

          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < a.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              FixedTestUtils
                  .assertEqual(expected.getRow(i), output.get(j).getRow(i),
                      15);
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
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(root);
            DRes<Matrix<DRes<SFixed>>> closedMatrix = fixedLinearAlgebra.input(matrix, 1);
            DRes<SFixed> closedScalar = FixedNumeric.using(root).input(s, 1);
            DRes<Matrix<DRes<SFixed>>> res1 = fixedLinearAlgebra
                .scale(closedScalar, closedMatrix);
            DRes<Matrix<DRes<SFixed>>> res2 = fixedLinearAlgebra.scale(s, closedMatrix);
            DRes<Matrix<DRes<SFixed>>> res3 = fixedLinearAlgebra
                .scale(closedScalar, matrix);
            DRes<Matrix<DRes<BigDecimal>>> open1 = fixedLinearAlgebra.openMatrix(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = fixedLinearAlgebra.openMatrix(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = fixedLinearAlgebra.openMatrix(res3);
            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };
          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              FixedTestUtils
                  .assertEqual(expected.getRow(i), output.get(j).getRow(i),
                      15);
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
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(root);
            DRes<Matrix<DRes<SFixed>>> closedMatrix = fixedLinearAlgebra.input(matrix, 1);
            DRes<Matrix<DRes<SFixed>>> closedArrayList = fixedLinearAlgebra
                .input(vector, 1);
            DRes<Matrix<DRes<SFixed>>> res1 = fixedLinearAlgebra
                .mult(closedMatrix, closedArrayList);
            DRes<Matrix<DRes<SFixed>>> res2 = fixedLinearAlgebra
                .mult(matrix, closedArrayList);
            DRes<Matrix<DRes<SFixed>>> res3 = fixedLinearAlgebra
                .mult(closedMatrix, vector);
            DRes<Matrix<DRes<BigDecimal>>> open1 = fixedLinearAlgebra.openMatrix(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = fixedLinearAlgebra.openMatrix(res2);
            DRes<Matrix<DRes<BigDecimal>>> open3 = fixedLinearAlgebra.openMatrix(res3);
            return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
                new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
          };
          List<Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              FixedTestUtils
                  .assertEqual(expected.getRow(i), output.get(j).getRow(i),
                      15);
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
          ArrayList<BigDecimal> vector = allOneArrayList(dimension, precision);
          ArrayList<BigDecimal> expected = new ArrayList<>(dimension);
          for (int i = 0; i < dimension; i++) {
            expected.add(BigDecimal.valueOf(dimension).setScale(1));
          }
          Application<List<ArrayList<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {

            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(root);
            DRes<Matrix<DRes<SFixed>>> closedMatrix = fixedLinearAlgebra.input(matrix, 1);
            DRes<ArrayList<DRes<SFixed>>> closedArrayList = fixedLinearAlgebra
                .input(vector, 1);
            DRes<ArrayList<DRes<SFixed>>> res1 =
                fixedLinearAlgebra.vectorMult(closedMatrix, closedArrayList);
            DRes<ArrayList<DRes<SFixed>>> res2 = fixedLinearAlgebra
                .vectorMult(matrix, closedArrayList);
            DRes<ArrayList<DRes<SFixed>>> res3 = fixedLinearAlgebra
                .vectorMult(closedMatrix, vector);
            DRes<ArrayList<DRes<BigDecimal>>> open1 = fixedLinearAlgebra.openArrayList(res1);
            DRes<ArrayList<DRes<BigDecimal>>> open2 = fixedLinearAlgebra.openArrayList(res2);
            DRes<ArrayList<DRes<BigDecimal>>> open3 = fixedLinearAlgebra.openArrayList(res3);
            return () -> Arrays.asList(
                open1.out().stream().map(x -> x.out())
                    .collect(Collectors.toCollection(ArrayList::new)),
                open2.out().stream().map(x -> x.out())
                    .collect(Collectors.toCollection(ArrayList::new)),
                open3.out().stream().map(x -> x.out())
                    .collect(Collectors.toCollection(ArrayList::new)));
          };
          List<ArrayList<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < matrix.getHeight(); i++) {
            for (int j = 0; j < output.size(); j++) {
              FixedTestUtils
                  .assertEqual(expected.get(i), output.get(j).get(i), 15);
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
          ArrayList<BigDecimal> vector = allOneArrayList(dimension - 1, precision);
          Application<List<ArrayList<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<ArrayList<DRes<SFixed>>> closedArrayList = FixedLinearAlgebra.using(root).input(vector, 1);
            FixedLinearAlgebra.using(root).vectorMult(matrix, closedArrayList);
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
          Application<List<ArrayList<BigDecimal>>, ProtocolBuilderNumeric> testApplication1 = root -> {
            DRes<Matrix<DRes<SFixed>>> closedMatrix = FixedLinearAlgebra.using(root).input(matrix1, 1);
            FixedLinearAlgebra.using(root).add(matrix2, closedMatrix);
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
          Application<List<ArrayList<BigDecimal>>, ProtocolBuilderNumeric> testApplication2 = root -> {
            DRes<Matrix<DRes<SFixed>>> closedMatrix = FixedLinearAlgebra.using(root).input(matrix1, 1);
            FixedLinearAlgebra.using(root).add(matrix3, closedMatrix);
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
          Application<List<ArrayList<BigDecimal>>, ProtocolBuilderNumeric> testApplication1 = root -> {
            DRes<Matrix<DRes<SFixed>>> closedMatrix = FixedLinearAlgebra.using(root).input(matrix1, 1);
            FixedLinearAlgebra.using(root).mult(matrix2, closedMatrix);
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

  public static class TestTransposeMatrix<ResourcePoolT extends ResourcePool>
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
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(root);
            DRes<Matrix<DRes<SFixed>>> closed = fixedLinearAlgebra.input(input, 1);
            DRes<Matrix<DRes<SFixed>>> transposed = fixedLinearAlgebra.transpose(closed);
            DRes<Matrix<DRes<BigDecimal>>> opened = fixedLinearAlgebra.openMatrix(transposed);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };
          Matrix<BigDecimal> output = runApplication(testApplication);
          System.out.println(output);
          for (int i = 0; i < input.getHeight(); i++) {
            FixedTestUtils
                .assertEqual(output.getColumn(i), input.getRow(i), 15);
          }
        }
      };
    }
  }

  private static ArrayList<BigDecimal> allOneArrayList(int dimension, int precision) {
    ArrayList<BigDecimal> vector = new ArrayList<>(dimension);
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
