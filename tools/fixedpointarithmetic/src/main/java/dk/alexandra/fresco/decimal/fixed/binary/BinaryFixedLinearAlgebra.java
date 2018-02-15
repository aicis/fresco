package dk.alexandra.fresco.decimal.fixed.binary;

import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class BinaryFixedLinearAlgebra extends DefaultLinearAlgebra {

  public BinaryFixedLinearAlgebra(ProtocolBuilderNumeric builder, int precision) {
    super(builder, scope -> new BinaryFixedNumeric(scope, precision));
  }
  //
  // @Override
  // public DRes<Matrix<DRes<SReal>>> input(Matrix<BigDecimal> a, int inputParty) {
  // Matrix<BigInteger> asInts =
  // mapMatrix(a, e -> e.setScale(precision, RoundingMode.DOWN).unscaledValue());
  // DRes<Matrix<DRes<SInt>>> closed = builder.collections().closeMatrix(asInts, inputParty);
  // return () -> mapMatrix(closed.out(), e -> new SFixed(e));
  // }
  //
  // @Override
  // public DRes<Matrix<DRes<BigDecimal>>> open(DRes<Matrix<DRes<SReal>>> a) {
  // DRes<Matrix<DRes<SInt>>> closedAsInts =
  // () -> mapMatrix(a.out(), entry -> ((SFixed) entry.out()).getSInt());
  // DRes<Matrix<DRes<BigInteger>>> openAsInts = builder.collections().openMatrix(closedAsInts);
  // MatrixUtils utils = new MatrixUtils();
  // return () -> mapMatrix(utils.unwrapMatrix(openAsInts), e -> () -> new BigDecimal(e,
  // precision));
  // }
  //
  // private <S, T> Matrix<T> mapMatrix(Matrix<S> a, Function<S, T> map) {
  // ArrayList<ArrayList<T>> rows = new ArrayList<>(a.getHeight());
  // for (ArrayList<S> row : a.getRows()) {
  // rows.add(new ArrayList<>(row.stream().map(map).collect(Collectors.toList())));
  // }
  // return new Matrix<>(a.getHeight(), a.getWidth(), rows);
  // }

}
