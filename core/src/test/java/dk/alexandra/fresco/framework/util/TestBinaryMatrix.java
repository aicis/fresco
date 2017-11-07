package dk.alexandra.fresco.framework.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestBinaryMatrix {

  @Test
  public void testRows() {
    
    BinaryMatrix m1 = new BinaryMatrix(3, 3);
    BitSet row = new BitSet();
    row.set(0, true);
    row.set(1, true);
    row.set(2, true);
    m1.setRow(2, row);
    Assert.assertTrue(m1.get(2, 2));
    Assert.assertThat(m1.getRow(2), Is.is(row));
    
    BinaryMatrix m2 = m1.getRows(new int[]{2,0});
    Assert.assertThat(m2.getRow(0), Is.is(row));
    Assert.assertThat(m2.getRow(1), Is.is(new BitSet()));
  }

  @Test
  public void testColumns() {
    BinaryMatrix m1 = new BinaryMatrix(3, 3);
    BitSet column = new BitSet();
    column.set(0, true);
    column.set(1, true);
    column.set(2, true);
    m1.setColumn(2, column);
    Assert.assertTrue(m1.get(2, 2));
    Assert.assertThat(m1.getColumn(2), Is.is(column));
    
    BinaryMatrix m2 = m1.getColumns(new int[]{2,0});
    Assert.assertThat(m2.getColumn(0), Is.is(column));
    Assert.assertThat(m2.getColumn(1), Is.is(new BitSet()));
  }
  
  @Test
  public void testVectorMultiplication() {
    BinaryMatrix m1 = new BinaryMatrix(3, 3);
    BitSet column = new BitSet();
    column.set(0, true);
    column.set(1, true);
    column.set(2, true);
    m1.setColumn(2, column);
    m1.set(0, 1, true);

    /*
     * 0 1 1   1 = 0
     * 0 0 1 x 1 = 1 
     * 0 0 1   1 = 1 
     */
    BitSet result = m1.multiply(column);
    Assert.assertThat(result.get(0), Is.is(false));
    Assert.assertThat(result.get(1), Is.is(true));
    Assert.assertThat(result.get(2), Is.is(true));
  }
  
  @Test
  public void testMatrixMultiplication() {
    BinaryMatrix m1 = new BinaryMatrix(3, 3);
    BitSet column = new BitSet();
    column.set(0, true);
    column.set(1, true);
    
    m1.setColumn(2, column);
    m1.set(0, 1, true);

    BinaryMatrix m2 = new BinaryMatrix(3, 3);
    m2.set(0, 0, true);
    m2.set(0, 1, true);
    m2.set(0, 2, true);

    m2.set(2, 0, true);
    m2.set(2, 1, true);
    m2.set(2, 2, true);
    /*
     * 0 1 1   1 1 1 = 1 1 1 
     * 0 0 1 x 0 0 0 = 1 1 1 
     * 0 0 0   1 1 1 = 0 0 0
     */
    BinaryMatrix result = m1.multiply(m2);
    Assert.assertThat(result.get(0, 0), Is.is(true));
    Assert.assertThat(result.get(0, 1), Is.is(true));
    Assert.assertThat(result.get(0, 2), Is.is(true));
    Assert.assertThat(result.get(1, 0), Is.is(true));
    Assert.assertThat(result.get(1, 1), Is.is(true));
    Assert.assertThat(result.get(1, 2), Is.is(true));
    Assert.assertThat(result.get(2, 0), Is.is(false));
    Assert.assertThat(result.get(2, 1), Is.is(false));
    Assert.assertThat(result.get(2, 2), Is.is(false));
  }

  @Test
  public void testTranspose() {
    BinaryMatrix m1 = new BinaryMatrix(3, 3);
    BitSet row = new BitSet();
    row.set(0, true);
    row.set(1, true);
    row.set(2, true);
    m1.setRow(0, row);
    m1.setRow(1, row);
    m1.setRow(2, row);
    m1.clearColumn(1);

    BinaryMatrix result = m1.transpose();
    Assert.assertThat(result.get(0, 0), Is.is(true));
    Assert.assertThat(result.get(0, 1), Is.is(true));
    Assert.assertThat(result.get(0, 2), Is.is(true));
    Assert.assertThat(result.get(1, 0), Is.is(false));
    Assert.assertThat(result.get(1, 1), Is.is(false));
    Assert.assertThat(result.get(1, 2), Is.is(false));
    Assert.assertThat(result.get(2, 0), Is.is(true));
    Assert.assertThat(result.get(2, 1), Is.is(true));
    Assert.assertThat(result.get(2, 2), Is.is(true));
  }

  @Test
  public void testSerialization() {
    BinaryMatrix m1 =BinaryMatrix.getRandomMatrix(3, 3, new Random());

    byte[] bytes = m1.toByteArray();
    
    BinaryMatrix m2 = new BinaryMatrix(bytes);
    Assert.assertThat(m2, Is.is(m1));

  }
  
  @Test
  public void testMatrixAddition() {
    BinaryMatrix m1 = new BinaryMatrix(3, 3);
    BitSet column = new BitSet();
    column.set(0, true);
    column.set(1, true);

    m1.setColumn(2, column);
    m1.set(0, 1, true);

    BinaryMatrix m2 = new BinaryMatrix(3, 3);
    m2.set(0, 0, true);
    m2.set(0, 1, true);
    m2.set(0, 2, true);

    m2.set(2, 0, true);
    m2.set(2, 1, true);
    m2.set(2, 2, true);
    /*
     * 0 1 1   1 1 1 = 1 0 0 
     * 0 0 1 + 0 0 0 = 0 0 1 
     * 0 0 0   1 1 1 = 1 1 1
     */
    m1.add(m2);
    Assert.assertThat(m1.get(0, 0), Is.is(true));
    Assert.assertThat(m1.get(0, 1), Is.is(false));
    Assert.assertThat(m1.get(0, 2), Is.is(false));
    Assert.assertThat(m1.get(1, 0), Is.is(false));
    Assert.assertThat(m1.get(1, 1), Is.is(false));
    Assert.assertThat(m1.get(1, 2), Is.is(true));
    Assert.assertThat(m1.get(2, 0), Is.is(true));
    Assert.assertThat(m1.get(2, 1), Is.is(true));
    Assert.assertThat(m1.get(2, 2), Is.is(true));
    
    try{
      m1.add(new BinaryMatrix(2, 2));
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }

    try{
      m1.add(new BinaryMatrix(3, 2));
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
    
    try{
      m1.add(new BinaryMatrix(2, 3));
      Assert.fail("Should not be reachable");
    } catch(IllegalArgumentException e) {
    }
  }

  @Test
  public void testOuterProduct() {
    BitSet column = new BitSet();
    column.set(0, true);
    column.set(2, true);
    BinaryMatrix m1 = BinaryMatrix.outerProduct(3, 3, column, column);
    
    /*
     * 1   1 0 1 = 1 0 1 
     * 0 x       = 0 0 0 
     * 1         = 1 0 1
     */
    Assert.assertThat(m1.get(0, 0), Is.is(true));
    Assert.assertThat(m1.get(0, 1), Is.is(false));
    Assert.assertThat(m1.get(0, 2), Is.is(true));
    Assert.assertThat(m1.get(1, 0), Is.is(false));
    Assert.assertThat(m1.get(1, 1), Is.is(false));
    Assert.assertThat(m1.get(1, 2), Is.is(false));
    Assert.assertThat(m1.get(2, 0), Is.is(true));
    Assert.assertThat(m1.get(2, 1), Is.is(false));
    Assert.assertThat(m1.get(2, 2), Is.is(true));
  }

  @Test
  public void testFromColumns() {
    BitSet column = new BitSet();
    column.set(0, true);
    column.set(2, true);
    
    BitSet column2 = new BitSet();
    column2.set(0, true);
    List<BitSet> columns = new ArrayList<BitSet>();
    columns.add(column);
    columns.add(column2);
    BinaryMatrix m1 = BinaryMatrix.fromColumns(columns, 3);
    
    /*
     * 1 1   
     * 0 0   
     * 1 0  
     */
    Assert.assertThat(m1.get(0, 0), Is.is(true));
    Assert.assertThat(m1.get(0, 1), Is.is(true));
    Assert.assertThat(m1.get(1, 0), Is.is(false));
    Assert.assertThat(m1.get(1, 1), Is.is(false));
    Assert.assertThat(m1.get(2, 0), Is.is(true));
    Assert.assertThat(m1.get(2, 1), Is.is(false));
  }
  
  @Test
  public void testEquals() {
    BinaryMatrix m1 =BinaryMatrix.getRandomMatrix(3, 3, new Random(0));
    BinaryMatrix m2 =BinaryMatrix.getRandomMatrix(3, 3, new Random(0));
    
    Assert.assertTrue(m1.equals(m1));
    Assert.assertTrue(m1.equals(m2));
    Assert.assertFalse(m1.equals(null));
    Assert.assertFalse(m1.equals("Not a matrix"));
    Assert.assertFalse(m1.equals(new BinaryMatrix(2,2)));
    Assert.assertFalse(m1.equals(new BinaryMatrix(2,3)));
    Assert.assertFalse(m1.equals(new BinaryMatrix(3,2)));
  }
  
  @Test
  public void testToString() {
    BinaryMatrix m1 = new BinaryMatrix(2, 2);
    String expected = ("0 0 \n0 0 \n");
    Assert.assertThat(m1.toString().replaceAll("\r", ""), Is.is(expected));
  }
  
}

