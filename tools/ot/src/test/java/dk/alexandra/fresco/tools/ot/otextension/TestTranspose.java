package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestTranspose {
  private Method doSanityCheck;

  private static List<StrictBitVector> getSquareMatrix() {
    return new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 })));
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

  /**
   * Setup a local Transpose class.
   */
  @Before
  public void setup() throws NoSuchMethodException, SecurityException {
    doSanityCheck = Transpose.class.getDeclaredMethod("doSanityCheck",
        List.class);
    doSanityCheck.setAccessible(true);
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testSquareMatrix() {
    boolean thrown = false;
    try {
      doSanityCheck.invoke(Transpose.class, getSquareMatrix());
    } catch (Exception e) {
      thrown = true;
    }
    assertEquals(false, thrown);
  }

  @Test
  public void testNewTranspose() {
    // Verify that it is possible to construct a Transpose object
    new Transpose();
  }

  @Test
  public void testBigMatrix() {
    boolean thrown = false;
    List<StrictBitVector> matrix = new ArrayList<>(128);
    for (int i = 0; i < 128; i++) {
      matrix.add(new StrictBitVector(1024));
    }
    try {
      doSanityCheck.invoke(Transpose.class, matrix);
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
      doSanityCheck.invoke(Transpose.class, matrix);
    } catch (Exception e) {
      thrown = true;
    }
    assertEquals(false, thrown);
  }

  @Test
  public void testTransposeOneByteBlock() throws NoSuchMethodException,
      IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    /**
     * Construct the following bit matrix
     * 1 1 1 1 1 1 1 1, 0xFF
     * 0 0 0 0 0 0 0 0, 0x00
     * 0 0 0 0 0 0 0 0, 0x00
     * 0 0 0 0 0 0 0 1, 0x01
     * 0 0 0 0 0 0 0 0, 0x00
     * 0 0 0 0 0 0 0 0, 0x00
     * 0 0 0 0 0 0 0 0, 0x00
     * 1 1 1 1 1 1 1 1, 0xFF.
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
    Method transposeByteBlock = Transpose.class.getDeclaredMethod(
        "transposeByteBlock", List.class, int.class, int.class);
    transposeByteBlock.setAccessible(true);
    transposeByteBlock.invoke(Transpose.class, input, 0, 0);
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
  public void testTransposeFourByteBlocks() throws NoSuchMethodException,
      SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    List<byte[]> input = getSquareByteMatrix();
    Method transposeAllByteBlocks = Transpose.class.getDeclaredMethod(
        "transposeAllByteBlocks", List.class);
    transposeAllByteBlocks.setAccessible(true);
    transposeAllByteBlocks.invoke(Transpose.class, input);
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
  public void testEklundh() throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    List<byte[]> input = getSquareByteMatrix();
    Method doEklundh = Transpose.class.getDeclaredMethod("doEklundh",
        List.class);
    doEklundh.setAccessible(true);
    doEklundh.invoke(Transpose.class, input);
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
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0x00 })));
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
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00 })));
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
    matrix = new ArrayList<>();
    // Notice 24 is a product of 8, but not a 2 power
    for (int i = 0; i < 24; i++) {
      matrix.add(new StrictBitVector(24));
    }
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
  public void testWrongAmountOfColumns() throws IllegalAccessException,
      InvocationTargetException {
    boolean thrown;
    List<StrictBitVector> matrix = new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF })));
    thrown = false;
    try {
      doSanityCheck.invoke(Transpose.class, matrix);
    } catch (InvocationTargetException e) {
      assertEquals(
          "The amount columns in the matrix is not 8*2^x for some x > 1",
          e.getTargetException().getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testNonEqualColumns() throws IllegalAccessException,
      InvocationTargetException {
    boolean thrown;
    List<StrictBitVector> matrix = new ArrayList<>(
        Arrays.asList(
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }),
            new StrictBitVector(new byte[] { (byte) 0x00, (byte) 0x00 })));
    thrown = false;
    try {
      doSanityCheck.invoke(Transpose.class, matrix);
    } catch (InvocationTargetException e) {
      assertEquals("Not all rows are of equal length", e.getTargetException()
          .getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}
