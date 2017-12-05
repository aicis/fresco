package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.StrictBitVector;

public class TestTranspose {
  private static List<StrictBitVector> getSquareMatrix() {
    return new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16)));
  }

  private static List<byte[]> getSquareByteMatrix() {
    return new ArrayList<>(Arrays.asList(
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0x00, (byte) 0x00 },
        new byte[] { (byte) 0x00, (byte) 0x00 },
        new byte[] { (byte) 0x00, (byte) 0x00 },
        new byte[] { (byte) 0x00, (byte) 0x00 },
        new byte[] { (byte) 0x00, (byte) 0x00 },
        new byte[] { (byte) 0x00, (byte) 0x00 },
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0x00, (byte) 0x00 },
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0xFF, (byte) 0xFF },
        new byte[] { (byte) 0x00, (byte) 0x00 }));
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testSquareMatrix() {
    boolean thrown = false;
    try {
      Transpose.doSanityCheck(getSquareMatrix());
    } catch (Exception e) {
      thrown = true;
    }
    assertEquals(false, thrown);
  }

  @Test
  public void testBigMatrix() {
    boolean thrown = false;
    List<StrictBitVector> matrix = new ArrayList<>(128);
    for (int i = 0; i < 128; i++) {
      matrix.add(new StrictBitVector(1024));
    }
    try {
      Transpose.doSanityCheck(matrix);
    } catch (Exception e) {
      thrown = true;
    }
    assertEquals(false, thrown);
    thrown = false;
    matrix = new ArrayList<>(1024);
    for (int i = 0; i < 1024; i++) {
      matrix.add(new StrictBitVector(128));
    }
    try {
      Transpose.doSanityCheck(matrix);
    } catch (Exception e) {
      thrown = true;
    }
    assertEquals(false, thrown);
  }

  @Test
  public void testTransposeOneByteBlock() {
    /**
     * Construct the following bit matrix 
     * 1 1 1 1 1 1 1 1, 0xFF 
     * 0 0 0 0 0 0 0 0, 0x00 
     * 0 0 0 0 0 0 0 0, 0x00 
     * 0 0 0 0 0 0 0 1, 0x01 
     * 0 0 0 0 0 0 0 0, 0x00 
     * 0 0 0 0 0 0 0 0, 0x00
     * 0 0 0 0 0 0 0 0, 0x00
     * 1 1 1 1 1 1 1 1, 0xFF
     */
    List<byte[]> input = new ArrayList<>(
        Arrays.asList(
            new byte[] { (byte) 0xFF }, 
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0x00 }, 
            new byte[] { (byte) 0x01 },
            new byte[] { (byte) 0x00 }, 
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0x00 }, 
            new byte[] { (byte) 0xFF }));
    Transpose.transposeByteBlock(input, 0, 0); 
    /**
     * Verify that the result is 
     * 1 0 0 0 0 0 0 1 0x81
     * 1 0 0 0 0 0 0 1 0x81
     * 1 0 0 0 0 0 0 1 0x81
     * 1 0 0 0 0 0 0 1 0x81
     * 1 0 0 0 0 0 0 1 0x81 
     * 1 0 0 0 0 0 0 1 0x81 
     * 1 0 0 0 0 0 0 1 0x81 
     * 1 0 0 1 0 0 0 1 0x91
     */
    for (int i = 0; i < 7; i++) {
      assertEquals((byte) 0x81, input.get(i)[0]);
    }
    assertEquals((byte) 0x91, input.get(7)[0]);
  }

  @Test
  public void testTransposeFourByteBlocks() {
    List<byte[]> input = getSquareByteMatrix();
    Transpose.transposeAllByteBlocks(input);
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x81, input.get(i)[0]);
    }
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x81, input.get(i)[1]);
    }
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x7E, input.get(i + 8)[0]);
    }
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x7E, input.get(i + 8)[1]);
    }
  }

  @Test
  public void testEklundh() {
    List<byte[]> input = getSquareByteMatrix();
    Transpose.doEklundh(input);
    List<byte[]> referenceMatrix = new ArrayList<>(
        Arrays.asList(
            new byte[] { (byte) 0xFF, (byte) 0x00 },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0x00 },
            new byte[] { (byte) 0xFF, (byte) 0x00 },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0x00 }));

    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(referenceMatrix.get(i)[j], input.get(i)[j]);
      }
    }
  }

  @Test
  public void testSquareTranspose() {
    List<StrictBitVector> input = getSquareMatrix();
    List<StrictBitVector> res = Transpose.transpose(input);
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x81, res.get(i).toByteArray()[0]);
      assertEquals((byte) 0x7E, res.get(i).toByteArray()[1]);
    }
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x81, res.get(i + 8).toByteArray()[0]);
      assertEquals((byte) 0x7E, res.get(i + 8).toByteArray()[1]);
    }
  }
  
  // Transpose a wide matrix (more columns, than rows)
  @Test
  public void testWideTranspose() {
    List<StrictBitVector> input = new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0x00 }, 16)));
    List<StrictBitVector> res = Transpose.transpose(input);
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x81, res.get(i).toByteArray()[0]);
    }
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x7E, res.get(8 + i).toByteArray()[0]);
    }
  }
  
  // Transpose a high matrix (more rows, than columns)
  @Test
  public void testTallTranspose() {
    List<StrictBitVector> input = new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8),
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8),
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0xFF }, 8),
            new StrictBitVector(new byte[] { (byte) 0x00 }, 8)));
    List<StrictBitVector> res = Transpose.transpose(input);
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x81, res.get(i).toByteArray()[0]);
    }
    for (int i = 0; i < 8; i++) {
      assertEquals((byte) 0x7E, res.get(i).toByteArray()[1]);
    }
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testWrongAmountOfRows() {
    boolean thrown;
    List<StrictBitVector> matrix = getSquareMatrix();
    matrix.remove(0);
    thrown = false;
    try {
      Transpose.transpose(matrix);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount rows in the matrix is not 8*2^x for some x > 1",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testWrongAmountOfColumns() {
    boolean thrown;
    List<StrictBitVector> matrix = new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }, 24)));
    thrown = false;
    try {
      Transpose.doSanityCheck(matrix);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The amount columns in the matrix is not 8*2^x for some x > 1",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testNonEqualColumns() {
    boolean thrown;
    List<StrictBitVector> matrix = new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }, 16),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 24),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }, 16)));
    thrown = false;
    try {
      Transpose.doSanityCheck(matrix);
    } catch (IllegalArgumentException e) {
      assertEquals("Not all rows are of equal length", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}