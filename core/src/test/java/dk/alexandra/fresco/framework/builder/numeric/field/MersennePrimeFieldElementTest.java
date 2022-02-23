package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

public class MersennePrimeFieldElementTest {

  private MersennePrimeModulus modulus;
  private MersennePrimeModulus bigModulus;
  private FieldElement element1;
  private FieldElement element2;
  private FieldElement element3;

  @Before
  public void setUp() {
    //prime=113
    modulus = new MersennePrimeModulus(7, 15);
    //prime=340282366920938463463374607431768211283
    bigModulus = new MersennePrimeModulus(128, 173);
    element1 = MersennePrimeFieldElement.create(9, modulus);
    element2 = MersennePrimeFieldElement.create(25, modulus);
    element3 = MersennePrimeFieldElement.create(49, modulus);
  }

  @Test(expected = NullPointerException.class)
  public void nullModulus() {
    BigIntegerFieldElement.create(BigInteger.ZERO, null);
  }

  @Test(expected = NullPointerException.class)
  public void nullValue() {
    BigIntegerFieldElement.create(BigInteger.ZERO, null);
  }

  @Test
  public void creators() {
    testCreation(27, 27, modulus);
    testCreation(27 + 113, 27, modulus);
    testCreation(27 - 113, 27, modulus);
    testCreation(-1, 113 - 1, modulus);
    testCreation(0, 0, modulus);
  }

  private void testCreation(int value, int expected, MersennePrimeModulus modulus) {
    FieldElement element1 = MersennePrimeFieldElement.create(value, modulus);
    FieldElement element2 = MersennePrimeFieldElement.create("" + value, modulus);
    FieldElement element3 = MersennePrimeFieldElement.create(BigInteger.valueOf(value), modulus);
    assertThat(element1.toBigInteger(),
        Is.is(BigInteger.valueOf(expected)));
    assertThat(element2.toBigInteger(),
        Is.is(BigInteger.valueOf(expected)));
    assertThat(element3.toBigInteger(),
        Is.is(BigInteger.valueOf(expected)));
  }

  @Test
  public void negate() {
    BigInteger result1 = element1.negate().toBigInteger();
    BigInteger result2 = element2.negate().toBigInteger();
    BigInteger result3 = element3.negate().toBigInteger();
    BigInteger value1 = element1.toBigInteger();
    BigInteger value2 = element2.toBigInteger();
    BigInteger value3 = element3.toBigInteger();
    assertThat(result1, Is.is(modulus.getPrime().subtract(value1)));
    assertThat(result2, Is.is(modulus.getPrime().subtract(value2)));
    assertThat(result3, Is.is(modulus.getPrime().subtract(value3)));
  }

  @Test
  public void sqrt() {
    FieldElement element = MersennePrimeFieldElement.create(2, modulus);
    FieldElement sqrt = element.sqrt();
    BigInteger value = sqrt.toBigInteger();
    assertThat(value, Is.is(BigInteger.valueOf(62)));

    element = MersennePrimeFieldElement
        .create("180740608519057052622341767564917758093", bigModulus);
    BigInteger expected = element.toBigInteger();
    sqrt = element.sqrt();
    value = sqrt.toBigInteger();
    assertThat(value.pow(2).mod(bigModulus.getPrime()), Is.is(expected));
  }

  @Test
  public void modInverse() {
    List<Integer> values = Arrays.asList(-123, -12, -1, 1, 12, 123, 1234, 12345, 123456, 1234567);
    List<MersennePrimeFieldDefinition> definitions = Stream.of(8, 16, 32, 64, 128, 256, 384, 512).map(MersennePrimeFieldDefinition::find).collect(
        Collectors.toList());

    for (MersennePrimeFieldDefinition definition : definitions) {
      for (Integer value : values) {
        testModInverse(BigInteger.valueOf(value), definition, BigInteger.valueOf(value).modInverse(definition.getModulus()));
      }
    }
  }

  private void testModInverse(BigInteger value, MersennePrimeFieldDefinition definition, BigInteger expected) {
    BigInteger result = definition.createElement(value).modInverse().toBigInteger();
    assertThat(result, Is.is(expected));
  }

  @Test
  public void toStringTest() {
    FieldElement element = MersennePrimeFieldElement.create(BigInteger.valueOf(7854), bigModulus);
    assertThat(element.toString(), StringContains.containsString("7854"));
  }

  @Test
  public void testIsZero() {
    FieldElement element = MersennePrimeFieldElement.create(BigInteger.valueOf(0), bigModulus);
    assertTrue(element.isZero());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownBitLength() {
    MersennePrimeFieldDefinition.find(7);
  }
}
