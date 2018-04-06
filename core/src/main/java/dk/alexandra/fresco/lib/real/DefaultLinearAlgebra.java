package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class DefaultLinearAlgebra implements RealLinearAlgebra {

  private final ProtocolBuilderNumeric builder;

  protected DefaultLinearAlgebra(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> input(Matrix<BigDecimal> a, int inputParty) {
    return builder.par(par -> {
      Matrix<DRes<SReal>> matrix =
          new Matrix<>(a.getHeight(), a.getWidth(), i -> new ArrayList<>(a.getRow(i).stream()
              .map(e -> par.realNumeric().input(e, inputParty)).collect(Collectors.toList())));
      return () -> matrix;
    });
  }

  @Override
  public DRes<Vector<DRes<SReal>>> input(Vector<BigDecimal> a, int inputParty) {
    return builder.par(par -> {
      Vector<DRes<SReal>> matrix = a.stream().map(e -> par.realNumeric().input(e, inputParty))
          .collect(Collectors.toCollection(Vector::new));
      return () -> matrix;
    });
  }

  @Override
  public DRes<Matrix<DRes<BigDecimal>>> openMatrix(DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      Matrix<DRes<BigDecimal>> matrix = new Matrix<>(a.out().getHeight(), a.out().getWidth(),
          i -> new ArrayList<>(a.out().getRow(i).stream().map(e -> par.realNumeric().open(e))
              .collect(Collectors.toList())));
      return () -> matrix;
    });
  }

  @Override
  public DRes<Vector<DRes<BigDecimal>>> openVector(DRes<Vector<DRes<SReal>>> a) {
    return builder.par(par -> {
      Vector<DRes<BigDecimal>> vector = a.out().stream().map(e -> par.realNumeric().open(e))
          .collect(Collectors.toCollection(Vector::new));
      return () -> vector;
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> add(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.par(par -> {
      return add(par, a.out(), b.out(),
          (builder, x) -> builder.realNumeric().add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.par(par -> {
      return add(par, a, b.out(),
          (builder, x) -> builder.realNumeric().add(x.getFirst(), x.getSecond()));
    });
  }

  /**
   * Add two matrices using the given builder and fixed point add operation.
   *
   * @param builder The builder to be used for this computation
   * @param a Matrix of type <code>A</code>
   * @param b Matrix of type <code>B</code>
   * @param addOperator The addition operator which adds an element of type <code>A</code> and type
   *        <code>B</code> to give an element of type <code>C</code>
   * @return A matrix of type <code>C</code> which is the sum of the two matrices
   */
  private <A, B, C> DRes<Matrix<C>> add(ProtocolBuilderNumeric builder, Matrix<A> a, Matrix<B> b,
      BiFunction<ProtocolBuilderNumeric, Pair<A, B>, C> addOperator) {
    return builder.par(par -> {
      if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
        throw new IllegalArgumentException("Matrices must have same sizes - " + a.getWidth() + "x"
            + a.getHeight() + " != " + b.getWidth() + "x" + b.getHeight());
      }
      Matrix<C> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
        ArrayList<C> row = new ArrayList<>();
        List<A> rowA = a.getRow(i);
        List<B> rowB = b.getRow(i);
        for (int j = 0; j < a.getWidth(); j++) {
          row.add(addOperator.apply(par, new Pair<>(rowA.get(j), rowB.get(j))));
        }
        return row;
      });
      return () -> result;
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, Matrix<BigDecimal> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b, (scope, x) -> scope.realAdvanced()
          .innerProductWithPublicPart(x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a, b.out(), (builder, x) -> builder.realAdvanced()
          .innerProductWithPublicPart(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b.out(),
          (builder, x) -> builder.realAdvanced().innerProduct(x.getFirst(), x.getSecond()));
    });
  }

  /**
   * Calculate the product of two matrices using the given builder and arithmetic operations.
   *
   * @param builder The builder to be used for this computation
   * @param a Matrix of type <code>A</code>
   * @param b Matrix of type <code>B</code>
   * @param innerProductOperator An inner product operator which takes the inner product of a vector
   *        of type <code>A</code> and type <code>B</code> to produce a value of type <code>C</code>
   * @return the product of the two matrices
   */
  private <A, B, C> DRes<Matrix<C>> mult(ProtocolBuilderNumeric builder, Matrix<A> a, Matrix<B> b,
      BiFunction<ProtocolBuilderNumeric, Pair<List<A>, List<B>>, C> innerProductOperator) {
    return builder.par(par -> {

      if (a.getWidth() != b.getHeight()) {
        throw new IllegalArgumentException(
            "Matrice sizes does not match - " + a.getWidth() + " != " + b.getHeight());
      }

      Matrix<C> result = new Matrix<>(a.getHeight(), b.getWidth(), i -> {
        ArrayList<C> row = new ArrayList<>(b.getWidth());
        List<A> rowA = a.getRow(i);
        for (int j = 0; j < b.getWidth(); j++) {
          row.add(innerProductOperator.apply(par, new Pair<>(rowA, b.getColumn(j))));
        }
        return row;
      });
      return () -> result;
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(BigDecimal s, DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(),
          (builder, x) -> builder.realNumeric().mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(),
          (builder, x) -> builder.realNumeric().mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, Matrix<BigDecimal> a) {
    return builder.par(par -> {
      return scale(par, s, a,
          (builder, x) -> builder.realNumeric().mult(x.getSecond(), x.getFirst()));
    });
  }

  /**
   * Scale the given matrix by a scalar using the given builder and multiplication operation.
   *
   * @param builder The builder to be used for this computation
   * @param a A value of type <code>A</code>
   * @param b Matrix of type <code>B</code>
   * @param multiplicationOperator An multiplication operator which multiplies an element of type
   *        <code>A</code> and type <code>B</code> to give an element of type <code>C</code>
   * @return the scaled matrix
   */
  private <A, B, C> DRes<Matrix<C>> scale(ProtocolBuilderNumeric builder, A a, Matrix<B> b,
      BiFunction<ProtocolBuilderNumeric, Pair<A, B>, C> multiplicationOperator) {
    return builder.par(par -> {
      Matrix<C> result = new Matrix<>(b.getHeight(), b.getWidth(),
          i -> b.getRow(i).stream().map(x -> multiplicationOperator.apply(par, new Pair<>(a, x)))
              .collect(Collectors.toCollection(ArrayList::new)));
      return () -> result;
    });
  }

  @Override
  public DRes<Vector<DRes<SReal>>> vectorMult(DRes<Matrix<DRes<SReal>>> a,
      DRes<Vector<DRes<SReal>>> v) {
    return builder.par(par -> {
      return vectorMult(par, a.out(), v.out(),
          (builder, x) -> builder.realAdvanced().innerProduct(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Vector<DRes<SReal>>> vectorMult(DRes<Matrix<DRes<SReal>>> a, Vector<BigDecimal> v) {
    return builder.par(par -> {
      return vectorMult(par, a.out(), v, (builder, x) -> builder.realAdvanced()
          .innerProductWithPublicPart(x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<Vector<DRes<SReal>>> vectorMult(Matrix<BigDecimal> a, DRes<Vector<DRes<SReal>>> v) {
    return builder.par(par -> {
      return vectorMult(par, a, v.out(), (builder, x) -> builder.realAdvanced()
          .innerProductWithPublicPart(x.getFirst(), x.getSecond()));
    });
  }

  /**
   * Multiply a matrix to a vector using the given inner product operator.
   *
   * @param builder The builder to be used for this computation
   * @param a Matrix of type <code>A</code>
   * @param v Vector of type <code>B</code>
   * @param innerProductOperator An inner product operator which takes the inner product of a vector
   *        of type <code>A</code> and type <code>B</code> to produce a value of type <code>C</code>
   * @return A vector of type <code>C</code> which is the product of the matrix and the vector
   */
  private <A, B, C> DRes<Vector<C>> vectorMult(ProtocolBuilderNumeric builder, Matrix<A> a,
      Vector<B> v,
      BiFunction<ProtocolBuilderNumeric, Pair<List<A>, List<B>>, C> innerProductOperator) {
    return builder.par(par -> {

      if (a.getWidth() != v.size()) {
        throw new IllegalArgumentException(
            "Matrix and vector sizes does not match - " + a.getWidth() + " != " + v.size());
      }

      Vector<C> result =
          a.getRows().stream().map(r -> innerProductOperator.apply(par, new Pair<>(r, v)))
              .collect(Collectors.toCollection(Vector::new));
      return () -> result;
    });
  }

}
