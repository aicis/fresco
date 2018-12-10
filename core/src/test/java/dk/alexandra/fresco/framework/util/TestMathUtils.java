package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestMathUtils {

  private ModulusBigInteger modulus = new ModulusBigInteger("113");

  @Test
  public void testIsQuadraticResidue() {
    assertEquals(true, MathUtils.isQuadraticResidue(new BigInteger("2"), modulus));
    assertEquals(false, MathUtils.isQuadraticResidue(new BigInteger("23"), modulus));
  }

  @Test
  public void testModularSqrt() {
    BigInteger value = new BigInteger("2");
    BigInteger actual = MathUtils.modularSqrt(value, modulus);
    BigInteger expected = new BigInteger("62");
    assertEquals(expected, actual);
  }

  @Test
  public void testModularSqrtBigModulus() {
    // do 0, 1 and p - 1
    ModulusBigInteger bigModulus = new ModulusBigInteger("340282366920938463463374607431768211283");
    // chose this number so it has a root
    BigInteger value = new BigInteger("180740608519057052622341767564917758093");
    BigInteger actual = MathUtils.modularSqrt(value, bigModulus);
    assertEquals(actual.pow(2).mod(bigModulus.getBigInteger()), value);
  }

  @Test
  public void testSum() {
    List<BigInteger> summands = Arrays.asList(
        BigInteger.ONE,
        BigInteger.ZERO,
        modulus.getBigInteger().subtract(BigInteger.TEN).mod(modulus.getBigInteger()),
        new BigInteger("42").mod(modulus.getBigInteger())
    );
    BigInteger expected = BigInteger.ONE.add(BigInteger.ZERO)
        .add(modulus.getBigInteger().subtract(BigInteger.TEN).mod(modulus.getBigInteger())).add(new BigInteger("42").mod(modulus.getBigInteger()))
        .mod(modulus.getBigInteger());
    assertEquals(expected, MathUtils.sum(summands, modulus.getBigInteger()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testModularSqrtNoSqrt() {
    MathUtils.modularSqrt(new BigInteger("23"), modulus);
  }

}
