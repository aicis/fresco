package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestBinaryMatrix {

  @Test
  public void testConstructors() {
    BinaryMatrix bm1 = new BinaryMatrix(8,2);
    assertEquals(8, bm1.getHeight());
    assertEquals(2, bm1.getWidth());
    for (int i = 0; i < bm1.getWidth(); i++) {
      for (int j = 0; j < bm1.getHeight(); j++) {
        assertFalse(bm1.get(j, i));
      }
    }
    byte[] array = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x01};
    BinaryMatrix bm2 = new BinaryMatrix(array);
    assertEquals(8, bm2.getHeight());
    assertEquals(2, bm2.getWidth());
    assertTrue(bm2.get(0, 0));
    assertTrue(bm2.get(0, 1));
    for (int i = 0; i < bm2.getWidth(); i++) {
      for (int j = 1; j < bm2.getHeight(); j++) {
        assertFalse(bm2.get(j, i));
      }
    }
    boolean exception = false;
    try {
      new BinaryMatrix(-8,2);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      new BinaryMatrix(8,-2);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
  }

  @Test
  public void testGettersAndSetters() {
    BinaryMatrix bm1 = new BinaryMatrix(4,4);
    // set (identity matrix)
    bm1.set(0, 0, true);
    bm1.set(1, 1, true);
    bm1.set(2, 2, true);
    bm1.set(3, 3, true);
    bm1.set(0, 1, true);
    bm1.set(0, 1, false);
    // get
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        boolean entry = bm1.get(i, j);
        if (i == j) {
          assertTrue(entry);
        } else {
          assertFalse(entry);
        }
      }
    }
    // getRow
    BitSet row1 = bm1.getRow(3);
    assertEquals(bm1.get(3, 0), row1.get(0));
    assertEquals(bm1.get(3, 1), row1.get(1));
    assertEquals(bm1.get(3, 2), row1.get(2));
    assertEquals(bm1.get(3, 3), row1.get(3));
    // getRows
    BinaryMatrix bm2 = bm1.getRows(new int[] {0, 3});
    assertEquals(2, bm2.getHeight());
    assertEquals(4, bm2.getWidth());
    assertEquals(bm1.get(0, 0), bm2.get(0, 0));
    assertEquals(bm1.get(0, 1), bm2.get(0, 1));
    assertEquals(bm1.get(0, 2), bm2.get(0, 2));
    assertEquals(bm1.get(0, 3), bm2.get(0, 3));
    assertEquals(bm1.get(3, 0), bm2.get(1, 0));
    assertEquals(bm1.get(3, 1), bm2.get(1, 1));
    assertEquals(bm1.get(3, 2), bm2.get(1, 2));
    assertEquals(bm1.get(3, 3), bm2.get(1, 3));
    // getColumn
    BitSet column1 = bm1.getColumn(1);
    assertEquals(bm1.get(0, 1), column1.get(0));
    assertEquals(bm1.get(1, 1), column1.get(1));
    assertEquals(bm1.get(2, 1), column1.get(2));
    assertEquals(bm1.get(3, 1), column1.get(3));
    // getColumns
    BinaryMatrix columns = bm1.getColumns(new int[] {1, 2});
    assertEquals(bm1.get(0, 1), columns.get(0, 0));
    assertEquals(bm1.get(1, 1), columns.get(1, 0));
    assertEquals(bm1.get(2, 1), columns.get(2, 0));
    assertEquals(bm1.get(3, 1), columns.get(3, 0));
    assertEquals(bm1.get(0, 2), columns.get(0, 1));
    assertEquals(bm1.get(1, 2), columns.get(1, 1));
    assertEquals(bm1.get(2, 2), columns.get(2, 1));
    assertEquals(bm1.get(3, 2), columns.get(3, 1));
    // clearColumn
    bm1.clearColumn(0);
    assertTrue(bm1.getColumn(0).isEmpty());
    // setRow
    BitSet row2 = BitSet.valueOf(new byte[] {(byte)15}); // 00001111
    bm1.setRow(1, row2);
    assertEquals(row2, bm1.getRow(1));
    // setColumn
    BitSet column2 = BitSet.valueOf(new byte[] {(byte)15}); // 00001111
    bm1.setColumn(1, column2);
    assertEquals(column2, bm1.getColumn(1));
  }

  @Test
  public void testIndexOutOfBoundExceptions() {
    BinaryMatrix binaryMatrix = new BinaryMatrix(8,2);
    // Set
    boolean exception = false;
    try {
      binaryMatrix.set(-1, 0, true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      binaryMatrix.set(0, -1, true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      binaryMatrix.set(8, 0, true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      binaryMatrix.set(0, 2, true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    // Get
    exception = false;
    try {
      binaryMatrix.get(-1, 0);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      binaryMatrix.get(0, -1);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      binaryMatrix.get(8, 0);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      binaryMatrix.get(0, 2);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
  }

  @Test
  public void testTranspose() {
    // Set up an 8 x 2 matrix
    byte[] array = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x09};
    BinaryMatrix binaryMatrix = new BinaryMatrix(array);
    BinaryMatrix transpose = binaryMatrix.transpose();
    assertEquals(binaryMatrix.getWidth(), transpose.getHeight());
    assertEquals(binaryMatrix.getHeight(), transpose.getWidth());
    assertEquals(binaryMatrix.getColumn(0), transpose.getRow(0));
    assertEquals(binaryMatrix.getColumn(1), transpose.getRow(1));
    assertEquals(binaryMatrix, transpose.transpose());
  }

  @Test
  public void testAdd() {
    byte[] array = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x09};
    BinaryMatrix binaryMatrix = new BinaryMatrix(array);
    assertFalse(binaryMatrix.getColumn(0).isEmpty());
    assertFalse(binaryMatrix.getColumn(1).isEmpty());
    binaryMatrix.add(binaryMatrix);
    assertTrue(binaryMatrix.getColumn(0).isEmpty());
    assertTrue(binaryMatrix.getColumn(1).isEmpty());
    byte[] array2 = new byte[] {0x00, 0x08, 0x00, 0x02, 0x00, 0x01};
    BinaryMatrix binaryMatrix2 = new BinaryMatrix(array2);
    binaryMatrix.add(binaryMatrix2);
    assertTrue(binaryMatrix.getColumn(0).isEmpty());
    assertEquals(1, binaryMatrix.getColumn(1).cardinality());
    assertTrue(binaryMatrix.get(0,1));
    // exceptions
    BinaryMatrix binaryMatrix3 = new BinaryMatrix(8,3);
    boolean exception = false;
    try {
      binaryMatrix.add(binaryMatrix3);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue("BinaryMatrix failed to throw exception", exception);
    BinaryMatrix binaryMatrix4 = new BinaryMatrix(9,2);
    exception = false;
    try {
      binaryMatrix.add(binaryMatrix4);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue("BinaryMatrix failed to throw exception", exception);
  }

  @Test
  public void testMultiply() {
    byte[] array1 = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x09};
    BinaryMatrix binaryMatrix1 = new BinaryMatrix(array1);
    BitSet vector = new BitSet(2);
    assertTrue(binaryMatrix1.multiply(vector).isEmpty());
    vector.set(1);
    assertEquals(binaryMatrix1.getColumn(1), binaryMatrix1.multiply(vector));
    byte[] array2 = new byte[] {0x00, 0x02, 0x00, 0x02, 0x09};
    BinaryMatrix identity = new BinaryMatrix(array2);
    assertEquals(binaryMatrix1, binaryMatrix1.multiply(identity));
    assertEquals(new BinaryMatrix(8,2), binaryMatrix1.multiply(new BinaryMatrix(2, 2)));
    BinaryMatrix tooLarge = new BinaryMatrix(3,3);
    boolean exception = false;
    try {
      binaryMatrix1.multiply(tooLarge);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
    BinaryMatrix tooSmall = new BinaryMatrix(1,1);
    exception = false;
    try {
      binaryMatrix1.multiply(tooSmall);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);

  }

  @Test
  public void testOuterProduct() {
    BitSet b1 = new BitSet(3);
    b1.set(0);
    b1.set(2);
    BitSet b2 = BitSet.valueOf(new byte[] {0x11});
    BinaryMatrix binaryMatrix = BinaryMatrix.outerProduct(3, 8, b1, b2);
    assertEquals(3, binaryMatrix.getHeight());
    assertEquals(8, binaryMatrix.getWidth());
    assertEquals(b2, binaryMatrix.getRow(0));
    assertEquals(new BitSet(8), binaryMatrix.getRow(1));
    assertEquals(b2, binaryMatrix.getRow(2));
  }

  @Test
  public void testGetRandomMatrix() {
    Random rand1 = new Random(0);
    BinaryMatrix binaryMatrix1 = new BinaryMatrix(8, 2);
    for (int j = 0; j < binaryMatrix1.getWidth(); j++) {
      for (int i = 0; i < binaryMatrix1.getHeight(); i++) {
        binaryMatrix1.set(i, j, rand1.nextBoolean());
      }
    }
    Random rand2 = new Random(0);
    BinaryMatrix binaryMatrix2 = BinaryMatrix.getRandomMatrix(8, 2, rand2);
    assertEquals(binaryMatrix1, binaryMatrix2);
  }

  @Test
  public void testFromColumns() {
    BitSet column1 = BitSet.valueOf(new byte[] { 0x11, 0x09});
    BitSet column2 = BitSet.valueOf(new byte[] { 0x02, 0x10});
    List<BitSet> list = new ArrayList<>();
    list.add(column1);
    list.add(column2);
    BinaryMatrix binaryMatrix1 = BinaryMatrix.fromColumns(list, 16);
    assertEquals(16, binaryMatrix1.getHeight());
    assertEquals(2, binaryMatrix1.getWidth());
    assertEquals(column1, binaryMatrix1.getColumn(0));
    assertEquals(column2, binaryMatrix1.getColumn(1));
    BinaryMatrix binaryMatrix2 = BinaryMatrix.fromColumns(list, 8);
    assertEquals(8, binaryMatrix2.getHeight());
    column1.clear(8, 16);
    column2.clear(8, 16);
    assertEquals(column1, binaryMatrix2.getColumn(0));
    assertEquals(column2, binaryMatrix2.getColumn(1));
  }

  @Test
  public void testToByteArray() {
    byte[] array1 = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x09};
    BinaryMatrix binaryMatrix1 = new BinaryMatrix(array1);
    assertArrayEquals(array1, binaryMatrix1.toByteArray());
  }

  @Test
  public void testToString() {
    byte[] array1 = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x09};
    BinaryMatrix binaryMatrix1 = new BinaryMatrix(array1);
    String s = "1 1 \n0 0 \n0 0 \n0 1 \n0 0 \n0 0 \n0 0 \n0 0 \n";
    assertEquals(s, binaryMatrix1.toString());
  }

  @Test
  public void testEquals() {
    byte[] array1 = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x09};
    byte[] array2 = new byte[] {0x00, 0x08, 0x00, 0x02, 0x01, 0x08};
    BinaryMatrix binaryMatrix1 = new BinaryMatrix(array1);
    BinaryMatrix binaryMatrix2 = new BinaryMatrix(array2);
    assertNotEquals(binaryMatrix1, binaryMatrix2);
    assertNotEquals(binaryMatrix1, "Test");
    assertNotEquals(binaryMatrix1, null);
    assertEquals(binaryMatrix1, binaryMatrix1);
    BinaryMatrix binaryMatrix3 = new BinaryMatrix(array1);
    assertEquals(binaryMatrix1, binaryMatrix3);
    BinaryMatrix binaryMatrix4 = new BinaryMatrix(9, 2);
    assertNotEquals(binaryMatrix1, binaryMatrix4);
    BinaryMatrix binaryMatrix5 = new BinaryMatrix(8, 1);
    assertNotEquals(binaryMatrix1, binaryMatrix5);
  }
}
