package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;

public class TestMathUtils {

  BigInteger modulus = new BigInteger("113");

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
    BigInteger bigModulus = new BigInteger("340282366920938463463374607431768211283");
    // chose this number so it has a root
    BigInteger value = new BigInteger("180740608519057052622341767564917758093");
    BigInteger actual = MathUtils.modularSqrt(value, bigModulus);
    assertEquals(actual.pow(2).mod(bigModulus), value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testModularSqrtNoSqrt() {
    MathUtils.modularSqrt(new BigInteger("23"), modulus);
  }

}
