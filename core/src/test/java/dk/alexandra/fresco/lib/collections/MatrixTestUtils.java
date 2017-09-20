package dk.alexandra.fresco.lib.collections;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class MatrixTestUtils {
  
  /**
   * Create matrix of given dimensions filled with values from 0 to numRows * numCols.
   * 
   * @param numRows
   * @param numCols
   * @return
   */
  public Matrix<BigInteger> getInputMatrix(int numRows, int numCols) {
    ArrayList<ArrayList<BigInteger>> mat = new ArrayList<>();
    int counter = 0;
    for (int r = 0; r < numRows; r++) {
      ArrayList<BigInteger> row = new ArrayList<>();
      for (int c = 0; c < numCols; c++) {
        row.add(BigInteger.valueOf(counter++));
      }
      mat.add(row);
    }
    return new Matrix<>(numRows, numCols, mat);
  }
  
  /**
   * Creates matrix from 2d array.
   * 
   * @param rows
   * @return
   */
  public <T> Matrix<T> getInputMatrix(T[][] rows) {
    int h = rows.length;
    int w = rows[0].length;
    ArrayList<ArrayList<T>> mat = new ArrayList<>();
    for (T[] row: rows) {
      mat.add(new ArrayList<>(Arrays.asList(row)));
    }
    return new Matrix<>(h, w, mat);
  }
  
}
