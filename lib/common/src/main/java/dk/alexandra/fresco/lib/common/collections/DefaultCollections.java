package dk.alexandra.fresco.lib.common.collections;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.RowPairD;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.io.CloseList;
import dk.alexandra.fresco.lib.common.collections.io.CloseMatrix;
import dk.alexandra.fresco.lib.common.collections.io.OpenList;
import dk.alexandra.fresco.lib.common.collections.io.OpenMatrix;
import dk.alexandra.fresco.lib.common.collections.io.OpenPair;
import dk.alexandra.fresco.lib.common.collections.io.OpenRowPair;
import dk.alexandra.fresco.lib.common.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.common.collections.shuffle.ShuffleRows;
import dk.alexandra.fresco.lib.common.conditional.ConditionalSelectRow;
import dk.alexandra.fresco.lib.common.conditional.SwapNeighborsIf;
import dk.alexandra.fresco.lib.common.conditional.SwapRowsIf;
import java.math.BigInteger;
import java.util.List;

public class DefaultCollections implements Collections {

  private final ProtocolBuilderNumeric builder;

  public DefaultCollections(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<Pair<DRes<BigInteger>, DRes<BigInteger>>> openPair(
      DRes<Pair<DRes<SInt>, DRes<SInt>>> closedPair) {
    return builder.par(new OpenPair(closedPair));
  }

  @Override
  public DRes<RowPairD<BigInteger, BigInteger>> openRowPair(DRes<RowPairD<SInt, SInt>> closedPair) {
    return builder.par(new OpenRowPair(closedPair));
  }

  @Override
  public DRes<List<DRes<SInt>>> closeList(List<BigInteger> openList, int inputParty) {
    return builder.par(new CloseList(openList, inputParty));
  }

  @Override
  public DRes<List<DRes<SInt>>> closeList(int numberOfInputs, int inputParty) {
    return builder.par(new CloseList(numberOfInputs, inputParty));
  }

  @Override
  public <T extends DRes<SInt>> DRes<List<DRes<BigInteger>>> openList(DRes<List<T>> closedList) {
    return builder.par(new OpenList<>(closedList));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> closeMatrix(Matrix<BigInteger> openMatrix, int inputParty) {
    return builder.par(new CloseMatrix(openMatrix, inputParty));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> closeMatrix(int h, int w, int inputParty) {
    return builder.par(new CloseMatrix(h, w, inputParty));
  }

  @Override
  public <T extends DRes<SInt>> DRes<Matrix<DRes<BigInteger>>> openMatrix(
      DRes<Matrix<T>> closedMatrix) {
    return builder.par(new OpenMatrix<>(closedMatrix));
  }

  @Override
  public <T extends DRes<SInt>> DRes<List<DRes<SInt>>> condSelect(
      DRes<SInt> condition, DRes<List<T>> left, DRes<List<T>> right) {
    return builder.par(new ConditionalSelectRow<>(condition, left, right));
  }

  @Override
  public <T extends DRes<SInt>> DRes<RowPairD<SInt, SInt>> swapIf(
      DRes<SInt> condition, DRes<List<T>> left, DRes<List<T>> right) {
    return builder.par(new SwapRowsIf<>(condition, left, right));
  }

  @Override
  public <T extends DRes<SInt>> DRes<Matrix<DRes<SInt>>> swapNeighborsIf(
      DRes<List<T>> conditions, DRes<Matrix<T>> rows) {
    return builder.par(new SwapNeighborsIf<>(conditions, rows));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int[] idxPerm) {
    return builder.seq(
        new PermuteRows(values, idxPerm, builder.getBasicNumericContext().getMyId(), true));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int permProviderPid) {
    return builder.seq(new PermuteRows(values, new int[] {}, permProviderPid, false));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> shuffle(DRes<Matrix<DRes<SInt>>> values) {
    return builder.seq(new ShuffleRows(values));
  }
}
