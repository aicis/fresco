package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class DefaultFixedLinearAlgebra implements FixedLinearAlgebra {

  private final ProtocolBuilderNumeric builder;

  DefaultFixedLinearAlgebra(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> input(Matrix<BigDecimal> a, int inputParty) {
    return builder.par(par -> {
      Matrix<DRes<SFixed>> matrix =
          new Matrix<>(a.getHeight(), a.getWidth(), i -> new ArrayList<>(a.getRow(i).stream()
              .map(e -> FixedNumeric.using(par).input(e, inputParty)).collect(Collectors.toList())));
      return () -> matrix;
    });
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> input(ArrayList<BigDecimal> a, int inputParty) {
    return builder.par(par -> {
      ArrayList<DRes<SFixed>> matrix = a.stream().map(e -> FixedNumeric.using(par).input(e, inputParty))
          .collect(Collectors.toCollection(ArrayList::new));
      return () -> matrix;
    });
  }

  @Override
  public DRes<Matrix<DRes<BigDecimal>>> openMatrix(DRes<Matrix<DRes<SFixed>>> a) {
    return builder.par(par -> {
      Matrix<DRes<BigDecimal>> matrix = new Matrix<>(a.out().getHeight(), a.out().getWidth(),
          i -> new ArrayList<>(a.out().getRow(i).stream().map(e -> FixedNumeric.using(par).open(e))
              .collect(Collectors.toList())));
      return () -> matrix;
    });
  }

  @Override
  public DRes<ArrayList<DRes<BigDecimal>>> openArrayList(DRes<ArrayList<DRes<SFixed>>> a) {
    return builder.par(par -> {
      ArrayList<DRes<BigDecimal>> vector = a.out().stream().map(e -> FixedNumeric.using(par).open(e))
          .collect(Collectors.toCollection(ArrayList::new));
      return () -> vector;
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> add(DRes<Matrix<DRes<SFixed>>> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.par(par -> {
      return entrywiseBinaryOperator(par, a.out(), b.out(),
          (builder, x) -> FixedNumeric.using(builder).add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.par(par -> {
      return entrywiseBinaryOperator(par, a, b.out(),
          (builder, x) -> FixedNumeric.using(builder).add(x.getFirst(), x.getSecond()));
    });
  }

  /**
   * Apply an operator taking two arguments in an entrywise fashion to the entries of two matrices
   * of equal dimensions.
   *
   * <p>
   * This can be used to generalize the implementations of operations such as matrix addition and
   * subtraction.
   * </p>
   *
   * @param builder the builder to be used for this computation
   * @param a matrix of type <code>A</code>
   * @param b matrix of type <code>B</code>
   * @param operator the operator which takes an element of type <code>A</code> and type
   *        <code>B</code> to give an element of type <code>C</code>
   * @return A matrix of type <code>C</code> which is the result of the entrywise application of the
   *         <code>operator</code> of the two matrices
   * @throws IllegalArgumentException if matrices <code>a</code> and <code>b</code> are not of eqaul
   *         dimensions.
   */
  private <A, B, C> DRes<Matrix<C>> entrywiseBinaryOperator(ProtocolBuilderNumeric builder,
      Matrix<A> a, Matrix<B> b, BiFunction<ProtocolBuilderNumeric, Pair<A, B>, C> operator) {
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
          row.add(operator.apply(par, new Pair<>(rowA.get(j), rowB.get(j))));
        }
        return row;
      });
      return () -> result;
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> sub(DRes<Matrix<DRes<SFixed>>> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.par(par -> {
      return entrywiseBinaryOperator(par, a.out(), b.out(),
          (builder, x) -> FixedNumeric.using(builder).sub(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> sub(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.par(par -> {
      return entrywiseBinaryOperator(par, a, b.out(),
          (builder, x) -> FixedNumeric.using(builder).sub(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> sub(DRes<Matrix<DRes<SFixed>>> a, Matrix<BigDecimal> b) {
    return builder.par(par -> {
      return entrywiseBinaryOperator(par, a.out(), b,
          (builder, x) -> FixedNumeric.using(builder).sub(x.getFirst(), x.getSecond()));
    });
  }



  @Override
  public DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a, Matrix<BigDecimal> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b, (scope, x) -> AdvancedFixedNumeric.using(scope)
          .innerProductWithPublicPart(x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a, b.out(), (builder, x) -> AdvancedFixedNumeric.using(builder)
          .innerProductWithPublicPart(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b.out(),
          (builder, x) -> AdvancedFixedNumeric.using(builder).innerProduct(x.getFirst(), x.getSecond()));
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
  public DRes<Matrix<DRes<SFixed>>> scale(BigDecimal s, DRes<Matrix<DRes<SFixed>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(),
          (builder, x) -> FixedNumeric.using(builder).mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, DRes<Matrix<DRes<SFixed>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(),
          (builder, x) -> FixedNumeric.using(builder).mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, Matrix<BigDecimal> a) {
    return builder.par(par -> {
      return scale(par, s, a,
          (builder, x) -> FixedNumeric.using(builder).mult(x.getSecond(), x.getFirst()));
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
  public DRes<ArrayList<DRes<SFixed>>> vectorMult(DRes<Matrix<DRes<SFixed>>> a,
      DRes<ArrayList<DRes<SFixed>>> v) {
    return builder.par(par -> {
      return vectorMult(par, a.out(), v.out(),
          (builder, x) -> AdvancedFixedNumeric.using(builder).innerProduct(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> vectorMult(DRes<Matrix<DRes<SFixed>>> a, ArrayList<BigDecimal> v) {
    return builder.par(par -> {
      return vectorMult(par, a.out(), v, (builder, x) -> AdvancedFixedNumeric.using(builder)
          .innerProductWithPublicPart(x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> vectorMult(Matrix<BigDecimal> a, DRes<ArrayList<DRes<SFixed>>> v) {
    return builder.par(par -> {
      return vectorMult(par, a, v.out(), (builder, x) -> AdvancedFixedNumeric.using(builder)
          .innerProductWithPublicPart(x.getFirst(), x.getSecond()));
    });
  }

  /**
   * Multiply a matrix to a vector using the given inner product operator.
   *
   * @param builder The builder to be used for this computation
   * @param a Matrix of type <code>A</code>
   * @param v ArrayList of type <code>B</code>
   * @param innerProductOperator An inner product operator which takes the inner product of a vector
   *        of type <code>A</code> and type <code>B</code> to produce a value of type <code>C</code>
   * @return A vector of type <code>C</code> which is the product of the matrix and the vector
   */
  private <A, B, C> DRes<ArrayList<C>> vectorMult(ProtocolBuilderNumeric builder, Matrix<A> a,
      ArrayList<B> v,
      BiFunction<ProtocolBuilderNumeric, Pair<List<A>, List<B>>, C> innerProductOperator) {
    return builder.par(par -> {

      if (a.getWidth() != v.size()) {
        throw new IllegalArgumentException(
            "Matrix and vector sizes does not match - " + a.getWidth() + " != " + v.size());
      }

      ArrayList<C> result =
          a.getRows().stream().map(r -> innerProductOperator.apply(par, new Pair<>(r, v)))
              .collect(Collectors.toCollection(ArrayList::new));
      return () -> result;
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> transpose(DRes<Matrix<DRes<SFixed>>> matrix) {
    return () -> transpose(matrix.out());
  }

  private <A> Matrix<A> transpose(Matrix<A> matrix) {
    Matrix<A> res = new Matrix<>(matrix.getWidth(), matrix.getHeight(),
        i -> new ArrayList<>(matrix.getColumn(i)));
    return res;
  }

}
