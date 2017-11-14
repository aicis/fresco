package dk.alexandra.fresco.tools.ot.otextension;

import java.util.List;

public class Transpose {

  public static void transpose(List<byte[]> input) {
    // Ensure the input is formed as needed by Eklundh
    doSanityCheck(input);
    // Transpose blocks of 8 bits using the trivial algorithm
    transposeAllByteBlocks(input);
    // Do Eklundh to complete the transposing
    doEklundh(input);
  }

  protected static void doEklundh(List<byte[]> input) {
    int rows = input.size();
    // Multiply by 8 because there are 8 bits in a byte
    int byteColumns = input.get(0).length;
    // Do the Eklundh transposing
    for (int blockSize = 1; blockSize <= byteColumns / 2; blockSize = blockSize
        * 2) {
      for (int i = 0; i < rows; i = i + 2 * 8 * blockSize) {
        for (int j = 0; j < byteColumns; j = j
            + 2 * blockSize) {
          // swap the blocks
          for (int k = 0; k < blockSize * 8; k++) {
            for (int l = 0; l < blockSize; l++) {
              byte temp = input.get(i + k)[j + blockSize + l];
              input.get(i + k)[j + blockSize + l] = input
                  .get(i + blockSize * 8 + k)[j + l];
              input.get(i + blockSize * 8 + k)[j + l] = temp;
            }
          }
        }
      }
    }
  }

  protected static void doSanityCheck(List<byte[]> input) {
    int rows = input.size();
    // Check if the amount of rows is 8*2^x for some x
    if ((rows % 8 != 0) || // Check 8 | rows
        (((rows / 8) & ((rows / 8) - 1)) != 0)) { // Verify that the msb is 1
                                                  // and all other bits are 0
      throw new IllegalArgumentException(
          "The amount rows in the matrix is not 8*2^x for some x > 1");
    }
    if ((input.get(0).length & (input.get(0).length - 1)) != 0) { // Verify that
                                                                  // the msb
      // is 1 and all other
      // bits are 0
      throw new IllegalArgumentException(
          "The amount columns in the matrix is not 8*2^x for some x > 1");
    }
    // Multiply by 8 because there are 8 bits in a byte
    int columns = input.get(0).length * 8;
    if (rows != columns)
      throw new IllegalArgumentException("The matrix is not square");
    // Check that all columns are of equal length
    for (int i = 1; i < rows; i++) {
      if (input.get(0).length != input.get(i).length)
        throw new IllegalArgumentException("Not all rows are of equal length");
    }
  }

  protected static void transposeAllByteBlocks(List<byte[]> input) {
    // Start by transposing one byte and 8 rows at a time using the trivial
    // O(n^2) algorithm
    for (int i = 0; i < input.size(); i = i + 8) {
      for (int j = 0; j < input.get(0).length * 8; j = j + 8) {
        transposeByteBlock(input, i, j);
      }
    }
  }

  protected static void transposeByteBlock(List<byte[]> input, int rowOffset,
      int columnOffset) {
    /**
     * By having 8 variables we hope that the JVM will only access the main
     * memory per iteration, to read a byte, rather than both reading and
     * writing to 8 bytes at different places in main memory
     */
    byte newRow0 = 0, newRow1 = 0, newRow2 = 0, newRow3 = 0, newRow4 = 0,
        newRow5 = 0, newRow6 = 0, newRow7 = 0;
    for (int k = 0; k < 8; k++) {
      byte currentRow = input.get(rowOffset + k)[columnOffset / 8];
      /**
       * First extract the bit of position (column) x for row x using AND (&),
       * then shift it to the leftmost position and do an unsigned rightshift to
       * move it into the correct position for the given row. Finally XOR (^)
       * the new bit into the current value for the row
       */
      newRow0 ^= (byte) (((currentRow & 0x80) << 0) >>> k);
      newRow1 ^= (byte) (((currentRow & 0x40) << 1) >>> k);
      newRow2 ^= (byte) (((currentRow & 0x20) << 2) >>> k);
      newRow3 ^= (byte) (((currentRow & 0x10) << 3) >>> k);
      newRow4 ^= (byte) (((currentRow & 0x08) << 4) >>> k);
      newRow5 ^= (byte) (((currentRow & 0x04) << 5) >>> k);
      newRow6 ^= (byte) (((currentRow & 0x02) << 6) >>> k);
      newRow7 ^= (byte) (((currentRow & 0x01) << 7) >>> k);
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
