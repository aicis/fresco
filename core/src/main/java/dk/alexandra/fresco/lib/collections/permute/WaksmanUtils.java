package dk.alexandra.fresco.lib.collections.permute;

import java.math.BigInteger;
import java.util.ArrayList;

import dk.alexandra.fresco.lib.collections.Matrix;

public class WaksmanUtils {

  // pre-condition: n is power of 2
  private int log2(int n) {
    return (int) (Math.log(n) / Math.log(2));
  }

  public int[] invert(int[] p) {
    int[] inv = new int[p.length];
    for (int i = 0; i < p.length; i++) {
      inv[p[i]] = i;
    }
    return inv;
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

  private void setControlBits(int[] perm, int[][] controlBits, int rowIdx, int colIdx) {
    int n = perm.length;

    if (n == 2) {
      // for our base case we only have one swapper gate
      // which we can set directly
      controlBits[rowIdx][colIdx] = perm[0];
      return;
    }

    int[] I = new int[n / 2];
    int[] O = new int[n / 2];
    for (int j = 0; j < O.length; j++) {
      I[j] = -1;
      O[j] = -1;
    }

    int[] topPerm = new int[n / 2];
    int[] bottomPerm = new int[n / 2];

    int[] permInv = invert(perm);

    while (true) {

      // find next O swapper that doesn't have control bit set
      int unsetOSwapperIdx = nextUnsetSwapperIdx(O);
      // if there is no such swapper, we have set all I and O swappers
      // at this level of the network and can stop
      if (unsetOSwapperIdx == -1) {
        break;
      }

      int OSwIdx = unsetOSwapperIdx;
      O[OSwIdx] = 0;

      int firstOElIdx = topElIndexOf(OSwIdx);
      int OElIdx = firstOElIdx;

      while (true) {

        /*
         * we will "route" from the current output element to the input element that maps to it in
         * the given permutation via the top permutation.
         */

        // look up which input element maps to this output element in
        // the permutation
        int IElIdx = permInv[OElIdx];

        // get the swapper of the input element
        int ISwIdx = swapperIdxOf(IElIdx);

        // set top sub-permutation for the selected I and O swappers
        topPerm[ISwIdx] = OSwIdx;

        /*
         * we are routing through the top permutation and by convention all top elements must go to
         * the top permutation and all bottom elements must go to bottom permutation. if our input
         * element is the bottom element we must therefore set the input swapper to "swap"
         */
        if (isTopEl(IElIdx)) {
          I[ISwIdx] = 0;
        } else {
          I[ISwIdx] = 1;
        }

        /*
         * now that we have set an I swapper, we can take the other input element associated with
         * this swapper, find which output element it maps to in our permutation and set its
         * corresponding O swapper by "routing" to it through the bottom permutation
         */
        IElIdx = neighElIdxOf(IElIdx);

        OElIdx = perm[IElIdx];

        // look up swapper of output element
        OSwIdx = swapperIdxOf(OElIdx);

        // we are routing through the bottom permutation so if output
        // element
        // is top we need to set swapper to "swap"
        if (isTopEl(OElIdx)) {
          O[OSwIdx] = 1;
        } else {
          O[OSwIdx] = 0;
        }

        // update bottom permutation
        bottomPerm[ISwIdx] = OSwIdx;

        // get neighbor element of output element
        OElIdx = neighElIdxOf(OElIdx);

        /*
         * note that we are guaranteed to terminate since we are picking the next element to visit
         * based on a permutation so the only element we are ever going to revisit is the one we
         * started with at which point we stop
         */
        if (OElIdx == firstOElIdx) {
          break;
        }
      }
    }

    /*
     * we have set all input and output swappers at this level of the network so we update our
     * swapper matrix and continue on setting the swappers for our two sub permutations
     */
    int numCols = controlBits[0].length;
    for (int j = 0; j < I.length; j++) {
      controlBits[rowIdx + j][colIdx] = I[j];
      controlBits[rowIdx + j][numCols - 1 - colIdx] = O[j];
    }
    setControlBits(topPerm, controlBits, rowIdx, colIdx + 1);
    setControlBits(bottomPerm, controlBits, rowIdx + I.length / 2, colIdx + 1);
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
