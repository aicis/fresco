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

  /**
   * Opens a pair of secret values.
   * 
   * @param closedPair
   * @return
   */
  public DRes<Pair<DRes<BigInteger>, DRes<BigInteger>>> openPair(
      DRes<Pair<DRes<SInt>, DRes<SInt>>> closedPair);

  /**
   * Opens all values in a pair of rows (represented as lists).
   * 
   * @param closedPair
   * @return
   */
  public DRes<RowPairD<BigInteger, BigInteger>> openRowPair(DRes<RowPairD<SInt, SInt>> closedPair);

  /**
   * Closes list of input values.
   * 
   * To be called by party providing input.
   * 
   * @param openList
   * @param inputParty party providing input
   * @return
   */
  public DRes<List<DRes<SInt>>> closeList(List<BigInteger> openList, int inputParty);

  /**
   * Closes list of input values.
   * 
   * To be called by parties not providing input.
   * 
   * @param numberOfInputs
   * @param inputParty party providing input
   * @return
   */
  public DRes<List<DRes<SInt>>> closeList(int numberOfInputs, int inputParty);

  /**
   * Opens list of secret values.
   * 
   * @param closedList
   * @return
   */
  public DRes<List<DRes<BigInteger>>> openList(DRes<List<DRes<SInt>>> closedList);

  /**
   * Closes matrix of input values.
   * 
   * To be called by party providing input.
   * 
   * @param openMatrix
   * @param inputParty party providing input
   * @return
   */
  public DRes<Matrix<DRes<SInt>>> closeMatrix(Matrix<BigInteger> openMatrix, int inputParty);

  /**
   * Closes matrix of input values.
   * 
   * To be called by parties not providing input.
   * 
   * @param h height of matrix
   * @param w width of matrix
   * @param inputParty
   * @return
   */
  public DRes<Matrix<DRes<SInt>>> closeMatrix(int h, int w, int inputParty);

  /**
   * Opens matrix of secret values.
   * 
   * @param closedMatrix
   * @return
   */
  public DRes<Matrix<DRes<BigInteger>>> openMatrix(DRes<Matrix<DRes<SInt>>> closedMatrix);

  // Conditional

  /**
   * Returns <code>left</code> if <code>condition</code> is 1 and <code>right</code> otherwise.
   * 
   * @param condition must be 0 or 1
   * @param left
   * @param right
   * @return
   */
  public DRes<List<DRes<SInt>>> condSelect(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right);

  /**
   * Swaps <code>left</code> and <code>right</code> if <code>condition</code> is 1, keeps original
   * order otherwise. Returns result as a pair.
   * 
   * @param condition must be 0 or 1
   * @param left
   * @param right
   * @return
   */
  public DRes<RowPairD<SInt, SInt>> condSwap(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right);

  /**
   * Applies a conditional swap to all neighboring rows in matrix. Returns result as new matrix.
   * 
   * @param conditions determines for each neighbor pair whether to swap.
   * @param mat
   * @return
   */
  public DRes<Matrix<DRes<SInt>>> condSwapNeighbors(DRes<List<DRes<SInt>>> conditions,
      DRes<Matrix<DRes<SInt>>> mat);

  // Permutations

  /**
   * Permutes the rows of <code>values</code> according to <code>idxPerm</code>.
   * 
   * To be called by party choosing the permutation.
   * 
   * @param values
   * @param idxPerm encodes the desired permutation by supplying for each index a new index
   * @return
   */
  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int[] idxPerm);

  /**
   * Permutes the rows of <code>values</code> according to <code>idxPerm</code>.
   * 
   * To be called by parties not choosing the permutation.
   * 
   * @param values
   * @param permProviderPid the ID of the party choosing permutation
   * @return
   */
  public DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int permProviderPid);

  /**
   * Randomly permutes (shuffles) rows of <code>values</code>. Uses secure source of randomness.
   * 
   * @param values
   * @return
   */
  public DRes<Matrix<DRes<SInt>>> shuffle(DRes<Matrix<DRes<SInt>>> values);

  // Relational (SQL-like) operators

  /**
   * Performs a SQL-like group-by sum operation. Groups rows by column <code>groupColIdx</code> and
   * sums values in resulting groups in column <code>aggColIdx</code>.
   * 
   * NOTE: this particular implementation leaks equality of values in column
   * <code>groupColIdx</code> and the size of the result.
   * 
   * @param values
   * @param groupColIdx
   * @param aggColIdx
   * @return
   */
  public DRes<Matrix<DRes<SInt>>> leakyAggregateSum(DRes<Matrix<DRes<SInt>>> values,
      int groupColIdx, int aggColIdx);

  // Interfaces for testing only

  /**
   * Performs a SQL-like group-by sum operation. Groups rows by column <code>groupColIdx</code> and
   * sums values in resulting groups in column <code>aggColIdx</code>.
   * 
   * Accepts any source of randomness.
   * 
   * NOTE: testing interface, not to be used in production.
   * 
   * NOTE: this particular implementation leaks equality of values in column
   * <code>groupColIdx</code> and the size of the result.
   * 
   * @param values
   * @param groupColIdx
   * @param aggColIdx
   * @param rand source of randomness
   * @return
   */
  DRes<Matrix<DRes<SInt>>> leakyAggregateSum(DRes<Matrix<DRes<SInt>>> values, int groupColIdx,
      int aggColIdx, Random rand);

  /**
   * Randomly permutes (shuffles) rows of <code>values</code>.
   * 
   * Accepts any source of randomness.
   * 
   * NOTE: testing interface, not to be used in production.
   * 
   * @param values
   * @param rand source of randomness
   * @return
   */
  DRes<Matrix<DRes<SInt>>> shuffle(DRes<Matrix<DRes<SInt>>> values, Random rand);

}
