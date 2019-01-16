package dk.alexandra.fresco.framework.builder.numeric.field;

import java.math.BigInteger;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class BigIntegerModulusTest {

  @Test
  public void getBigInteger() {
    BigInteger modulus = new BigInteger("13");
    Assert.assertThat(new BigIntegerModulus(modulus).getBigInteger(), Is.is(modulus));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getNegativeModulus() {
    BigInteger modulus = new BigInteger("-13");
    new BigIntegerModulus(modulus);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getZeroModulus() {
    BigInteger modulus = BigInteger.ZERO;
    new BigIntegerModulus(modulus);
  }

  @Test
  public void testOfToString() {
    BigInteger modulus = new BigInteger("13");
    Assert.assertThat(
        new BigIntegerModulus(modulus).toString(),
        Matchers.containsString(modulus.toString()));
    Assert.assertThat(
        new BigIntegerModulus(modulus).toString(),
        Matchers.containsString("BigIntegerModulus"));
  }
}