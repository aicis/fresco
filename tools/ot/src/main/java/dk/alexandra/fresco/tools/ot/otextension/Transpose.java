package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class used to do bit transposition using Eklundhs method. Transposition is carried out in on a
 * row-major matrix represented as a list (rows) of bit vectors.
 */
public class Transpose {

  private Transpose() {
    // This class is meant to only contain static helper methods.
  }

  /**
   * Transposes, in-place, a matrix represent in row-major as a list of byte arrays.
   *
   * @param input The matrix to transpose
   */
  public static List<StrictBitVector> transpose(List<StrictBitVector> input) {
    // Ensure the is correctly formed
    doSanityCheck(input);
    int minDim = Math.min(input.get(0).getSize(), input.size());
    int maxDim = Math.max(input.get(0).getSize(), input.size());
    // Check if the matrix is tall
    boolean tall = minDim == input.get(0).getSize();
    int rows = tall ? minDim : maxDim;
    int columns = tall ? maxDim : minDim;
    byte[][] res = new byte[rows][columns / Byte.SIZE];
    // Process all squares of minDim x minDim
    List<List<byte[]>> squares = IntStream.range(0, maxDim / minDim)
        .mapToObj(i -> extractSquare(input, minDim, tall, i)).map(m -> {
          transposeAllByteBlocks(m);
          return m;
        })
        .map(m -> {
          doEklundh(m);
          return m;
        })
        .collect(Collectors.toList());
    IntStream.range(0, maxDim / minDim)
        .forEach(i -> insertSquare(minDim, tall, res, squares.get(i), i));
    return IntStream.range(0, res.length).mapToObj(i -> res[i]).map(StrictBitVector::new)
        .collect(Collectors.toList());
  }

  private static void insertSquare(int minDim, boolean tall, byte[][] res,
      List<byte[]> currentSquare, int i) {
    for (int j = 0; j < minDim; j++) {
      int rowOffset = tall ? 0 : i * minDim;
      int columnOffset = tall ? i * minDim / Byte.SIZE : 0;
      System.arraycopy(currentSquare.get(j), 0, res[j + rowOffset], columnOffset, minDim / 8);
    }
  }

  private static List<byte[]> extractSquare(List<StrictBitVector> input, int length, boolean tall,
      int i) {
    byte[][] tempSquare = new byte[length][length / Byte.SIZE];
    int rowOffset = tall ? i * length : 0;
    int columnOffset = tall ? 0 : i * length / Byte.SIZE;
    for (int j = 0; j < tempSquare.length; j++) {
      byte[] row = input.get(rowOffset + j).toByteArray();
      System.arraycopy(row, columnOffset, tempSquare[j], 0, tempSquare[j].length);
    }
    return Arrays.asList(tempSquare);
  }

  /**
   * Complete the Eklundh algorithm for transposing with initial blocks of 8 bits. That is, assuming
   * all blocks of 8 bits have already been transposed
   *
   * @param input The matrix to transpose. Represented in row-major
   */
  private static void doEklundh(List<byte[]> input) {
    int rows = input.size();
    // Multiply by 8 because there are 8 bits in a byte
    int byteColumns = input.get(0).length;
    // Do the Eklundh transposing
    for (int blockSize = 1; blockSize <= byteColumns / 2; blockSize = blockSize * 2) {
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
   * @param input The list of arrays of which to swap
   * @param row The row offset
   * @param column The column offset
   * @param blockSize The amount of bits in the block to swap
   */
  private static void swap(List<byte[]> input, int row, int column, int blockSize) {
    for (int k = 0; k < blockSize * 8; k++) {
      for (int l = 0; l < blockSize; l++) {
        byte temp = input.get(row + k)[column + blockSize + l];
        input.get(row + k)[column + blockSize + l] = input.get(row + blockSize * 8 + k)[column + l];
        input.get(row + blockSize * 8 + k)[column + l] = temp;
      }
    }
  }

  /**
   * Check that a matrix obeys the rules needed to do Eklundh transposing.
   *
   * @param input The matrix to check
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
   * @param input The input
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
   * Transposes 8x8 bit blocks of a row-major matrix, at positions "rowOffset", "columnOffset".
   *
   * @param input The matrix to transpose
   * @param rowOffset The row offset
   * @param columnOffset The column offset
   */
  private static void transposeByteBlock(List<byte[]> input, int rowOffset, int columnOffset) {
    /*
     * By having 8 variables we hope that the JVM will only access the main memory per iteration, to
     * read a byte, rather than both reading and writing to 8 bytes at different places in main
     * memory.
     */
    byte newRow0 = 0;
    byte newRow1 = 0;
    byte newRow2 = 0;
    byte newRow3 = 0;
    byte newRow4 = 0;
    byte newRow5 = 0;
    byte newRow6 = 0;
    byte newRow7 = 0;
    for (int k = 0; k < Byte.SIZE; k++) {
      byte currentRow = input.get(rowOffset + k)[columnOffset / Byte.SIZE];
      /*
       * First extract the bit of position (column) x for row x using AND (&), then shift it to the
       * leftmost position and do an unsigned rightshift to move it into the correct position for
       * the given row. Finally XOR (^) the new bit into the current value for the row
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
}
