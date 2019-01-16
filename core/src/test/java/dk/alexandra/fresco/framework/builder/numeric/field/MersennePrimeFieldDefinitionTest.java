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
  public void getModulus() {
    testValues(3, 3);
    testValues(20, 1);
    testValues(4, 2);
    testValues(3, 3);
    testValues(31, 37);
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