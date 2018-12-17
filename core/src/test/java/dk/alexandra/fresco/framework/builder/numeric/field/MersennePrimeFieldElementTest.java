package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class MersennePrimeFieldElementTest {

  //prime=113
  private MersennePrimeModulus modulus = new MersennePrimeModulus(7, 15);
  //prime=340282366920938463463374607431768211283
  private MersennePrimeModulus bigModulus = new MersennePrimeModulus(128, 173);
  private FieldElement element1 = MersennePrimeFieldElement.create(9, modulus);
  private FieldElement element2 = MersennePrimeFieldElement.create(25, modulus);
  private FieldElement element3 = MersennePrimeFieldElement.create(49, modulus);

  @Test
  public void creators() {
    FieldElement element1 = MersennePrimeFieldElement.create(27, modulus);
    FieldElement element2 = MersennePrimeFieldElement.create("27", modulus);
    FieldElement element3 = MersennePrimeFieldElement.create(BigInteger.valueOf(27), modulus);
    assertThat(MersennePrimeFieldElement.extractValue(element1), Is.is(BigInteger.valueOf(27)));
    assertThat(MersennePrimeFieldElement.extractValue(element2), Is.is(BigInteger.valueOf(27)));
    assertThat(MersennePrimeFieldElement.extractValue(element3), Is.is(BigInteger.valueOf(27)));
  }

  @Test
  public void negate() {
    BigInteger result1 = MersennePrimeFieldElement.extractValue(element1.negate());
    BigInteger result2 = MersennePrimeFieldElement.extractValue(element2.negate());
    BigInteger result3 = MersennePrimeFieldElement.extractValue(element3.negate());
    BigInteger value1 = MersennePrimeFieldElement.extractValue(element1);
    BigInteger value2 = MersennePrimeFieldElement.extractValue(element2);
    BigInteger value3 = MersennePrimeFieldElement.extractValue(element3);
    assertThat(result1, Is.is(modulus.getPrime().subtract(value1)));
    assertThat(result2, Is.is(modulus.getPrime().subtract(value2)));
    assertThat(result3, Is.is(modulus.getPrime().subtract(value3)));
  }

  @Test
  public void sqrt() {
    FieldElement element = MersennePrimeFieldElement.create(2, modulus);
    FieldElement sqrt = element.sqrt();
    BigInteger value = MersennePrimeFieldElement.extractValue(sqrt);
    assertThat(value, Is.is(BigInteger.valueOf(62)));

    element = MersennePrimeFieldElement
        .create("180740608519057052622341767564917758093", bigModulus);
    BigInteger expected = MersennePrimeFieldElement.extractValue(element);
    sqrt = element.sqrt();
    value = MersennePrimeFieldElement.extractValue(sqrt);
    assertThat(value.pow(2).mod(bigModulus.getPrime()), Is.is(expected));
  }

  @Test
  public void modInverse() {
    BigInteger result1 = MersennePrimeFieldElement
        .extractValue(MersennePrimeFieldElement.create(1, modulus).modInverse());
    BigInteger result2 = MersennePrimeFieldElement
        .extractValue(MersennePrimeFieldElement.create(27, modulus).modInverse());
    BigInteger result3 = MersennePrimeFieldElement
        .extractValue(MersennePrimeFieldElement.create(56, modulus).modInverse());
    BigInteger result4 = MersennePrimeFieldElement
        .extractValue(MersennePrimeFieldElement.create(77, modulus).modInverse());
    BigInteger result5 = MersennePrimeFieldElement
        .extractValue(MersennePrimeFieldElement.create(112, modulus).modInverse());
    assertThat(result1, Is.is(BigInteger.valueOf(1)));
    assertThat(result2, Is.is(BigInteger.valueOf(67)));
    assertThat(result3, Is.is(BigInteger.valueOf(111)));
    assertThat(result4, Is.is(BigInteger.valueOf(91)));
    assertThat(result5, Is.is(BigInteger.valueOf(112)));
  }

  @Test
  public void toStringTest() {
    FieldElement element = MersennePrimeFieldElement.create(BigInteger.valueOf(7854), bigModulus);
    assertThat(element.toString(), StringContains.containsString("7854"));
  }
}