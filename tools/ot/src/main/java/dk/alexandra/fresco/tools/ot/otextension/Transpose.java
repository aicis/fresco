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
    List<List<byte[]>> squares = IntStream.range(0, maxDim / minDim).parallel()
        .mapToObj(i -> extractSquare(input, minDim, tall, i)).map(m -> {
          transposeAllByteBlocks(m);
          return m;
        }).map(m -> {
          doEklundh(m);
          return m;
        }).collect(Collectors.toList());
    IntStream.range(0, maxDim / minDim).parallel()
        .forEach(i -> insertSquare(res, squares.get(i), minDim, tall, i));
    return IntStream.range(0, res.length).parallel().mapToObj(i -> res[i]).map(StrictBitVector::new)
        .collect(Collectors.toList());
  }

  /**
   * Given a matrix <i>M</i> where the smallest dimension, <i>m</i>, divides the largest,<i>n</i>,
   * we can see <i>M</i> as a matrix consisting of a sequence of <i>n/m</i> square sub matrices of
   * dimension <i>m x m</i>. This takes such a square matrix an inserts it into the larger matrix
   * at a given position in the sequence.
   * @param matrix the larger matrix in which to insert a square
   * @param square the square matrix
   * @param minDim the smallest dimension of the larger matrix
   * @param wide if the matrix is wide, i.e., if the first dimension is smaller than the second.
   * @param i the position in which to insert the square matrix
   */
  private static void insertSquare(byte[][] matrix, List<byte[]> square, int minDim, boolean wide,
      int i) {
    for (int j = 0; j < minDim; j++) {
      int rowOffset = wide ? 0 : i * minDim;
      int columnOffset = wide ? i * minDim / Byte.SIZE : 0;
      System.arraycopy(square.get(j), 0, matrix[j + rowOffset], columnOffset, minDim / Byte.SIZE);
    }
  }

  /**
   * Given a matrix <i>M</i> where the smallest dimension, <i>m</i>, divides the largest,<i>n</i>,
   * we can see <i>M</i> as a matrix consisting of a sequence of <i>n/m</i> square sub matrices of
   * dimension <i>m x m</i>. This extracts such a square matrix from a given position in the
   * sequence of a larger matrix <i>M</i>.
   *
   * @param matrix the matrix from which to extract a square matrix
   * @param minDim the smallest dimension of the matrix
   * @param tall if the matrix is tall, i.e., if the first dimension is larger than the second
   * @param i the position the sequence from which to extract a square matrix
   * @return the corresponding square matrix
   */
  private static List<byte[]> extractSquare(List<StrictBitVector> matrix, int minDim, boolean tall,
      int i) {
    byte[][] tempSquare = new byte[minDim][minDim / Byte.SIZE];
    int rowOffset = tall ? i * minDim : 0;
    int columnOffset = tall ? 0 : i * minDim / Byte.SIZE;
    for (int j = 0; j < tempSquare.length; j++) {
      byte[] row = matrix.get(rowOffset + j).toByteArray();
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
    int byteColumns = input.get(0).length;
    for (int blockSize = 1; blockSize <= byteColumns / 2; blockSize = blockSize * 2) {
      for (int i = 0; i < rows; i = i + 2 * Byte.SIZE * blockSize) {
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
    for (int k = 0; k < blockSize * Byte.SIZE; k++) {
      for (int l = 0; l < blockSize; l++) {
        byte temp = input.get(row + k)[column + blockSize + l];
        input.get(row + k)[column + blockSize + l] =
            input.get(row + blockSize * Byte.SIZE + k)[column + l];
        input.get(row + blockSize * Byte.SIZE + k)[column + l] = temp;
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
    if ((rows % Byte.SIZE != 0) || // Check 8 | rows
        (((rows / Byte.SIZE) & ((rows / Byte.SIZE) - 1)) != 0)) {
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
    for (int i = 0; i < input.size(); i = i + Byte.SIZE) {
      for (int j = 0; j < input.get(0).length * Byte.SIZE; j = j + Byte.SIZE) {
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
    input.get(rowOffset + 0)[columnOffset / Byte.SIZE] = newRow0;
    input.get(rowOffset + 1)[columnOffset / Byte.SIZE] = newRow1;
    input.get(rowOffset + 2)[columnOffset / Byte.SIZE] = newRow2;
    input.get(rowOffset + 3)[columnOffset / Byte.SIZE] = newRow3;
    input.get(rowOffset + 4)[columnOffset / Byte.SIZE] = newRow4;
    input.get(rowOffset + 5)[columnOffset / Byte.SIZE] = newRow5;
    input.get(rowOffset + 6)[columnOffset / Byte.SIZE] = newRow6;
    input.get(rowOffset + 7)[columnOffset / Byte.SIZE] = newRow7;
  }
}
