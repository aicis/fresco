package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;

public class TestNumericResourcePool {

  private final BigInteger defaultModulus = new BigInteger("251");

  @Test
  public void testConvertRepresentationLessThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = convertRepresentation(pool, BigInteger.TEN);
    assertEquals(BigInteger.TEN, actual);
  }

  private BigInteger convertRepresentation(NumericResourcePool pool, BigInteger bigInteger) {
    BigIntegerI value = BigInt.fromConstant(bigInteger, defaultModulus);
    return pool.convertRepresentation(value);
  }

  @Test
  public void testConvertRepresentationGreaterThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = convertRepresentation(pool, new BigInteger("200"));
    assertEquals(new BigInteger("200").subtract(defaultModulus), actual);
  }

  @Test
  public void testConvertRepresentationEqualsHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = convertRepresentation(pool, defaultModulus.divide(BigInteger.valueOf(2)));
    assertEquals(defaultModulus.divide(BigInteger.valueOf(2)), actual);
  }

  private class MockNumericResourcePool implements NumericResourcePool {

    private final BigInteger modulus;

    MockNumericResourcePool(BigInteger modulus) {
      this.modulus = modulus;
    }

    @Override
    public BigInteger getModulus() {
      return this.modulus;
    }

    @Override
    public int getMyId() {
      return 0;
    }

    @Override
    public int getNoOfParties() {
      return 0;
    }
  }
}
