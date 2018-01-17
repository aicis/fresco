package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to do bit transposition using Eklundhs method. Transposition is
 * carried out in on a row-major matrix represented as a list (rows) of bit
 * vectors.
 */
public class Transpose {

  /**
   * Transposes, in-place, a matrix represent in row-major as a list of byte
   * arrays.
   * 
   * @param input
   *          The matrix to transpose
   */
  public static List<StrictBitVector> transpose(List<StrictBitVector> input) {
    // Ensure the is correctly formed
    doSanityCheck(input);
    int minDim = Math.min(input.get(0).getSize(), input.size());
    int maxDim = Math.max(input.get(0).getSize(), input.size());
    // Allocate the new matrix
    int rows;
    int columns;
    // Check if the matrix is tall
    if (minDim == input.get(0).getSize()) {
      // Then the new matrix will be wide
      rows = minDim;
      columns = maxDim;
    } else {
      // Otherwise the matrix is wide, and the new matrix will be tall
      rows = maxDim;
      columns = minDim;
    }
    // Allocate result matrix
    List<StrictBitVector> res = initializeMatrix(rows, columns);
    // Allocate temporary matrix
    List<byte[]> currentSquare = initializeByteMatrix(minDim, minDim);
    // Process all squares of minDim x minDim
    for (int i = 0; i < maxDim / minDim; i++) {
      // Copy current block into "currentSquare"
      for (int j = 0; j < minDim; j++) {
        for (int k = 0; k < minDim; k++) {
          if (minDim == input.get(0).getSize()) {
            ByteArrayHelper.setBit(currentSquare.get(j), k,
                input.get(i * minDim + j).getBit(k, false));
          } else {
            ByteArrayHelper.setBit(currentSquare.get(j), k,
                input.get(j).getBit(i * minDim + k, false));
          }
        }
      }
      // Transpose blocks of 8 bits using the trivial algorithm
      transposeAllByteBlocks(currentSquare);
      // Do Eklundh to complete the transposing
      doEklundh(currentSquare);
      // Put "currentSqaure" into its correct position in "res"
      for (int j = 0; j < minDim; j++) {
        for (int k = 0; k < minDim; k++) {
          if (minDim == input.get(0).getSize()) {
            res.get(j).setBit(i * minDim + k,
                ByteArrayHelper.getBit(currentSquare.get(j), k), false);
          } else {
            res.get(i * minDim + j).setBit(k,
                ByteArrayHelper.getBit(currentSquare.get(j), k), false);
          }
        }
      }
    }
    return res;
  }

  /**
   * Complete the Eklundh algorithm for transposing with initial blocks of 8
   * bits. That is, assuming all blocks of 8 bits have already been transposed
   * 
   * @param input
   *          The matrix to transpose. Represented in row-major
   */
  private static void doEklundh(List<byte[]> input) {
    int rows = input.size();
    // Multiply by 8 because there are 8 bits in a byte
    int byteColumns = input.get(0).length;
    // Do the Eklundh transposing
    for (int blockSize = 1; blockSize <= byteColumns / 2; blockSize = blockSize
        * 2) {
      for (int i = 0; i < rows; i = i + 2 * 8 * blockSize) {
        for (int j = 0; j < byteColumns; j = j + 2 * blockSize) {
          // swap the blocks
          swap(input, i, j, blockSize);
        }
      }
    }
  }

  /**
   * Swaps the content of two square blocks, in-place.
   * 
   * @param input
   *          The list of arrays of which to swap
   * @param row
   *          The row offset
   * @param column
   *          The column offset
   * @param blockSize
   *          The amount of bits in the block to swap
   */
  private static void swap(List<byte[]> input, int row, int column,
      int blockSize) {
    for (int k = 0; k < blockSize * 8; k++) {
      for (int l = 0; l < blockSize; l++) {
        byte temp = input.get(row + k)[column + blockSize + l];
        input.get(row + k)[column + blockSize + l] = input
            .get(row + blockSize * 8 + k)[column + l];
        input.get(row + blockSize * 8 + k)[column + l] = temp;
      }
    }
  }

  /**
   * Check that a matrix obeys the rules needed to do Eklundh transposing.
   * 
   * @param input
   *          The matrix to check
   */
  private static void doSanityCheck(List<StrictBitVector> input) {
    int rows = input.size();
    // Check if the amount of rows is 8*2^x for some x
    if ((rows % 8 != 0) || // Check 8 | rows
        (((rows / 8) & ((rows / 8) - 1)) != 0)) {
      // Verify that the msb is 1 and all other bits are 0
      throw new IllegalArgumentException(
          "The amount rows in the matrix is not 8*2^x for some x > 1");
    }
    if ((input.get(0).getSize() & (input.get(0).getSize() - 1)) != 0) {
      // Verify that the msb is 1 and all other bits are 0
      throw new IllegalArgumentException(
          "The amount columns in the matrix is not 8*2^x for some x > 1");
    }
    // Check that all columns are of equal length
    for (int i = 1; i < rows; i++) {
      if (input.get(0).getSize() != input.get(i).getSize()) {
        throw new IllegalArgumentException("Not all rows are of equal length");
      }
    }
  }

  /**
   * Transpose all 8 bit squares in a square matrix, in-place.
   * 
   * @param input
   *          The input
   */
  private static void transposeAllByteBlocks(List<byte[]> input) {
    // Start by transposing one byte and 8 rows at a time using the trivial
    // O(n^2) algorithm
    for (int i = 0; i < input.size(); i = i + 8) {
      for (int j = 0; j < input.get(0).length * 8; j = j + 8) {
        transposeByteBlock(input, i, j);
      }
    }
  }

  /**
   * Transposes 8x8 bit blocks of a row-major matrix, at positions "rowOffset",
   * "columnOffset".
   * 
   * @param input
   *          The matrix to transpose
   * @param rowOffset
   *          The row offset
   * @param columnOffset
   *          The column offset
   */
  private static void transposeByteBlock(List<byte[]> input, int rowOffset,
      int columnOffset) {
    /*
     * By having 8 variables we hope that the JVM will only access the main
     * memory per iteration, to read a byte, rather than both reading and
     * writing to 8 bytes at different places in main memory.
     */
    byte newRow0 = 0;
    byte newRow1 = 0;
    byte newRow2 = 0;
    byte newRow3 = 0;
    byte newRow4 = 0;
    byte newRow5 = 0;
    byte newRow6 = 0;
    byte newRow7 = 0;
    for (int k = 0; k < 8; k++) {
      byte currentRow = input.get(rowOffset + k)[columnOffset / 8];
      /*
       * First extract the bit of position (column) x for row x using AND (&),
       * then shift it to the leftmost position and do an unsigned rightshift to
       * move it into the correct position for the given row. Finally XOR (^)
       * the new bit into the current value for the row
       */
      newRow0 ^= (byte) (((currentRow & 0x80) << 24 + 0) >>> (24 + k));
      newRow1 ^= (byte) (((currentRow & 0x40) << 24 + 1) >>> (24 + k));
      newRow2 ^= (byte) (((currentRow & 0x20) << 24 + 2) >>> (24 + k));
      newRow3 ^= (byte) (((currentRow & 0x10) << 24 + 3) >>> (24 + k));
      newRow4 ^= (byte) (((currentRow & 0x08) << 24 + 4) >>> (24 + k));
      newRow5 ^= (byte) (((currentRow & 0x04) << 24 + 5) >>> (24 + k));
      newRow6 ^= (byte) (((currentRow & 0x02) << 24 + 6) >>> (24 + k));
      newRow7 ^= (byte) (((currentRow & 0x01) << 24 + 7) >>> (24 + k));
    }
    input.get(rowOffset + 0)[columnOffset / 8] = newRow0;
    input.get(rowOffset + 1)[columnOffset / 8] = newRow1;
    input.get(rowOffset + 2)[columnOffset / 8] = newRow2;
    input.get(rowOffset + 3)[columnOffset / 8] = newRow3;
    input.get(rowOffset + 4)[columnOffset / 8] = newRow4;
    input.get(rowOffset + 5)[columnOffset / 8] = newRow5;
    input.get(rowOffset + 6)[columnOffset / 8] = newRow6;
    input.get(rowOffset + 7)[columnOffset / 8] = newRow7;
  }

  /**
   * Allocate a matrix in row-major form, as a list of byte arrays.
   * 
   * @param rows
   *          Rows in the matrix
   * @param columns
   *          columns in the matrix
   * @return The constructed matrix
   */
  private static List<StrictBitVector> initializeMatrix(int rows, int columns) {
    List<StrictBitVector> res = new ArrayList<>(rows);
    for (int i = 0; i < rows; i++) {
      // A byte is always 8 bits
      res.add(new StrictBitVector(columns));
    }
    return res;
  }

  private static List<byte[]> initializeByteMatrix(int rows, int columns) {
    List<byte[]> res = new ArrayList<>(rows);
    for (int i = 0; i < rows; i++) {
      // A byte is always 8 bits
      res.add(new byte[columns / 8]);
    }
    return res;
  }
}
