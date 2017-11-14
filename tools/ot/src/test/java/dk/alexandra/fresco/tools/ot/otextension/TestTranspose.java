package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestTranspose {
  private static List<byte[]> getSquareMatrix() {
    return new ArrayList<>(
        Arrays.asList(
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

  /**** POSITIVE TESTS ****/
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
    List<byte[]> matrix = new ArrayList<>(128);
    for (int i = 0; i < 128; i++) {
      matrix.add(new byte[1024 / 8]);
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
      matrix.add(new byte[128 / 8]);
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
    List<byte[]> input = getSquareMatrix();
    Transpose.transposeAllByteBlocks(input);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x81, input.get(i)[0]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x81, input.get(i)[1]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x7E, input.get(i + 8)[0]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x7E, input.get(i + 8)[1]);
  }

  @Test
  public void testEklundh() {
    List<byte[]> input = getSquareMatrix();
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

    for (int i = 0; i < 16; i++)
      for (int j = 0; j < 2; j++)
        assertEquals(referenceMatrix.get(i)[j], input.get(i)[j]);
  }

  @Test
  public void testSquareTranspose() {
    List<byte[]> input = getSquareMatrix();
    List<byte[]> res = Transpose.transpose(input);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x81, res.get(i)[0]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x7E, res.get(i)[1]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x81, res.get(i + 8)[0]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x7E, res.get(i + 8)[1]);
  }
  
  // Transpose a wide matrix (more columns, than rows)
  @Test
  public void testWideTranspose() {
    List<byte[]> input = new ArrayList<>(
        Arrays.asList(
            new byte[] { (byte) 0xFF, (byte) 0x00 },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0x00 }));
    List<byte[]> res = Transpose.transpose(input);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x81, res.get(i)[0]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x7E, res.get(8 + i)[0]);
  }
  
  // Transpose a high matrix (more rows, than columns)
  @Test
  public void testTallTranspose() {
    List<byte[]> input = new ArrayList<>(
        Arrays.asList(
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0x00 },
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0xFF },
            new byte[] { (byte) 0x00 }));
    List<byte[]> res = Transpose.transpose(input);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x81, res.get(i)[0]);
    for (int i = 0; i < 8; i++)
      assertEquals((byte) 0x7E, res.get(i)[1]);
  }

  @Test
  public void testXor() {
    byte[] arr1 = { (byte) 0x00, (byte) 0x02, (byte) 0xFF };
    byte[] arr2 = { (byte) 0xF0, (byte) 0x02, (byte) 0xF0 };
    COTeShared.xor(arr1, arr2);
    assertEquals((byte) 0xF0, arr1[0]);
    assertEquals((byte) 0x00, arr1[1]);
    assertEquals((byte) 0x0F, arr1[2]);
  }

  @Test
  public void testXorList() {
    byte[] arr1 = { (byte) 0x00, (byte) 0x02, (byte) 0xFF };
    byte[] arr2 = { (byte) 0xF0, (byte) 0x02, (byte) 0xF0 };
    List<byte[]> list1 = new ArrayList<>(2);
    List<byte[]> list2 = new ArrayList<>(2);
    list1.add(arr1);
    list1.add(arr2);
    list2.add(arr2.clone());
    list2.add(arr1.clone());
    COTeShared.xor(list1, list2);
    assertEquals((byte) 0xF0, list1.get(0)[0]);
    assertEquals((byte) 0x00, list1.get(0)[1]);
    assertEquals((byte) 0x0F, list1.get(0)[2]);
    assertEquals((byte) 0xF0, list1.get(1)[0]);
    assertEquals((byte) 0x00, list1.get(1)[1]);
    assertEquals((byte) 0x0F, list1.get(1)[2]);
  }

  /**** NEGATIVE TESTS ****/
  @Test
  public void testWrongAmountOfRows() {
    boolean thrown;
    List<byte[]> matrix = getSquareMatrix();
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
    List<byte[]> matrix = new ArrayList<>(
        Arrays.asList(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }));
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
    List<byte[]> matrix = new ArrayList<>(
        Arrays.asList(new byte[] { (byte) 0xFF, (byte) 0xFF },
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
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
            new byte[] { (byte) 0x00, (byte) 0x00 }));
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
