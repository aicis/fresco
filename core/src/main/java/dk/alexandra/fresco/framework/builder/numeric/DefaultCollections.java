package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.RowPairD;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.io.CloseList;
import dk.alexandra.fresco.lib.collections.io.CloseMatrix;
import dk.alexandra.fresco.lib.collections.io.OpenList;
import dk.alexandra.fresco.lib.collections.io.OpenMatrix;
import dk.alexandra.fresco.lib.collections.io.OpenPair;
import dk.alexandra.fresco.lib.collections.io.OpenRowPair;
import dk.alexandra.fresco.lib.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.collections.relational.MiMCAggregation;
import dk.alexandra.fresco.lib.collections.shuffle.ShuffleRows;
import dk.alexandra.fresco.lib.conditional.ConditionalSelectRow;
import dk.alexandra.fresco.lib.conditional.SwapNeighborsIf;
import dk.alexandra.fresco.lib.conditional.SwapRowsIf;
import java.math.BigInteger;
import java.util.List;

public class DefaultCollections implements Collections {

  private final ProtocolBuilderNumeric builder;

  protected DefaultCollections(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
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
  public DRes<List<DRes<BigInteger>>> openList(DRes<List<DRes<SInt>>> closedList) {
    return builder.par(new OpenList(closedList));
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
  public DRes<Matrix<DRes<BigInteger>>> openMatrix(DRes<Matrix<DRes<SInt>>> closedMatrix) {
    return builder.par(new OpenMatrix(closedMatrix));
  }

  @Override
  public DRes<List<DRes<SInt>>> condSelect(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right) {
    return builder.par(new ConditionalSelectRow(condition, left, right));
  }

  @Override
  public DRes<RowPairD<SInt, SInt>> swapIf(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right) {
    return builder.par(new SwapRowsIf(condition, left, right));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> swapNeighborsIf(DRes<List<DRes<SInt>>> conditions,
      DRes<Matrix<DRes<SInt>>> rows) {
    return builder.par(new SwapNeighborsIf(conditions, rows));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int[] idxPerm) {
    return builder
        .seq(new PermuteRows(values, idxPerm, builder.getBasicNumericContext().getMyId(), true));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int permProviderPid) {
    return builder.seq(new PermuteRows(values, new int[] {}, permProviderPid, false));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> shuffle(DRes<Matrix<DRes<SInt>>> values) {
    return builder.seq(new ShuffleRows(values));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> leakyAggregateSum(DRes<Matrix<DRes<SInt>>> values,
      int groupColIdx, int aggColIdx) {
    return builder.seq(new MiMCAggregation(values, groupColIdx, aggColIdx));
  }

}
