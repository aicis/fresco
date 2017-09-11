package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.RowPairD;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;

public interface Collections extends ComputationDirectory {

  // I/O

  public DRes<Pair<DRes<BigInteger>, DRes<BigInteger>>> openPair(
      DRes<Pair<DRes<SInt>, DRes<SInt>>> closedPair);

  public DRes<RowPairD<BigInteger, BigInteger>> openRowPair(DRes<RowPairD<SInt, SInt>> closedPair);

  public DRes<List<DRes<SInt>>> closeList(List<BigInteger> openList, int inputParty);

  public DRes<List<DRes<SInt>>> closeList(int numberOfInputs, int inputParty);

  public DRes<List<DRes<BigInteger>>> openList(DRes<List<DRes<SInt>>> closedList);

  public DRes<Matrix<DRes<SInt>>> closeMatrix(Matrix<BigInteger> openMatrix, int inputParty);

  public DRes<Matrix<DRes<SInt>>> closeMatrix(int h, int w, int inputParty);

  public DRes<Matrix<DRes<BigInteger>>> openMatrix(DRes<Matrix<DRes<SInt>>> closedMatrix);

  // Conditional

  public DRes<List<DRes<SInt>>> condSelect(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right);

  public DRes<RowPairD<SInt, SInt>> condSwap(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right);

  public DRes<Matrix<DRes<SInt>>> condSwapNeighbors(DRes<List<DRes<SInt>>> conditions,
      DRes<Matrix<DRes<SInt>>> mat);

  // Permutations

  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int[] idxPerm);

  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int permProviderPid);

  public DRes<Matrix<DRes<SInt>>> shuffle(DRes<Matrix<DRes<SInt>>> values, Random rand);

  public DRes<Matrix<DRes<SInt>>> shuffle(DRes<Matrix<DRes<SInt>>> values);

  // Relational (SQL-like) operators

  public DRes<Matrix<DRes<SInt>>> leakyAggregateSum(DRes<Matrix<DRes<SInt>>> values, int groupColIdx,
      int aggColIdx);

  public DRes<Matrix<DRes<SInt>>> leakyAggregateSum(DRes<Matrix<DRes<SInt>>> values, int groupColIdx,
      int aggColIdx, Random rand);
  
}
