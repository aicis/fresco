package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

public class BigIntegerFieldElementTest {

  private BigIntegerModulus modulus;
  private BigIntegerModulus bigModulus;
  private FieldElement element1;
  private FieldElement element2;
  private FieldElement element3;

  @Before
  public void setUp() {
    modulus = new BigIntegerModulus(BigInteger.valueOf(113));
    bigModulus =
        new BigIntegerModulus(
            new BigInteger("340282366920938463463374607431768211283"));
    element1 = BigIntegerFieldElement.create(9, modulus);
    element2 = BigIntegerFieldElement.create(25, modulus);
    element3 = BigIntegerFieldElement.create(49, modulus);
  }

  @Test(expected = NullPointerException.class)
  public void nullModulus() {
    BigIntegerFieldElement.create(BigInteger.ZERO, null);
  }

  @Test(expected = NullPointerException.class)
  public void nullValue() {
    BigIntegerFieldElement.create((BigInteger) null, modulus);
  }

  @Test
  public void creators() {
    testCreation(27, 27, modulus);
    testCreation(27 + 113, 27, modulus);
    testCreation(27 - 113, 27, modulus);
    testCreation(-1, 113 - 1, modulus);
    testCreation(0, 0, modulus);
  }

  private void testCreation(int value, int expected, BigIntegerModulus modulus) {
    FieldElement element1 = BigIntegerFieldElement.create(value, modulus);
    FieldElement element2 = BigIntegerFieldElement.create("" + value, modulus);
    FieldElement element3 = BigIntegerFieldElement.create(BigInteger.valueOf(value), modulus);
    assertThat(element1.toBigInteger(), Is.is(BigInteger.valueOf(expected)));
    assertThat(element2.toBigInteger(), Is.is(BigInteger.valueOf(expected)));
    assertThat(element3.toBigInteger(), Is.is(BigInteger.valueOf(expected)));
  }

  @Test
  public void negate() {
    BigInteger result1 = element1.negate().toBigInteger();
    BigInteger result2 = element2.negate().toBigInteger();
    BigInteger result3 = element3.negate().toBigInteger();
    BigInteger value1 = element1.toBigInteger();
    BigInteger value2 = element2.toBigInteger();
    BigInteger value3 = element3.toBigInteger();
    assertThat(result1, Is.is(modulus.getBigInteger().subtract(value1)));
    assertThat(result2, Is.is(modulus.getBigInteger().subtract(value2)));
    assertThat(result3, Is.is(modulus.getBigInteger().subtract(value3)));
  }

  @Test
  public void sqrt() {
    FieldElement element = BigIntegerFieldElement.create(2, modulus);
    FieldElement sqrt = element.sqrt();
    BigInteger value = sqrt.toBigInteger();
    assertThat(value, Is.is(BigInteger.valueOf(62)));

    element = BigIntegerFieldElement
        .create("180740608519057052622341767564917758093", bigModulus);
    BigInteger expected = element.toBigInteger();
    sqrt = element.sqrt();
    value = sqrt.toBigInteger();
    assertThat(value.pow(2).mod(bigModulus.getBigInteger()), Is.is(expected));
  }

  @Test
  public void modInverse() {
    BigInteger result1 = BigIntegerFieldElement.create(1, modulus).modInverse().toBigInteger();
    BigInteger result2 = BigIntegerFieldElement.create(27, modulus).modInverse().toBigInteger();
    BigInteger result3 = BigIntegerFieldElement.create(56, modulus).modInverse().toBigInteger();
    BigInteger result4 = BigIntegerFieldElement.create(77, modulus).modInverse().toBigInteger();
    BigInteger result5 = BigIntegerFieldElement.create(112, modulus).modInverse().toBigInteger();
    assertThat(result1, Is.is(BigInteger.valueOf(1)));
    assertThat(result2, Is.is(BigInteger.valueOf(67)));
    assertThat(result3, Is.is(BigInteger.valueOf(111)));
    assertThat(result4, Is.is(BigInteger.valueOf(91)));
    assertThat(result5, Is.is(BigInteger.valueOf(112)));
  }

  @Test
  public void multiply() {
    FieldElement element1 = BigIntegerFieldElement.create(1, modulus);
    FieldElement element2 = BigIntegerFieldElement.create(2, modulus);
    FieldElement element3 = BigIntegerFieldElement.create(4, modulus);
    BigInteger result1 = BigIntegerFieldElement.create(27, modulus).multiply(element1).toBigInteger();
    BigInteger result2 = BigIntegerFieldElement.create(43, modulus).multiply(element2).toBigInteger();
    BigInteger result3 = BigIntegerFieldElement.create(76, modulus).multiply(element3).toBigInteger();
    BigInteger result4 = BigIntegerFieldElement.create(98, modulus).multiply(element2).toBigInteger();
    BigInteger result5 = BigIntegerFieldElement.create(112, modulus).multiply(element3).toBigInteger();
    assertThat(result1, Is.is(BigInteger.valueOf(27)));
    assertThat(result2, Is.is(BigInteger.valueOf(86)));
    assertThat(result3, Is.is(BigInteger.valueOf(78)));
    assertThat(result4, Is.is(BigInteger.valueOf(83)));
    assertThat(result5, Is.is(BigInteger.valueOf(109)));
  }

  @Test
  public void toStringTest() {
    FieldElement element = BigIntegerFieldElement.create(BigInteger.valueOf(7854), bigModulus);
    assertThat(element.toString(), StringContains.containsString("7854"));
  }

  @Test
  public void testIsZero() {
    FieldElement element = BigIntegerFieldElement.create(BigInteger.valueOf(0), bigModulus);
    assertTrue(element.isZero());
  }
}
