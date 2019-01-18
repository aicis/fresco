package dk.alexandra.fresco.suite.spdz2k.datatypes;

import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class TestCompUInt128FieldElementMethods {

  private final BigInteger twoTo128 = BigInteger.ONE.shiftLeft(128);
  private FieldElement element = new CompUInt128(9);
  private FieldElement otherElement = new CompUInt128(42);

  @Test
  public void negate() {
    FieldElement result = element.negate();
    BigInteger actual = CompUInt128.extractValue(result);
    BigInteger expected = twoTo128.subtract(BigInteger.valueOf(9));
    assertThat(actual, Is.is(expected));
  }

  @Test
  public void arithmetic() {
    FieldElement sum = element.add(otherElement);
    FieldElement prod = element.multiply(otherElement);
    FieldElement diff = element.subtract(otherElement);
    assertThat(CompUInt128.extractValue(sum),
        Is.is(BigInteger.valueOf(9).add(BigInteger.valueOf(42))));
    assertThat(CompUInt128.extractValue(prod),
        Is.is(BigInteger.valueOf(9).multiply(BigInteger.valueOf(42))));
    final BigInteger expectedDiff = BigInteger.valueOf(9)
        .subtract(BigInteger.valueOf(42))
        .mod(twoTo128);
    assertThat(CompUInt128.extractValue(diff),
        Is.is(expectedDiff));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void sqrt() {
    element.sqrt();
  }

  @Test(expected = IllegalStateException.class)
  public void modInverse() {
    element.modInverse();
  }

  @Test
  public void toStringTest() {
    assertThat(element.toString(), StringContains.containsString("9"));
  }

}
