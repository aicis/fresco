package dk.alexandra.fresco.framework.builder.numeric.field;

import java.math.BigInteger;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class MersennePrimeFieldDefinitionTest {

  @Test(expected = IllegalArgumentException.class)
  public void getNegativeBitLength() {
    new MersennePrimeFieldDefinition(0, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getConstant() {
    new MersennePrimeFieldDefinition(8, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void largeConstant() {
    new MersennePrimeFieldDefinition(1, 2);
  }

  @Test
  public void createElement() {
      Long baseNumber = 2L;
      MersennePrimeFieldDefinition fieldDefintion = new MersennePrimeFieldDefinition(4, 2);
      MersennePrimeFieldElement elementFromString = (MersennePrimeFieldElement) fieldDefintion.createElement(baseNumber.toString());
      MersennePrimeFieldElement elementFromLong = (MersennePrimeFieldElement) fieldDefintion.createElement(baseNumber);
      MersennePrimeFieldElement elementFromBigInt = (MersennePrimeFieldElement) fieldDefintion.createElement(new BigInteger(baseNumber.toString()));

      MersennePrimeFieldElement result = (MersennePrimeFieldElement) elementFromLong.multiply(elementFromString).multiply(elementFromBigInt);
      Assert.assertThat(
              MersennePrimeFieldElement.extractValue(result).longValue(),
              Is.is(baseNumber*baseNumber*baseNumber));

  }

  @Test
  public void getModulus() {
    testValues(3, 3);
    testValues(20, 1);
    testValues(4, 2);
    testValues(3, 3);
    testValues(31, 37);
  }
  @Test
  public void convertToSigned() {
    MersennePrimeFieldDefinition definition_1 = new MersennePrimeFieldDefinition(3,1);
    testConversions(definition_1,3,3);
    testConversions(definition_1,4,-3);
    testConversions(definition_1,6,-1);
    testConversions(definition_1,7,0);
  }

  private void testConversions(MersennePrimeFieldDefinition definition, int input, int expected){
    Assert.assertEquals(BigInteger.valueOf(expected),definition.convertToSigned(BigInteger.valueOf(input)));
  }

  private void testValues(int bitLength, int constant) {
    int expected = (1 << bitLength) - constant;
    MersennePrimeFieldDefinition mersennePrimeFieldDefinition =
        new MersennePrimeFieldDefinition(bitLength, constant);
    BigInteger modulus = mersennePrimeFieldDefinition.getModulus();
    Assert.assertThat(
        modulus,
        Is.is(new BigInteger("" + expected)));
  }
}
