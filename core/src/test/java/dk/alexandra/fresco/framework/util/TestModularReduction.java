package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

public class TestModularReduction {

  @Test
  public void testModularReduction() {
    BigInteger m = BigInteger.valueOf(39);
    
    // Input smaller than m^2
    BigInteger x = BigInteger.valueOf(1517); 
    BigInteger y = BigInteger.valueOf(-1517); 
    ModularReducer reducer = new ModularReducer(m);
    assertEquals(x.mod(m), reducer.mod(x));
    assertEquals(y.mod(m), reducer.mod(y));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSmallModulus() {
    BigInteger m = BigInteger.valueOf(3);
    new ModularReducer(m);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testTwoPowerModulus() {
    BigInteger m = BigInteger.valueOf(8);
    new ModularReducer(m);
  }
}
