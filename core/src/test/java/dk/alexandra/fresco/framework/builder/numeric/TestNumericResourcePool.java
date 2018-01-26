package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import java.math.BigInteger;
import org.junit.Test;

public class TestNumericResourcePool {

  private final BigInteger defaultModulus = new BigInteger("251");

  @Test
  public void testConvertRepresentationLessThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = pool.convertRepresentation(BigInteger.TEN);
    assertEquals(BigInteger.TEN, actual);
  }

  @Test
  public void testConvertRepresentationGreaterThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = pool.convertRepresentation(new BigInteger("200"));
    assertEquals(new BigInteger("200").subtract(defaultModulus), actual);
  }

  @Test
  public void testConvertRepresentationEqualsHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = pool.convertRepresentation(defaultModulus.divide(BigInteger.valueOf(2)));
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
    public ByteSerializer<BigInteger> getSerializer() {
      return null;
    }

    @Override
    public int getMyId() {
      return 0;
    }

    @Override
    public int getNoOfParties() {
      return 0;
    }

    @Override
    public Drbg getRandomGenerator() {
      return null;
    }
  }
}
