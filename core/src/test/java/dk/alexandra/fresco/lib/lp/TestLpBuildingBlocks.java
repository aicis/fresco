package dk.alexandra.fresco.lib.lp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestLpBuildingBlocks {
  
  @Test
  public void testEnums(){
    assertThat(PivotRule.valueOf("BLAND"), is(PivotRule.BLAND));
    assertThat(PivotRule.valueOf("DANZIG"), is(PivotRule.DANZIG));
  }
  
  @Test
  public void testMatrix() {
    Matrix<String> matrix = new Matrix<String>(3, 3, (i) -> {
      ArrayList<String> row = new ArrayList<>(3);
      row.add(i + "0");
      row.add(i + "1");
      row.add(i + "2");
      return row;
    });
    assertThat(matrix.getHeight(), is(3));
    assertThat(matrix.getWidth(), is(3));
    assertThat(matrix.getRow(1).get(2), is("12"));
    List<String> col = Arrays.asList("02", "12", "22");
    assertThat(matrix.getColumn(2), is(col));
    String toString = matrix.toString();
    assertTrue(toString.contains("matrix"));
    assertTrue(toString.contains("00, 01, 02"));
    assertTrue(toString.contains("10, 11, 12"));
    assertTrue(toString.contains("20, 21, 22"));    
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testLpTableuBadDimensions1() {
    Matrix<DRes<SInt>> matrix = new Matrix<>(4, 3, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(3);
      row.addAll(Arrays.asList(null, null, null));
      return row;
    });
    ArrayList<DRes<SInt>> vector = new ArrayList<>(4);
    vector.addAll(Arrays.asList(null, null, null, null));
    DRes<SInt> z = null;    
    new LPTableau(matrix, vector, vector, z);
    fail("Should not be reachable");
  } 
  
  @Test(expected = IllegalArgumentException.class)
  public void testLpTableuBadDimensions2() {
    Matrix<DRes<SInt>> matrix = new Matrix<>(4, 3, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(3);
      row.addAll(Arrays.asList(null, null, null));
      return row;
    });
    ArrayList<DRes<SInt>> vector = new ArrayList<>(3);
    vector.addAll(Arrays.asList(null, null, null));
    DRes<SInt> z = null;    
    new LPTableau(matrix, vector, vector, z);
    fail("Should not be reachable");
  } 
  
  @Test(expected = IllegalArgumentException.class)
  public void testLpTableuBadDimensions3() {
    Matrix<DRes<SInt>> matrix = new Matrix<>(4, 3, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(3);
      row.addAll(Arrays.asList(null, null, null));
      return row;
    });
    ArrayList<DRes<SInt>> vector = new ArrayList<>(5);
    vector.addAll(Arrays.asList(null, null, null, null, null));
    ArrayList<DRes<SInt>> vector2 = new ArrayList<>(5);
    vector2.addAll(Arrays.asList(null, null, null, null, null));
    DRes<SInt> z = null;    
    new LPTableau(matrix, vector, vector2, z);
    fail("Should not be reachable");
  }
    
  @Test(expected = IllegalArgumentException.class)
  public void testLpSolverBadDimensions1() {
    Matrix<DRes<SInt>> matrix = new Matrix<>(4, 3, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(3);
      row.addAll(Arrays.asList(null, null, null));
      return row;
    });
    ArrayList<DRes<SInt>> b = new ArrayList<>(4);
    b.addAll(Arrays.asList(null, null, null, null));
    ArrayList<DRes<SInt>> f = new ArrayList<>(3);
    f.addAll(Arrays.asList(null, null, null));
    DRes<SInt> z = null;    
    LPTableau tab = new LPTableau(matrix, b, f, z);
    new LPSolver(LPSolver.PivotRule.DANZIG, tab, matrix, null, null);
    fail("Should not be reachable");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testLpSolverBadDimensions2() {
    Matrix<DRes<SInt>> matrix = new Matrix<>(4, 4, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(4);
      row.addAll(Arrays.asList(null, null, null, null));
      return row;
    });
    ArrayList<DRes<SInt>> b = new ArrayList<>(4);
    b.addAll(Arrays.asList(null, null, null, null));
    ArrayList<DRes<SInt>> f = new ArrayList<>(4);
    f.addAll(Arrays.asList(null, null, null, null));
    DRes<SInt> z = null;    
    LPTableau tab = new LPTableau(matrix, b, f, z);
    new LPSolver(LPSolver.PivotRule.DANZIG, tab, matrix, null, null);
    fail("Should not be reachable");
  }
}
