package dk.alexandra.fresco.fixedpoint;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;

public class SIntWrapperLinearAlgebra implements LinearAlgebra {

  private ProtocolBuilderNumeric builder;
  private int precision;

  public SIntWrapperLinearAlgebra(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
  }

  private BasicFixedNumeric getFixedNumeric(ProtocolBuilderNumeric builder) {
    return new SIntWrapperFixedNumeric(builder, precision);
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> add(DRes<Matrix<DRes<SFixed>>> a,
      DRes<Matrix<DRes<SFixed>>> b) {
    return builder.par(par -> {
      return add(par, a.out(), b.out(), (f, x) -> f.add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.par(par -> {
      return add(par, a, b.out(), (f, x) -> f.add(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a, Matrix<BigDecimal> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b, (f, x) -> innerProductWithPublicPart(f, x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a, b.out(), (f, x) -> innerProductWithPublicPart(f, x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a,
      DRes<Matrix<DRes<SFixed>>> b) {
    return builder.seq(seq -> {
      return mult(seq, a.out(), b.out(), (f, x) -> innerProduct(f, x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> scale(BigDecimal s, DRes<Matrix<DRes<SFixed>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(), (f, x) -> f.mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, DRes<Matrix<DRes<SFixed>>> a) {
    return builder.par(par -> {
      return scale(par, s, a.out(), (f, x) -> f.mult(x.getFirst(), x.getSecond()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, Matrix<BigDecimal> a) {
    return builder.par(par -> {
      return scale(par, s, a, (f, x) -> f.mult(x.getSecond(), x.getFirst()));
    });
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> input(Matrix<BigDecimal> a, int inputParty) {
    Matrix<BigInteger> asInts = mapMatrix(a, e -> e.setScale(precision).unscaledValue());
    DRes<Matrix<DRes<SInt>>> closed = builder.collections().closeMatrix(asInts, inputParty);
    return () -> mapMatrix(closed.out(), e -> new SFixedSIntWrapper(e));
  }

  @Override
  public DRes<Matrix<DRes<BigDecimal>>> open(DRes<Matrix<DRes<SFixed>>> a) {
    DRes<Matrix<DRes<SInt>>> closedAsInts = () -> mapMatrix(a.out(),
        entry -> ((SFixedSIntWrapper) entry.out()).getSInt());
    DRes<Matrix<DRes<BigInteger>>> openAsInts = builder.collections().openMatrix(closedAsInts);
    MatrixUtils utils = new MatrixUtils();
    return () -> mapMatrix(utils.unwrapMatrix(openAsInts), e -> () -> new BigDecimal(e, precision));
  }

  /**
   * Helper method where a map is applied to every entry of a matrix.
   * 
   * @param a
   *          The matrix.
   * @param map
   *          The map.
   * @return
   */
  private <S, T> Matrix<T> mapMatrix(Matrix<S> a, Function<S, T> map) {
    ArrayList<ArrayList<T>> rows = new ArrayList<ArrayList<T>>(a.getHeight());
    for (ArrayList<S> row : a.getRows()) {
      rows.add(new ArrayList<>(row.stream().map(map).collect(Collectors.toList())));
    }
    return new Matrix<>(a.getHeight(), a.getWidth(), rows);
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
  private <A> DRes<Matrix<DRes<SFixed>>> add(ProtocolBuilderNumeric builder, Matrix<A> a,
      Matrix<DRes<SFixed>> b, BiFunction<BasicFixedNumeric, Pair<A, DRes<SFixed>>, DRes<SFixed>> add) {
    return builder.par(par -> {
      if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
        throw new IllegalArgumentException("Matrices must have same sizes - " + a.getWidth() + "x"
            + a.getHeight() + " != " + b.getWidth() + "x" + b.getHeight());
      }
      BasicFixedNumeric fixed = getFixedNumeric(par);
      ArrayList<ArrayList<DRes<SFixed>>> rows = new ArrayList<>(a.getHeight());
      for (int j = 0; j < a.getHeight(); j++) {
        ArrayList<DRes<SFixed>> row = new ArrayList<>();
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
  private <A, B> DRes<Matrix<DRes<SFixed>>> mult(ProtocolBuilderNumeric builder, Matrix<A> a,
      Matrix<B> b, BiFunction<ProtocolBuilderNumeric, Pair<List<A>, List<B>>, DRes<SFixed>> innerProduct) {
    return builder.par(par -> {

      if (a.getWidth() != b.getHeight()) {
        throw new IllegalArgumentException(
            "Matrice sizes does not match - " + a.getWidth() + " != " + b.getHeight());
      }

      ArrayList<ArrayList<DRes<SFixed>>> rows = new ArrayList<>(a.getHeight());
      for (int i = 0; i < a.getHeight(); i++) {
        ArrayList<DRes<SFixed>> row = new ArrayList<>(b.getWidth());
        for (int j = 0; j < b.getWidth(); j++) {
          row.add(innerProduct.apply(par, new Pair<>(a.getRow(i), b.getColumn(j))));
        }
        rows.add(row);
      }
      return () -> new Matrix<>(a.getHeight(), b.getWidth(), rows);
    });
  }

  /**
   * Scale the given matrix by a scalar using the given builder and
   * multiplication operation.
   * 
   * @param builder
   * @param s
   * @param a
   * @param mult
   * @return
   */
  private <S, A> DRes<Matrix<DRes<SFixed>>> scale(ProtocolBuilderNumeric builder, S s, Matrix<A> a,
      BiFunction<BasicFixedNumeric, Pair<S, A>, DRes<SFixed>> mult) {
    return builder.par(par -> {
      BasicFixedNumeric fixed = getFixedNumeric(par);
      ArrayList<ArrayList<DRes<SFixed>>> rows = new ArrayList<>(a.getHeight());
      for (ArrayList<A> row : a.getRows()) {
        rows.add(new ArrayList<>(row.stream().map(x -> mult.apply(fixed, new Pair<>(s, x))).collect(Collectors.toList())));
      }
      return () -> new Matrix<>(a.getHeight(), a.getWidth(), rows);
    });
  }

  @Override
  public DRes<SFixed> innerProduct(List<BigDecimal> a, DRes<List<DRes<SFixed>>> b) {
    return innerProductWithPublicPart(builder, a, b.out());
  }

  private DRes<SFixed> innerProductWithPublicPart(ProtocolBuilderNumeric builder, List<BigDecimal> a, List<DRes<SFixed>> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
    }
    List<BigInteger> aInt = a.stream().map(x -> x.setScale(precision).unscaledValue()).collect(Collectors.toList());
    List<DRes<SInt>> bInt = b.stream().map(x -> ((SFixedSIntWrapper) x.out()).getSInt()).collect(Collectors.toList());
    DRes<SInt> innerProductInt = builder.advancedNumeric().innerProductWithPublicPart(aInt, bInt);
    DRes<SInt> innerProductUnscaled = builder.advancedNumeric().div(innerProductInt, BigInteger.TEN.pow(precision));
    return new SFixedSIntWrapper(innerProductUnscaled);
  }

  @Override
  public DRes<SFixed> innerProduct(DRes<List<DRes<SFixed>>> a, DRes<List<DRes<SFixed>>> b) {
    return innerProduct(builder, a.out(), b.out());
  }

  public DRes<SFixed> innerProduct(ProtocolBuilderNumeric builder, List<DRes<SFixed>> a, List<DRes<SFixed>> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
    }
    List<DRes<SInt>> aInt = a.stream().map(x -> ((SFixedSIntWrapper) x.out()).getSInt()).collect(Collectors.toList());
    List<DRes<SInt>> bInt = b.stream().map(x -> ((SFixedSIntWrapper) x.out()).getSInt()).collect(Collectors.toList());
    DRes<SInt> innerProductInt = builder.advancedNumeric().innerProduct(aInt, bInt);
    DRes<SInt> innerProductUnscaled = builder.advancedNumeric().div(innerProductInt, BigInteger.TEN.pow(precision));
    return new SFixedSIntWrapper(innerProductUnscaled);
  }

}
