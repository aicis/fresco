package dk.alexandra.fresco.lib.collections.permute;

import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigInteger;
import java.util.ArrayList;

public class WaksmanUtils {

  // pre-condition: n is power of 2
  private int log2(int n) {
    return (int) (Math.log(n) / Math.log(2));
  }

  private int nextUnsetSwapperIdx(int[] swappers) {
    for (int i = 0; i < swappers.length; i++) {
      if (swappers[i] == -1) {
        return i;
      }
    }
    return -1;
  }

  private int swapperIdxOf(int elIdx) {
    return elIdx / 2;
  }

  private int topElIndexOf(int swIdx) {
    return swIdx * 2;
  }

  private boolean even(int n) {
    return n % 2 == 0;
  }

  private boolean isTopEl(int elIdx) {
    return even(elIdx);
  }

  private int neighElIdxOf(int elIdx) {
    return isTopEl(elIdx) ? elIdx + 1 : elIdx - 1;
  }

  private boolean hasNextUnsetSwapper(int[] rowO) {
    return (nextUnsetSwapperIdx(rowO) != -1);
  }

  private int routeFromOutputToInput(int rowOElIdx, int[] invPerm, int[] topPerm, int[] rowI) {
    return route(rowOElIdx, invPerm, topPerm, rowI, true);
  }

  private int routeFromInputToOutput(int rowIElIdx, int[] perm, int[] bottomPerm, int[] rowO) {
    return route(rowIElIdx, perm, bottomPerm, rowO, false);
  }

  private int route(int fromElIdx, int[] perm, int[] subPerm, int[] swappers,
      boolean outputToInput) {
    // swapper associated with this element
    int fromSwpIdx = swapperIdxOf(fromElIdx);

    // the element we are routing to
    int toElIdx = perm[fromElIdx];

    // swapper of element we are routing to
    int toSwpIdx = swapperIdxOf(toElIdx);

    // set the swappers
    if (outputToInput) {
      // going from output to input means that we route through top permutation
      swappers[toSwpIdx] = isTopEl(toElIdx) ? 0 : 1;
      // update sub-permutation
      subPerm[toSwpIdx] = fromSwpIdx;
    } else {
      // going from input to output means that we route through bottom permutation
      swappers[toSwpIdx] = isTopEl(toElIdx) ? 1 : 0;
      // update sub-permutation
      subPerm[fromSwpIdx] = toSwpIdx;
    }

    // get neighbor element of target element
    return neighElIdxOf(toElIdx);
  }

  private void setControlBits(int[] perm, int[][] controlBits, int rowIdx, int colIdx) {
    int n = perm.length;

    if (n == 2) {
      // for our base case we only have one swapper gate
      // which we can set directly
      controlBits[rowIdx][colIdx] = perm[0];
      return;
    }

    int[] rowI = new int[n / 2];
    int[] rowO = new int[n / 2];
    for (int j = 0; j < rowO.length; j++) {
      rowI[j] = -1;
      rowO[j] = -1;
    }

    int[] topPerm = new int[n / 2];
    int[] bottomPerm = new int[n / 2];

    int[] permInv = invert(perm);

    // loop until we have set all swappers at this level of the network
    while (hasNextUnsetSwapper(rowO)) {
      // find next O swapper that doesn't have control bit set
      int rowOSwIdx = nextUnsetSwapperIdx(rowO);
      // route through bottom
      rowO[rowOSwIdx] = 0;

      int firstOElIdx = topElIndexOf(rowOSwIdx);
      int rowOElIdx = firstOElIdx;

      // route from output to input and back again
      int rowIElIdx = routeFromOutputToInput(rowOElIdx, permInv, topPerm, rowI);
      rowOElIdx = routeFromInputToOutput(rowIElIdx, perm, bottomPerm, rowO);

      // loop until we're back at the start
      while (!(rowOElIdx == firstOElIdx)) {
        rowIElIdx = routeFromOutputToInput(rowOElIdx, permInv, topPerm, rowI);
        rowOElIdx = routeFromInputToOutput(rowIElIdx, perm, bottomPerm, rowO);
      }
    }

    // we have set all input and output swappers at this level of the network so we update our
    // swapper matrix and continue setting the swappers for our two sub permutations
    int numCols = controlBits[0].length;
    for (int j = 0; j < rowI.length; j++) {
      controlBits[rowIdx + j][colIdx] = rowI[j];
      controlBits[rowIdx + j][numCols - 1 - colIdx] = rowO[j];
    }
    setControlBits(topPerm, controlBits, rowIdx, colIdx + 1);
    setControlBits(bottomPerm, controlBits, rowIdx + rowI.length / 2, colIdx + 1);
  }

  public int getNumRowsRequired(int n) {
    if (n < 2) {
      return 0;
    } else {
      return n / 2;
    }
  }

  public int getNumColsRequired(int n) {
    if (n < 2) {
      return 0;
    } else {
      return (int) log2(n) * 2 - 1;
    }
  }

  public boolean isPow2(int n) {
    return n >= 0 && ((n & (n - 1)) == 0);
  }

  public int[] invert(int[] p) {
    int[] inv = new int[p.length];
    for (int i = 0; i < p.length; i++) {
      inv[p[i]] = i;
    }
    return inv;
  }

  public Matrix<BigInteger> setControlBits(int[] perm) throws UnsupportedOperationException {
    int n = perm.length;
    if (!isPow2(n)) {
      throw new UnsupportedOperationException("Size must be power of 2");
    }

    int numRows = getNumRowsRequired(n);
    int numCols = getNumColsRequired(n);

    int[][] controlBits = new int[numRows][numCols];
    setControlBits(perm, controlBits, 0, 0);

    ArrayList<ArrayList<BigInteger>> rows = new ArrayList<>();
    for (int[] row : controlBits) {
      ArrayList<BigInteger> arr = new ArrayList<>();
      for (int val : row) {
        arr.add(BigInteger.valueOf(val));
      }
      rows.add(arr);
    }

    return new Matrix<>(numRows, numCols, rows);
  }
}
