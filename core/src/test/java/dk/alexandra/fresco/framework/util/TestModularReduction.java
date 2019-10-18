package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class TestModularReduction {

  private void testModularReduction(List<BigInteger> moduli) {
    for (BigInteger modulus : moduli) {
      ModularReductionAlgorithm reducer = ModularReductionAlgorithm.getReductionAlgorithm(modulus);

      // x = 7
      BigInteger x = BigInteger.valueOf(7);
      assertEquals(x.mod(modulus), reducer.apply(x));

      // x = -7
      x = x.negate();
      assertEquals(x.mod(modulus), reducer.apply(x));

      // x = m^2 - 1
      x = modulus.multiply(modulus).subtract(BigInteger.ONE);
      assertEquals(x.mod(modulus), reducer.apply(x));

      // x = -m^2 + 1
      x = x.negate();
      assertEquals(x.mod(modulus), reducer.apply(x));

      // x = 0
      x = BigInteger.valueOf(0);
      assertEquals(x.mod(modulus), reducer.apply(x));

      // x = m
      x = modulus;
      assertEquals(x.mod(modulus), reducer.apply(x));

      // x = -m
      x = modulus.negate();
      assertEquals(x.mod(modulus), reducer.apply(x));
    }
  }

  @Test
  public void testGeneralModularReduction() {
    List<Integer> modulusSizes = Arrays.asList(8, 16, 32, 64, 128, 256, 512);
    List<BigInteger> moduli = modulusSizes.stream().map(ModulusFinder::findSuitableModulus).collect(Collectors.toList());
    testModularReduction(moduli);
  }

  @Test
  public void testModularReduction2k() {
    List<Integer> modulusSizes = Arrays.asList(8, 16, 32, 64, 128, 256, 512);
    List<BigInteger> moduli = modulusSizes.stream().map(BigInteger.ONE::shiftLeft).collect(Collectors.toList());
    testModularReduction(moduli);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSmallModulus() {
    BigInteger m = BigInteger.valueOf(3);
    ModularReductionAlgorithm.getReductionAlgorithm(m);
  }

}
