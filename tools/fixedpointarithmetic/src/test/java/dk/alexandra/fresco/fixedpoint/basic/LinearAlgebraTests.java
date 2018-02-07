package dk.alexandra.fresco.fixedpoint.basic;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import dk.alexandra.fresco.fixedpoint.FixedNumeric;
import dk.alexandra.fresco.fixedpoint.LinearAlgebra;
import dk.alexandra.fresco.fixedpoint.SFixed;
import dk.alexandra.fresco.fixedpoint.SIntWrapperFixedNumeric;
import dk.alexandra.fresco.fixedpoint.SIntWrapperLinearAlgebra;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;

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
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 5);
            DRes<Matrix<DRes<SFixed>>> mat = la.input(input, 1);

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
          rowTwo.add(BigDecimal.valueOf(5.5));
          rowTwo.add(BigDecimal.valueOf(6.6));
          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> input = new Matrix<>(3, 2, mat);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 1);
            DRes<Matrix<DRes<SFixed>>> closed = la.input(input, 1);

            DRes<Matrix<DRes<BigDecimal>>> opened = la.open(closed);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input.getHeight(); i++) {
            assertTrue(output.getRow(i).equals(input.getRow(i)));
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
          // define input and output
          ArrayList<ArrayList<BigDecimal>> a = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.1), BigDecimal.valueOf(2.2))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.3), BigDecimal.valueOf(4.4)))));
          Matrix<BigDecimal> input1 = new Matrix<>(2, 2, a);

          ArrayList<ArrayList<BigDecimal>> b = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.2), BigDecimal.valueOf(2.3))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.4), BigDecimal.valueOf(4.8)))));
          Matrix<BigDecimal> input2 = new Matrix<>(2, 2, b);

          ArrayList<ArrayList<BigDecimal>> c = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(2.3), BigDecimal.valueOf(4.5))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(6.7), BigDecimal.valueOf(9.2)))));
          Matrix<BigDecimal> expected = new Matrix<>(2, 2, c);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 1);
            DRes<Matrix<DRes<SFixed>>> closed1 = la.input(input1, 1);
            DRes<Matrix<DRes<SFixed>>> closed2 = la.input(input2, 1);
            DRes<Matrix<DRes<SFixed>>> res = la.add(closed1, closed2);

            DRes<Matrix<DRes<BigDecimal>>> opened = la.open(res);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input1.getHeight(); i++) {
            assertTrue(expected.getRow(i).equals(output.getRow(i)));
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

        @Override
        public void test() throws Exception {
          // define input and output
          ArrayList<ArrayList<BigDecimal>> a = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.0), BigDecimal.valueOf(2.0))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(3.0), BigDecimal.valueOf(4.0)))));
          Matrix<BigDecimal> input1 = new Matrix<>(2, 2, a);

          ArrayList<ArrayList<BigDecimal>> b = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(2.0), BigDecimal.valueOf(0.0))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(1.0), BigDecimal.valueOf(2.0)))));
          Matrix<BigDecimal> input2 = new Matrix<>(2, 2, b);

          ArrayList<ArrayList<BigDecimal>> c = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(4.0), BigDecimal.valueOf(4.0))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(10.0), BigDecimal.valueOf(8.0)))));
          Matrix<BigDecimal> expected = new Matrix<>(2, 2, c);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 1);
            DRes<Matrix<DRes<SFixed>>> closed1 = la.input(input1, 1);
            DRes<Matrix<DRes<SFixed>>> closed2 = la.input(input2, 1);
            DRes<Matrix<DRes<SFixed>>> res = la.mult(closed1, closed2);

            DRes<Matrix<DRes<BigDecimal>>> opened = la.open(res);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input1.getHeight(); i++) {
            assertTrue(expected.getRow(i).equals(output.getRow(i)));
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
          Matrix<BigDecimal> input1 = new Matrix<>(2, 2, a);

          BigDecimal s = BigDecimal.valueOf(0.1);

          ArrayList<ArrayList<BigDecimal>> c = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.4)))));
          Matrix<BigDecimal> expected = new Matrix<>(2, 2, c);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 1);
            FixedNumeric fixed = new SIntWrapperFixedNumeric(root, 1);
            DRes<Matrix<DRes<SFixed>>> closedMatrix = la.input(input1, 1);
            DRes<SFixed> closedScalar = fixed.input(s, 1);
            DRes<Matrix<DRes<SFixed>>> res = la.scale(closedScalar, closedMatrix);

            DRes<Matrix<DRes<BigDecimal>>> opened = la.open(res);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input1.getHeight(); i++) {
            assertTrue(expected.getRow(i).equals(output.getRow(i)));
          }
        }
      };
    }
  }

  public static class TestMatrixScalePublic<ResourcePoolT extends ResourcePool>
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
          Matrix<BigDecimal> input1 = new Matrix<>(2, 2, a);

          BigDecimal s = BigDecimal.valueOf(0.1);

          ArrayList<ArrayList<BigDecimal>> c = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.4)))));
          Matrix<BigDecimal> expected = new Matrix<>(2, 2, c);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 1);
            FixedNumeric fixed = new SIntWrapperFixedNumeric(root, 1);
            DRes<SFixed> closedScalar = fixed.input(s, 1);
            DRes<Matrix<DRes<SFixed>>> res = la.scale(closedScalar, input1);

            DRes<Matrix<DRes<BigDecimal>>> opened = la.open(res);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input1.getHeight(); i++) {
            assertTrue(expected.getRow(i).equals(output.getRow(i)));
          }
        }
      };
    }
  }

  public static class TestMatrixPublicScale<ResourcePoolT extends ResourcePool>
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
          Matrix<BigDecimal> input1 = new Matrix<>(2, 2, a);

          BigDecimal s = BigDecimal.valueOf(0.1);

          ArrayList<ArrayList<BigDecimal>> c = new ArrayList<>(Arrays.asList(
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.2))),
              new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.4)))));
          Matrix<BigDecimal> expected = new Matrix<>(2, 2, c);

          // define functionality to be tested
          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = root -> {
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 1);
            DRes<Matrix<DRes<SFixed>>> closedMatrix = la.input(input1, 1);
            DRes<Matrix<DRes<SFixed>>> res = la.scale(s, closedMatrix);

            DRes<Matrix<DRes<BigDecimal>>> opened = la.open(res);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigDecimal> output = runApplication(testApplication);
          for (int i = 0; i < input1.getHeight(); i++) {
            assertTrue(expected.getRow(i).equals(output.getRow(i)));
          }
        }
      };
    }
  }

  public static class TestKnownMatrixMultiplication<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        final int n = 5;
        final int precision = 4;

        @Override
        public void test() throws Exception {
          // define input and output

          // Matrix
          ArrayList<ArrayList<BigDecimal>> a = new ArrayList<>(n);
          for (int i = 0; i < n; i++) {
            ArrayList<BigDecimal> row = new ArrayList<>(n);
            for (int j = 0; j < n; j++) {
              row.add(BigDecimal.ONE.setScale(precision));
            }
            a.add(row);
          }
          Matrix<BigDecimal> input1 = new Matrix<>(n, n, a);

          // Vector
          ArrayList<ArrayList<BigDecimal>> b = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            ArrayList<BigDecimal> row = new ArrayList<>(n);
            row.add(BigDecimal.ONE.setScale(precision));
            b.add(row);
          }
          Matrix<BigDecimal> input2 = new Matrix<>(n, 1, b);

          // Expected output
          ArrayList<ArrayList<BigDecimal>> e = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            ArrayList<BigDecimal> row = new ArrayList<>(n);
            row.add(BigDecimal.valueOf(n).setScale(1));
            e.add(row);
          }
          Matrix<BigDecimal> expected = new Matrix<>(n, 1, e);

          Application<Pair<Matrix<BigDecimal>, Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
            LinearAlgebra la = new SIntWrapperLinearAlgebra(root, 1);
            DRes<Matrix<DRes<SFixed>>> res1 = la.mult(input1, la.input(input2, 1));
            DRes<Matrix<DRes<SFixed>>> res2 = la.mult(la.input(input1, 1), input2);
            
            DRes<Matrix<DRes<BigDecimal>>> open1 = la.open(res1);
            DRes<Matrix<DRes<BigDecimal>>> open2 = la.open(res2);
            return () -> new Pair<>(new MatrixUtils().unwrapMatrix(open1), new MatrixUtils().unwrapMatrix(open2));
          };

          Pair<Matrix<BigDecimal>, Matrix<BigDecimal>> output = runApplication(testApplication);
          for (int i = 0; i < input1.getHeight(); i++) {
            assertTrue(expected.getRow(i).equals(output.getFirst().getRow(i)));
            assertTrue(expected.getRow(i).equals(output.getSecond().getRow(i)));
          }
        }
      };
    }
  }
}