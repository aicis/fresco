package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;

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
    assertThat(MersennePrimeFieldElement.extractValue(element1),
        Is.is(BigInteger.valueOf(expected)));
    assertThat(MersennePrimeFieldElement.extractValue(element2),
        Is.is(BigInteger.valueOf(expected)));
    assertThat(MersennePrimeFieldElement.extractValue(element3),
        Is.is(BigInteger.valueOf(expected)));
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
    BigInteger result = MersennePrimeFieldElement
        .extractValue(definition.createElement(value).modInverse());
    assertThat(result, Is.is(expected));
  }

  @Test
  public void toStringTest() {
    FieldElement element = MersennePrimeFieldElement.create(BigInteger.valueOf(7854), bigModulus);
    assertThat(element.toString(), StringContains.containsString("7854"));
  }
}