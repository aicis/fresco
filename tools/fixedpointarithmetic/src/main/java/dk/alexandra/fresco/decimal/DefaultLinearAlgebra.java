package dk.alexandra.fresco.decimal;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class DefaultLinearAlgebra implements LinearAlgebra {

  private final ProtocolBuilderNumeric builder;
  private final RealNumericProvider provider;

  public DefaultLinearAlgebra(ProtocolBuilderNumeric builder, RealNumericProvider provider) {
    this.builder = builder;
    this.provider = provider;
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> input(Matrix<BigDecimal> a, int inputParty) {
    return builder.par(par -> {
      RealNumeric numeric = provider.apply(par);
      Matrix<DRes<SReal>> matrix =
          new Matrix<>(a.getHeight(), a.getWidth(), i -> new ArrayList<>(a.getRow(i).stream()
              .map(e -> numeric.numeric().input(e, inputParty)).collect(Collectors.toList())));
      return () -> matrix;
    });
  }

  @Override
  public DRes<Matrix<DRes<BigDecimal>>> open(DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      RealNumeric numeric = provider.apply(par);
      Matrix<DRes<BigDecimal>> matrix = new Matrix<>(a.out().getHeight(), a.out().getWidth(),
          i -> new ArrayList<>(a.out().getRow(i).stream().map(e -> numeric.numeric().open(e))
              .collect(Collectors.toList())));
      return () -> matrix;
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> add(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.par(par -> {
      return add(par, a.out(), b.out(),
          (scope, x) -> scope.numeric().add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.par(par -> {
      return add(par, a, b.out(), (scope, x) -> scope.numeric().add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, Matrix<BigDecimal> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b,
          (scope, x) -> scope.advanced().innerProductWithPublicPart(x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a, b.out(),
          (scope, x) -> scope.advanced().innerProductWithPublicPart(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b.out(),
          (scope, x) -> scope.advanced().innerProduct(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(BigDecimal s, DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(), (f, x) -> f.numeric().mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(), (f, x) -> f.numeric().mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, Matrix<BigDecimal> a) {
    return builder.par(par -> {
      return scale(par, s, a, (f, x) -> f.numeric().mult(x.getSecond(), x.getFirst()));
    });
  }

  /**
   * Add two matrices using the given builder and fixed point add operation.
   * 
   * @param builder
   * @param a
   * @param b
   * @param add
   * @return
   */
  private <A, B, C> DRes<Matrix<C>> add(ProtocolBuilderNumeric builder, Matrix<A> a, Matrix<B> b,
      BiFunction<RealNumeric, Pair<A, B>, C> add) {
    return builder.par(par -> {
      if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
        throw new IllegalArgumentException("Matrices must have same sizes - " + a.getWidth() + "x"
            + a.getHeight() + " != " + b.getWidth() + "x" + b.getHeight());
      }
      RealNumeric numeric = provider.apply(par);
      Matrix<C> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
        ArrayList<C> row = new ArrayList<>();
        for (int j = 0; j < a.getWidth(); j++) {
          row.add(add.apply(numeric, new Pair<>(a.getRow(i).get(j), b.getRow(i).get(j))));
        }
        return row;
      });
      return () -> result;
    });
  }

  /**
   * Calculate the product of two matrices using the given builder and arithmetic operations.
   * 
   * @param builder
   * @param a
   * @param b
   * @param mult
   * @param add
   * @return
   */
  private <A, B, C> DRes<Matrix<C>> mult(ProtocolBuilderNumeric builder, Matrix<A> a, Matrix<B> b,
      BiFunction<RealNumeric, Pair<List<A>, List<B>>, C> innerProduct) {
    return builder.par(par -> {

      if (a.getWidth() != b.getHeight()) {
        throw new IllegalArgumentException(
            "Matrice sizes does not match - " + a.getWidth() + " != " + b.getHeight());
      }

      RealNumeric numeric = provider.apply(par);
      Matrix<C> result = new Matrix<>(a.getHeight(), b.getWidth(), i -> {
        ArrayList<C> row = new ArrayList<>(b.getWidth());
        for (int j = 0; j < b.getWidth(); j++) {
          row.add(innerProduct.apply(numeric, new Pair<>(a.getRow(i), b.getColumn(j))));
        }
        return row;
      });
      return () -> result;
    });
  }

  /**
   * Scale the given matrix by a scalar using the given builder and multiplication operation.
   * 
   * @param builder
   * @param a
   * @param b
   * @param mult
   * @return
   */
  private <A, B, C> DRes<Matrix<C>> scale(ProtocolBuilderNumeric builder, A a, Matrix<B> b,
      BiFunction<RealNumeric, Pair<A, B>, C> mult) {
    return builder.par(par -> {
      RealNumeric numeric = provider.apply(par);
      Matrix<C> result = new Matrix<>(b.getHeight(), b.getWidth(), i -> new ArrayList<>(b.getRow(i)
          .stream().map(x -> mult.apply(numeric, new Pair<>(a, x))).collect(Collectors.toList())));
      return () -> result;
    });
  }

}
