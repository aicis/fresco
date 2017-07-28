package dk.alexandra.fresco.lib.math.polynomial;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestPolynomial {

  private PolynomialFactory polyFactory = new PolynomialFactoryImpl();

  
  @Test
  public void testPolynomial() {
  
    Polynomial p = polyFactory.createPolynomial(4);
    
    Assert.assertThat(p.getMaxDegree(), Is.is(4));
    for(int i = 0; i< 4; i++) {
      p.setCoefficient(i, new DummyArithmeticSInt(i));  
    }
    
    for(int i = 0; i< 4; i++) {
      Assert.assertThat(p.getCoefficient(i), Is.is(new DummyArithmeticSInt(i)));  
    }    
    
    p.setMaxDegree(6);
    for(int i = 0; i< 4; i++) {
      Assert.assertThat(p.getCoefficient(i), Is.is(new DummyArithmeticSInt(i)));  
    }
    Assert.assertNull(p.getCoefficient(4));
    Assert.assertNull(p.getCoefficient(5));
    p.setMaxDegree(3);
    for(int i = 0; i< 3; i++) {
      Assert.assertThat(p.getCoefficient(i), Is.is(new DummyArithmeticSInt(i)));  
    }
    
    try {
      p.getCoefficient(4);
      Assert.fail("Should throw exception.");
    } catch (ArrayIndexOutOfBoundsException e){
    }
  }
  
}

