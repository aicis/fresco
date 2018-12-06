package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;

public class TestNumericResourcePool {

  private final Modulus defaultModulus = new Modulus(new BigInteger("251"));

  @Test
  public void testConvertRepresentationLessThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = convertRepresentation(pool, BigInteger.TEN);
    assertEquals(BigInteger.TEN, actual);
  }

  private BigInteger convertRepresentation(NumericResourcePool pool, BigInteger bigInteger) {
    FieldElement value = new BigInt(bigInteger.toString(), defaultModulus);
    return pool.convertRepresentation(value);
  }

  @Test
  public void testConvertRepresentationGreaterThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = convertRepresentation(pool, new BigInteger("200"));
    assertEquals(new BigInteger("200").subtract(defaultModulus.getBigInteger()), actual);
  }

  @Test
  public void testConvertRepresentationEqualsHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(defaultModulus);
    BigInteger actual = convertRepresentation(pool, defaultModulus.getBigInteger().divide(BigInteger.valueOf(2)));
    assertEquals(defaultModulus.getBigInteger().divide(BigInteger.valueOf(2)), actual);
  }

  private class MockNumericResourcePool implements NumericResourcePool {

    private final Modulus modulus;

    MockNumericResourcePool(Modulus modulus) {
      this.modulus = modulus;
    }

    @Override
    public Modulus getModulus() {
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
