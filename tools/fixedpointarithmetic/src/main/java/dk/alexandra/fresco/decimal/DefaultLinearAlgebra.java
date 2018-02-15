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

/**
 * Basic interface for fixed point numeric applications.
 */
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
      ArrayList<ArrayList<DRes<SReal>>> rows = new ArrayList<>(a.getHeight());
      BasicRealNumeric fixed = provider.apply(par).numeric();
      for (ArrayList<BigDecimal> row : a.getRows()) {
        rows.add(new ArrayList<>(
            row.stream().map(e -> fixed.input(e, inputParty)).collect(Collectors.toList())));
      }
      return () -> new Matrix<>(a.getHeight(), a.getWidth(), rows);
    });
  }

  @Override
  public DRes<Matrix<DRes<BigDecimal>>> open(DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      ArrayList<ArrayList<DRes<BigDecimal>>> rows = new ArrayList<>(a.out().getHeight());
      BasicRealNumeric fixed = provider.apply(par).numeric();
      for (ArrayList<DRes<SReal>> row : a.out().getRows()) {
        rows.add(
            new ArrayList<>(row.stream().map(e -> fixed.open(e)).collect(Collectors.toList())));
      }
      return () -> new Matrix<>(a.out().getHeight(), a.out().getWidth(), rows);
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> add(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.par(par -> {
      return add(par, a.out(), b.out(), (f, x) -> f.add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.par(par -> {
      return add(par, a, b.out(), (f, x) -> f.add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, Matrix<BigDecimal> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b, (f, x) -> provider.apply(f).advanced()
          .innerProductWithPublicPart(x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a, b.out(), (f, x) -> provider.apply(f).advanced()
          .innerProductWithPublicPart(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b.out(),
          (f, x) -> provider.apply(f).advanced().innerProduct(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(BigDecimal s, DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(), (f, x) -> f.mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, DRes<Matrix<DRes<SReal>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(), (f, x) -> f.mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, Matrix<BigDecimal> a) {
    return builder.par(par -> {
      return scale(par, s, a, (f, x) -> f.mult(x.getSecond(), x.getFirst()));
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
  private <A> DRes<Matrix<DRes<SReal>>> add(ProtocolBuilderNumeric builder, Matrix<A> a,
      Matrix<DRes<SReal>> b, BiFunction<BasicRealNumeric, Pair<A, DRes<SReal>>, DRes<SReal>> add) {
    return builder.par(par -> {
      if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
        throw new IllegalArgumentException("Matrices must have same sizes - " + a.getWidth() + "x"
            + a.getHeight() + " != " + b.getWidth() + "x" + b.getHeight());
      }
      BasicRealNumeric fixed = provider.apply(par).numeric();
      ArrayList<ArrayList<DRes<SReal>>> rows = new ArrayList<>(a.getHeight());
      for (int j = 0; j < a.getHeight(); j++) {
        ArrayList<DRes<SReal>> row = new ArrayList<>();
        for (int i = 0; i < a.getWidth(); i++) {
          row.add(add.apply(fixed, new Pair<>(a.getRow(j).get(i), b.getRow(j).get(i))));
        }
        rows.add(row);
      }
      return () -> new Matrix<>(a.getHeight(), a.getWidth(), rows);
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
  private <A, B> DRes<Matrix<DRes<SReal>>> mult(ProtocolBuilderNumeric builder, Matrix<A> a,
      Matrix<B> b,
      BiFunction<ProtocolBuilderNumeric, Pair<List<A>, List<B>>, DRes<SReal>> innerProduct) {
    return builder.par(par -> {

      if (a.getWidth() != b.getHeight()) {
        throw new IllegalArgumentException(
            "Matrice sizes does not match - " + a.getWidth() + " != " + b.getHeight());
      }

      ArrayList<ArrayList<DRes<SReal>>> rows = new ArrayList<>(a.getHeight());
      for (int i = 0; i < a.getHeight(); i++) {
        ArrayList<DRes<SReal>> row = new ArrayList<>(b.getWidth());
        for (int j = 0; j < b.getWidth(); j++) {
          row.add(innerProduct.apply(par, new Pair<>(a.getRow(i), b.getColumn(j))));
        }
        rows.add(row);
      }
      return () -> new Matrix<>(a.getHeight(), b.getWidth(), rows);
    });
  }

  /**
   * Scale the given matrix by a scalar using the given builder and multiplication operation.
   * 
   * @param builder
   * @param s
   * @param a
   * @param mult
   * @return
   */
  private <S, A> DRes<Matrix<DRes<SReal>>> scale(ProtocolBuilderNumeric builder, S s, Matrix<A> a,
      BiFunction<BasicRealNumeric, Pair<S, A>, DRes<SReal>> mult) {
    return builder.par(par -> {
      RealNumeric fixed = provider.apply(par);
      ArrayList<ArrayList<DRes<SReal>>> rows = new ArrayList<>(a.getHeight());
      for (ArrayList<A> row : a.getRows()) {
        rows.add(new ArrayList<>(row.stream()
            .map(x -> mult.apply(fixed.numeric(), new Pair<>(s, x))).collect(Collectors.toList())));
      }
      return () -> new Matrix<>(a.getHeight(), a.getWidth(), rows);
    });
  }
  
}
