package dk.alexandra.fresco.framework.builder.numeric.field;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class BigIntegerFieldElementTest {

  private BigIntegerModulus modulus = new BigIntegerModulus(BigInteger.valueOf(113));
  private BigIntegerModulus bigModulus = new BigIntegerModulus(
      new BigInteger("340282366920938463463374607431768211283"));
  private FieldElement element1 = BigIntegerFieldElement.create(9, modulus);
  private FieldElement element2 = BigIntegerFieldElement.create(25, modulus);
  private FieldElement element3 = BigIntegerFieldElement.create(49, modulus);

  @Test
  public void creators() {
    FieldElement element1 = BigIntegerFieldElement.create(27, modulus);
    FieldElement element2 = BigIntegerFieldElement.create("27", modulus);
    FieldElement element3 = BigIntegerFieldElement.create(BigInteger.valueOf(27), modulus);
    assertThat(BigIntegerFieldElement.extractValue(element1), Is.is(BigInteger.valueOf(27)));
    assertThat(element1, Is.is(element2));
    assertThat(element2, Is.is(element3));
  }

  @Test
  public void negate() {
    BigInteger result1 = BigIntegerFieldElement.extractValue(element1.negate());
    BigInteger result2 = BigIntegerFieldElement.extractValue(element2.negate());
    BigInteger result3 = BigIntegerFieldElement.extractValue(element3.negate());
    BigInteger value1 = BigIntegerFieldElement.extractValue(element1);
    BigInteger value2 = BigIntegerFieldElement.extractValue(element2);
    BigInteger value3 = BigIntegerFieldElement.extractValue(element3);
    assertThat(result1, Is.is(modulus.getBigInteger().subtract(value1)));
    assertThat(result2, Is.is(modulus.getBigInteger().subtract(value2)));
    assertThat(result3, Is.is(modulus.getBigInteger().subtract(value3)));
  }

  @Test
  public void sqrt() {
    FieldElement element = BigIntegerFieldElement.create(2, modulus);
    FieldElement sqrt = element.sqrt();
    BigInteger value = BigIntegerFieldElement.extractValue(sqrt);
    assertThat(value, Is.is(BigInteger.valueOf(62)));

    element = BigIntegerFieldElement
        .create("180740608519057052622341767564917758093", bigModulus);
    BigInteger expected = BigIntegerFieldElement.extractValue(element);
    sqrt = element.sqrt();
    value = BigIntegerFieldElement.extractValue(sqrt);
    assertThat(value.pow(2).mod(bigModulus.getBigInteger()), Is.is(expected));
  }

  @Test
  public void modInverse() {
    BigInteger result1 = BigIntegerFieldElement
        .extractValue(BigIntegerFieldElement.create(1, modulus).modInverse());
    BigInteger result2 = BigIntegerFieldElement
        .extractValue(BigIntegerFieldElement.create(27, modulus).modInverse());
    BigInteger result3 = BigIntegerFieldElement
        .extractValue(BigIntegerFieldElement.create(56, modulus).modInverse());
    BigInteger result4 = BigIntegerFieldElement
        .extractValue(BigIntegerFieldElement.create(77, modulus).modInverse());
    BigInteger result5 = BigIntegerFieldElement
        .extractValue(BigIntegerFieldElement.create(112, modulus).modInverse());
    assertThat(result1, Is.is(BigInteger.valueOf(1)));
    assertThat(result2, Is.is(BigInteger.valueOf(67)));
    assertThat(result3, Is.is(BigInteger.valueOf(111)));
    assertThat(result4, Is.is(BigInteger.valueOf(91)));
    assertThat(result5, Is.is(BigInteger.valueOf(112)));
  }

  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself",
      "EqualsBetweenInconvertibleTypes", "ConstantConditions"})
  @Test
  public void equals() {
    FieldElement firstElement =
        BigIntegerFieldElement.create(BigInteger.valueOf(27), modulus);
    FieldElement firstElementAgain =
        BigIntegerFieldElement.create(
            modulus.getBigInteger().add(BigInteger.valueOf(27)), modulus);
    FieldElement differentValue =
        BigIntegerFieldElement.create(BigInteger.valueOf(28), modulus);
    FieldElement differentPrime =
        BigIntegerFieldElement.create(BigInteger.valueOf(27), bigModulus);

    assertTrue(firstElement.equals(firstElementAgain));
    assertFalse(firstElement.equals(differentValue));
    assertFalse(firstElement.equals(differentPrime));

    assertTrue(firstElement.equals(firstElement));
    assertFalse(firstElement.equals(""));
    assertFalse(firstElement.equals(null));
  }

  @Test
  public void hash() {
    assertThat(element1.hashCode(), Is.is(5434));
    assertThat(element2.hashCode(), Is.is(5450));
    assertThat(element3.hashCode(), Is.is(5474));
  }

  @Test
  public void toStringTest() {
    FieldElement element = BigIntegerFieldElement.create(BigInteger.valueOf(7854), bigModulus);
    assertThat(element.toString(), StringContains.containsString("7854"));
  }
}