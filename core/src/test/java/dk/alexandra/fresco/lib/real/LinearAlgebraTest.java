package dk.alexandra.fresco.lib.real;

import static dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticRunner.run;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import org.junit.Test;

public class LinearAlgebraTest {

  @Test
  public void testCloseMatrix() {
    // input
    Matrix<BigDecimal> input = new Matrix<>(0, 0, new ArrayList<>());
    // functionality to be tested
    Matrix<SReal> output = run(root -> {
      // close inputs
      DRes<Matrix<DRes<SReal>>> mat = root.realLinAlg().input(input, 1);
      // unwrap and return result
      return () -> new MatrixUtils().unwrapMatrix(mat);
    }, 2);
    assertTrue(output.getRows().isEmpty());
  }

  @Test
  public void testCloseAndOpenMatrix() {
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
    Matrix<BigDecimal> output = run(root -> {
      DRes<Matrix<DRes<SReal>>> closed = root.realLinAlg().input(input, 1);
      DRes<Matrix<DRes<BigDecimal>>> opened = root.realLinAlg().openMatrix(closed);
      return () -> new MatrixUtils().unwrapMatrix(opened);
    }, 2);
    for (int i = 0; i < input.getHeight(); i++) {
      RealTestUtils.assertEqual(output.getRow(i), input.getRow(i), 15);
    }
  }

  @Test
  public void testMatrixAddition() {
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

    List<Matrix<BigDecimal>> output = run(root -> {
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
    }, 2);
    for (int i = 0; i < a.getHeight(); i++) {
      for (int j = 0; j < output.size(); j++) {
        RealTestUtils.assertEqual(expected.getRow(i), output.get(j).getRow(i), 15);
      }
    }
  }

  @Test
  public void testMatrixSubtraction() {
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

    List<Matrix<BigDecimal>> output = run(root -> {
      DRes<Matrix<DRes<SReal>>> closedA = root.realLinAlg().input(a, 1);
      DRes<Matrix<DRes<SReal>>> closedB = root.realLinAlg().input(b, 1);
      DRes<Matrix<DRes<SReal>>> res1 = root.realLinAlg().sub(closedA, closedB);
      DRes<Matrix<DRes<SReal>>> res2 = root.realLinAlg().sub(a, closedB);
      DRes<Matrix<DRes<SReal>>> res3 = root.realLinAlg().sub(closedA, b);
      DRes<Matrix<DRes<BigDecimal>>> open1 = root.realLinAlg().openMatrix(res1);
      DRes<Matrix<DRes<BigDecimal>>> open2 = root.realLinAlg().openMatrix(res2);
      DRes<Matrix<DRes<BigDecimal>>> open3 = root.realLinAlg().openMatrix(res3);
      return () -> Arrays.asList(new MatrixUtils().unwrapMatrix(open1),
              new MatrixUtils().unwrapMatrix(open2), new MatrixUtils().unwrapMatrix(open3));
    }, 2);
    for (int i = 0; i < a.getHeight(); i++) {
      for (int j = 0; j < output.size(); j++) {
        RealTestUtils.assertEqual(expected.getRow(i), output.get(j).getRow(i), 15);
      }
    }
  }

  @Test
  public void testMatrixScale() {
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
    List<Matrix<BigDecimal>> output = run(root -> {
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
    }, 2);
    for (int i = 0; i < matrix.getHeight(); i++) {
      for (int j = 0; j < output.size(); j++) {
        RealTestUtils.assertEqual(expected.getRow(i), output.get(j).getRow(i), 15);
      }
    }
  }

  @Test
  public void testMatrixMultiplication() {
    final int dimension = 50;
    final int precision = 4;

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
    List<Matrix<BigDecimal>> output = run(root -> {
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
    }, 2);
    for (int i = 0; i < matrix.getHeight(); i++) {
      for (int j = 0; j < output.size(); j++) {
        RealTestUtils.assertEqual(expected.getRow(i), output.get(j).getRow(i), 15);
      }
    }
  }

  @Test
  public void testMatrixOperate() {
    final int dimension = 50;
    final int precision = 4;

    Matrix<BigDecimal> matrix = allOneMatrix(dimension, dimension, precision);
    Vector<BigDecimal> vector = allOneVector(dimension, precision);
    Vector<BigDecimal> expected = new Vector<>(dimension);
    for (int i = 0; i < dimension; i++) {
      expected.add(BigDecimal.valueOf(dimension).setScale(1));
    }
    List<Vector<BigDecimal>> output = run(root -> {

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
    }, 2);
    for (int i = 0; i < matrix.getHeight(); i++) {
      for (int j = 0; j < output.size(); j++) {
        RealTestUtils.assertEqual(expected.get(i), output.get(j).get(i), 15);
      }
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void testVectorMultUnmatchedDimensions() {
    final int dimension = 10;
    final int precision = 4;

    Matrix<BigDecimal> matrix = allOneMatrix(dimension, dimension, precision);
    Vector<BigDecimal> vector = allOneVector(dimension - 1, precision);
    Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric> testApplication = root -> {
      DRes<Vector<DRes<SReal>>> closedVector = root.realLinAlg().input(vector, 1);
      root.realLinAlg().vectorMult(matrix, closedVector);
      return () -> null;
    };
    run(testApplication);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAdditionUnmatchedHeight() {
    final int dimension = 10;
    final int precision = 4;

    Matrix<BigDecimal> matrix1 = allOneMatrix(dimension, dimension, precision);
    Matrix<BigDecimal> matrix2 = allOneMatrix(dimension - 1, dimension, precision);
    run((Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric>) root -> {
      DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix1, 1);
      root.realLinAlg().add(matrix2, closedMatrix);
      return () -> null;
    });
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAdditionUnmatchedWidth() {
    final int dimension = 10;
    final int precision = 4;

    Matrix<BigDecimal> matrix1 = allOneMatrix(dimension, dimension, precision);
    Matrix<BigDecimal> matrix3 = allOneMatrix(dimension, dimension - 1, precision);
    run((Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric>) root -> {
      DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix1, 1);
      root.realLinAlg().add(matrix3, closedMatrix);
      return () -> null;
    });
  }

  @Test(expected=IllegalArgumentException.class)
  public void testMatrixMultUnmatchedDimensions() {
    final int dimension = 10;
    final int precision = 4;

    Matrix<BigDecimal> matrix1 = allOneMatrix(dimension, dimension, precision);
    Matrix<BigDecimal> matrix2 = allOneMatrix(dimension, dimension - 1, precision);
    run((Application<List<Vector<BigDecimal>>, ProtocolBuilderNumeric>) root -> {
      DRes<Matrix<DRes<SReal>>> closedMatrix = root.realLinAlg().input(matrix1, 1);
      root.realLinAlg().mult(matrix2, closedMatrix);
      return () -> null;
    });
  }

  @Test
  public void testTransposeMatrix() {
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
      DRes<Matrix<DRes<SReal>>> transposed = root.realLinAlg().transpose(closed);
      DRes<Matrix<DRes<BigDecimal>>> opened = root.realLinAlg().openMatrix(transposed);
      return () -> new MatrixUtils().unwrapMatrix(opened);
    };
    Matrix<BigDecimal> output = run(testApplication);
    for (int i = 0; i < input.getHeight(); i++) {
      RealTestUtils.assertEqual(output.getColumn(i), input.getRow(i), 15);
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
