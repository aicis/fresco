package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.RowPairD;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigInteger;
import java.util.List;

/**
 * Interface for operations on collections. This also includes opening and closing values. 
 */
public interface Collections extends ComputationDirectory {

  // I/O

  /**
   * Opens a pair of secret values.
   * 
   * @param closedPair secret values
   * @return opened values
   */
  DRes<Pair<DRes<BigInteger>, DRes<BigInteger>>> openPair(
      DRes<Pair<DRes<SInt>, DRes<SInt>>> closedPair);

  /**
   * Opens all values in a pair of rows (represented as lists).
   * 
   * @param closedPair secret values
   * @return openened rows
   */
  DRes<RowPairD<BigInteger, BigInteger>> openRowPair(DRes<RowPairD<SInt, SInt>> closedPair);

  /**
   * Closes list of input values. <br>
   * To be called by party providing input.
   * 
   * @param openList input list
   * @param inputParty party providing input
   * @return closed list
   */
  DRes<List<DRes<SInt>>> closeList(List<BigInteger> openList, int inputParty);

  /**
   * Closes list of input values. <br>
   * To be called by parties not providing input.
   * 
   * @param numberOfInputs number of inputs in list
   * @param inputParty party providing input
   * @return closed list
   */
  DRes<List<DRes<SInt>>> closeList(int numberOfInputs, int inputParty);

  /**
   * Opens list of secret values. <br>
   * 
   * @param closedList secret values
   * @return closed values
   */
  DRes<List<DRes<BigInteger>>> openList(DRes<List<DRes<SInt>>> closedList);

  /**
   * Closes matrix of input values. <br>
   * To be called by party providing input.
   * 
   * @param openMatrix input matrix
   * @param inputParty party providing input
   * @return closed matrix
   */
  DRes<Matrix<DRes<SInt>>> closeMatrix(Matrix<BigInteger> openMatrix, int inputParty);

  /**
   * Closes matrix of input values. <br>
   * To be called by parties not providing input.
   * 
   * @param h height of matrix
   * @param w width of matrix
   * @param inputParty party providing input
   * @return closed matrix
   */
  DRes<Matrix<DRes<SInt>>> closeMatrix(int h, int w, int inputParty);

  /**
   * Opens matrix of secret values.
   *
   * @param closedMatrix input matrix
   * @return open matrix
   */
  DRes<Matrix<DRes<BigInteger>>> openMatrix(DRes<Matrix<DRes<SInt>>> closedMatrix);

  // Conditional

  /**
   * Returns <code>left</code> if <code>condition</code> is 1 and <code>right</code> otherwise.
   * 
   * @param condition must be 0 or 1
   * @param left left row
   * @param right right row
   * @return left if condition right otherwise
   */
  DRes<List<DRes<SInt>>> condSelect(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right);

  /**
   * Swaps <code>left</code> and <code>right</code> if <code>condition</code> is 1, keeps original
   * order otherwise. Returns result as a pair.
   * 
   * @param condition must be 0 or 1
   * @param left left row
   * @param right right row
   * @return swapped rows if condition, same order rows otherwise
   */
  DRes<RowPairD<SInt, SInt>> swapIf(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right);

  /**
   * Applies a conditional swap to all neighboring rows in matrix. Returns result as new matrix.
   * 
   * @param conditions determines for each neighbor pair whether to swap.
   * @param mat matrix to be row-swapped
   * @return matrix with rows swapped according to conditions
   */
  DRes<Matrix<DRes<SInt>>> swapNeighborsIf(DRes<List<DRes<SInt>>> conditions,
      DRes<Matrix<DRes<SInt>>> mat);

  // Permutations

  /**
   * Permutes the rows of <code>values</code> according to <code>idxPerm</code>. <br>
   * To be called by party choosing the permutation.
   * 
   * @param values rows to permute
   * @param idxPerm encodes the desired permutation by supplying for each index a new index
   * @return permuted rows
   */
  DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int[] idxPerm);

  /**
   * Permutes the rows of <code>values</code> according to <code>idxPerm</code>. <br>
   * To be called by parties not choosing the permutation.
   * 
   * @param values rows to permute
   * @param permProviderPid the ID of the party choosing permutation
   * @return permuted rows
   */
  DRes<Matrix<DRes<SInt>>> permute(DRes<Matrix<DRes<SInt>>> values, int permProviderPid);

  /**
   * Randomly permutes (shuffles) rows of <code>values</code>. Uses secure source of randomness.
   * 
   * @param values rows to shuffle
   * @return shuffled rows
   */
  DRes<Matrix<DRes<SInt>>> shuffle(DRes<Matrix<DRes<SInt>>> values);

  // Relational (SQL-like) operators

  /**
   * Performs a SQL-like group-by sum operation. Groups rows by column <code>groupColIdx</code> and
   * sums values in resulting groups in column <code>aggColIdx</code>. <br>
   * NOTE: this particular implementation leaks equality of values in column
   * <code>groupColIdx</code> and the size of the result.
   * 
   * @param values rows to be aggregated
   * @param groupColIdx column to group by
   * @param aggColIdx column to aggregate
   * @return aggregated result
   */
  DRes<Matrix<DRes<SInt>>> leakyAggregateSum(DRes<Matrix<DRes<SInt>>> values,
      int groupColIdx, int aggColIdx);

}
