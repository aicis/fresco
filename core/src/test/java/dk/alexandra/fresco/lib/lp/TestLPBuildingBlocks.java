package dk.alexandra.fresco.lib.lp;

import java.math.BigInteger;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestLPBuildingBlocks {

  @Test
  public void testMatrix(){
    Matrix<String> matrix = new Matrix<String>(new String[3][3]);
    matrix.getIthRow(0)[0] = "00";
    matrix.getIthRow(0)[1] = "01";
    matrix.getIthRow(0)[2] = "02";
    matrix.getIthRow(1)[0] = "10";
    matrix.getIthRow(1)[1] = "11";
    matrix.getIthRow(1)[2] = "12";
    matrix.getIthRow(2)[0] = "20";
    matrix.getIthRow(2)[1] = "21";
    matrix.getIthRow(2)[2] = "22";
    Assert.assertThat(matrix.getHeight(), Is.is(3));
    Assert.assertThat(matrix.getWidth(), Is.is(3));
    Assert.assertThat(matrix.getElement(1, 2), Is.is("12"));
    try{
      String[] col = new String[2];
      matrix.getIthColumn(2, col);
      Assert.fail("Column and placeholder are not of similar length and should fail");
    } catch(RuntimeException e){
    }
    String[] col = new String[3];
    
    Assert.assertThat(matrix.getIthColumn(2, col), Is.is(new String[]{"02", "12", "22"}));

    String toString = matrix.toString();
    String expected = "[00, 01, 02]\n"
                    + "[10, 11, 12]\n"
                    + "[20, 21, 22]\n";
    Assert.assertThat(toString, Is.is(expected));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testLPTableuBadDimensions1(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][3]);
    
    new LPTableau(matrix, new SInt[4], new SInt[4], null);
    Assert.fail("Should not be reachable");
  }  

  @Test(expected=IllegalArgumentException.class)
  public void testLPTableuBadDimensions2(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][3]);
    
    new LPTableau(matrix, new SInt[3], new SInt[3], null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testLPTableuBadDimensions3(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][3]);
    
    new LPTableau(matrix, new SInt[3], new SInt[4], null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testLPSolverProtocolBadDimensions1(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][3]);
    new LPSolverProtocol(null, matrix, null, null, null, null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testLPSolverProtocolBadDimensions2(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    LPTableau tableau = new LPTableau(matrix, new SInt[4], new SInt[4], null);
    new LPSolverProtocol(tableau, matrix, null, null, null, null);
    Assert.fail("Should not be reachable");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testEnteringVariableProtocolBadDimensions1(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][3]);
    new EnteringVariableProtocol(null, matrix, null, null, null, null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testEnteringVariableProtocolBadDimensions2(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    LPTableau tableau = new LPTableau(matrix, new SInt[4], new SInt[4], null);
    new EnteringVariableProtocol(tableau, matrix, null, null, null, null);
    Assert.fail("Should not be reachable");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testEnteringVariableProtocolBadDimensions3(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    Matrix<SInt> matrixTable = new Matrix<>(new SInt[3][3]);
    LPTableau tableau = new LPTableau(matrixTable, new SInt[3], new SInt[3], null);
    new EnteringVariableProtocol(tableau, matrix, new SInt[5], null, null, null);
    Assert.fail("Should not be reachable");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testBlandEnteringVariableProtocolBadDimensions1(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][3]);
    new BlandEnteringVariableProtocol(null, matrix, null, null, null, null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testBlandEnteringVariableProtocolBadDimensions2(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    LPTableau tableau = new LPTableau(matrix, new SInt[4], new SInt[4], null);
    new BlandEnteringVariableProtocol(tableau, matrix, null, null, null, null);
    Assert.fail("Should not be reachable");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testBlandEnteringVariableProtocolBadDimensions3(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    Matrix<SInt> matrixTable = new Matrix<>(new SInt[3][3]);
    LPTableau tableau = new LPTableau(matrixTable, new SInt[3], new SInt[3], null);
    new BlandEnteringVariableProtocol(tableau, matrix, new SInt[5], null, null, null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testExitingVariableProtocolBadDimensions1(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][3]);
    new ExitingVariableProtocol(null, matrix, null, null, null, null, null, null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testExitingVariableProtocolBadDimensions2(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    LPTableau tableau = new LPTableau(matrix, new SInt[4], new SInt[4], null);
    new ExitingVariableProtocol(tableau, matrix, null, null, null, null, null, null);
    Assert.fail("Should not be reachable");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testExitingVariableProtocolBadDimensions3(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    Matrix<SInt> matrixTable = new Matrix<>(new SInt[3][3]);
    LPTableau tableau = new LPTableau(matrixTable, new SInt[3], new SInt[3], null);
    new ExitingVariableProtocol(tableau, matrix, new SInt[5], null, null, null, null, null);
    Assert.fail("Should not be reachable");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testExitingVariableProtocolBadDimensions4(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    Matrix<SInt> matrixTable = new Matrix<>(new SInt[3][3]);
    LPTableau tableau = new LPTableau(matrixTable, new SInt[3], new SInt[3], null);
    new ExitingVariableProtocol(tableau, matrix, new SInt[3], new SInt[5], null, null, null, null);
    Assert.fail("Should not be reachable");
  }
  @Test(expected=IllegalArgumentException.class)
  public void testExitingVariableProtocolBadDimensions5(){
    Matrix<SInt> matrix = new Matrix<>(new SInt[4][4]);
    Matrix<SInt> matrixTable = new Matrix<>(new SInt[3][3]);
    LPTableau tableau = new LPTableau(matrixTable, new SInt[3], new SInt[3], null);
    new ExitingVariableProtocol(tableau, matrix, new SInt[3], new SInt[3], new SInt[3], null, null, null);
    Assert.fail("Should not be reachable");
  }
  
  @Test
  public void testSimpleLPPrefix() {
    Matrix<SInt> m = new Matrix<SInt>(new SInt[4][4]);
    LPTableau tableau = new LPTableau(m, new SInt[4], new SInt[4], null);
    SInt pivot = new DummyArithmeticSInt(25);
    
    SInt[] basis = new SInt[]{new DummyArithmeticSInt(1), new DummyArithmeticSInt(2), new DummyArithmeticSInt(3)};
    
    SimpleLPPrefix prefix = new SimpleLPPrefix(m, tableau, pivot, basis, null);
    
    Assert.assertThat(prefix.getBasis().length, Is.is(3));
    Assert.assertThat(((DummyArithmeticSInt)prefix.getBasis()[1]).getValue(), Is.is(BigInteger.valueOf(2)));
  }
}
