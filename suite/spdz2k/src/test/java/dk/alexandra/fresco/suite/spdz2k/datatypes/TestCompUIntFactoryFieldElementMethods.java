package dk.alexandra.fresco.suite.spdz2k.datatypes;

import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestCompUIntFactoryFieldElementMethods {

  private final FieldDefinition fieldDefinition = new CompUInt128Factory();

  @Test
  public void createElementBigInteger() {
    FieldElement actual = fieldDefinition.createElement(BigInteger.valueOf(2));
    BigInteger expected = BigInteger.valueOf(2);
    assertThat(CompUInt128.extractValue(actual), Is.is(expected));
  }

  @Test
  public void createElementInt() {
    FieldElement actual = fieldDefinition.createElement(Integer.MAX_VALUE);
    BigInteger expected = BigInteger.valueOf(Integer.MAX_VALUE);
    assertThat(CompUInt128.extractValue(actual), Is.is(expected));
  }


  @Test
  public void createElementString() {
    FieldElement actual = fieldDefinition.createElement("2");
    BigInteger expected = BigInteger.valueOf(2);
    assertThat(CompUInt128.extractValue(actual), Is.is(expected));
  }

  @Test
  public void getModulus() {
    assertThat(fieldDefinition.getModulus(), Is.is(BigInteger.ONE.shiftLeft(64)));
  }

  @Test
  public void conversionUnsigned() {
    final BigInteger expectedUnsigned = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
    BigInteger actualUnsigned = fieldDefinition.convertToUnsigned(fieldDefinition
        .createElement(expectedUnsigned));
    assertThat(actualUnsigned, Is.is(expectedUnsigned));
  }

  @Test
  public void conversionUnsignedFullModulus() {
    // even though we are working mod 2^{k + s}, our effective modulus is 2^k
    final BigInteger expectedUnsigned = BigInteger.ONE.shiftLeft(64).add(BigInteger.ONE);
    BigInteger actualUnsigned = fieldDefinition.convertToUnsigned(fieldDefinition
        .createElement(expectedUnsigned));
    assertThat(actualUnsigned, Is.is(BigInteger.ONE));
  }

  @Test
  public void conversionSigned() {
    final BigInteger expectedUnsigned = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
    BigInteger actualUnsigned = fieldDefinition.convertToSigned(expectedUnsigned);
    assertThat(actualUnsigned, Is.is(BigInteger.valueOf(-1)));
  }

  @Test
  public void convertToBitVector() {
    byte[] bytes = {63, -1, -1, -1, -1, -1, -1, -44};
    BigInteger value = new BigInteger(1, bytes);
    FieldElement fieldElement = fieldDefinition.createElement(value);
    StrictBitVector vector = fieldDefinition.convertToBitVector(fieldElement);
    StrictBitVector expected = new StrictBitVector(bytes);
    assertThat(vector, Is.is(expected));
  }

}
