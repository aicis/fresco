package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestModularReduction {

  @Test
  public void testModularReduction() {
    
    List<Integer> modulusSizes = Arrays.asList(8, 16, 32, 64, 128, 256, 512);

    for (Integer l : modulusSizes) {
      BigInteger modulus = ModulusFinder.findSuitableModulus(l);
      ModularReducer reducer = new ModularReducer(modulus);

      BigInteger x = BigInteger.valueOf(7);
      assertEquals(x.mod(modulus), reducer.mod(x));
      
      x = x.negate();
      assertEquals(x.mod(modulus), reducer.mod(x));
      
      x = modulus.multiply(modulus).subtract(BigInteger.ONE);
      assertEquals(x.mod(modulus), reducer.mod(x));

      x = x.negate();
      assertEquals(x.mod(modulus), reducer.mod(x));
      
      x = BigInteger.valueOf(0);
      assertEquals(x.mod(modulus), reducer.mod(x));

      x = modulus;
      assertEquals(x.mod(modulus), reducer.mod(x));

      x = modulus.negate();
      assertEquals(x.mod(modulus), reducer.mod(x));
    }
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
