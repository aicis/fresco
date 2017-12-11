package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class TestDrngImpl {

  private DrngImpl drng;

  /**
   * Set up the DRNG to test.
   */
  @Before
  public void setup() {
    Random rand = new Random(42);
    byte[] seed = new byte[32];
    rand.nextBytes(seed);
    Drbg drbg = null;
    drbg = new AesCtrDrbg(seed);
    this.drng = new DrngImpl(drbg);
  }

  @Test
  public void testNextInt() {
    int limit = 1000;
    for (int i = 0; i < 10; i++) {
      int r = drng.nextInt(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test
  public void testNextIntByteSize() {
    int limit = 256;
    for (int i = 0; i < 10; i++) {
      int r = drng.nextInt(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test
  public void testNextIntMaxSize() {
    int limit = Integer.MAX_VALUE;
    for (int i = 0; i < 10; i++) {
      int r = drng.nextInt(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNextIntNegativeSize() {
    int limit = -1;
    for (int i = 0; i < 10; i++) {
      int r = drng.nextInt(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNextIntZeroSize() {
    int limit = 0;
    for (int i = 0; i < 10; i++) {
      int r = drng.nextInt(limit);
      assertInRange(r, 0, limit);
    }
  }


  @Test
  public void testNextLong() {
    long limit = Integer.MAX_VALUE + 1000L;
    for (int i = 0; i < 10; i++) {
      long r = drng.nextLong(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test
  public void testNextLongByteSize() {
    int limit = 256;
    for (int i = 0; i < 10; i++) {
      long r = drng.nextLong(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test
  public void testNextLongMaxSize() {
    long limit = Long.MAX_VALUE;
    for (int i = 0; i < 10; i++) {
      long r = drng.nextLong(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNextLongNegativeSize() {
    int limit = -1;
    for (int i = 0; i < 10; i++) {
      long r = drng.nextLong(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNextLongZeroSize() {
    int limit = 0;
    for (int i = 0; i < 10; i++) {
      long r = drng.nextLong(limit);
      assertInRange(r, 0, limit);
    }
  }

  @Test
  public void testNextBigInteger() {
    BigInteger limit = new
        BigInteger("1234390583094589083690724905729037458907233245900987120359876321450987345");
    for (int i = 0; i < 10; i++) {
      BigInteger b = drng.nextBigInteger(limit);
      assertInRange(b, BigInteger.ZERO, limit);
    }
  }

  @Test
  public void testNextBigIntegerByteSize() {
    BigInteger limit = new
        BigInteger("256");
    for (int i = 0; i < 10; i++) {
      BigInteger b = drng.nextBigInteger(limit);
      assertInRange(b, BigInteger.ZERO, limit);
    }
  }

  @Test
  public void testNextBigIntegerLongSize() {
    BigInteger limit = new
        BigInteger("123439058309458908362350987123509834703458923475903745093840934590872340"
            + "98237469839305077937827359696778362338580384509238907249057290374589072332459"
            + "00987120359876321450987345");
    for (int i = 0; i < 10; i++) {
      BigInteger b = drng.nextBigInteger(limit);
      assertInRange(b, BigInteger.ZERO, limit);
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNextBigIntegerZeroSize() {
    BigInteger limit = BigInteger.ZERO;
    for (int i = 0; i < 10; i++) {
      BigInteger b = drng.nextBigInteger(limit);
      assertInRange(b, BigInteger.ZERO, limit);
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNextBigIntegerNegativeSize() {
    BigInteger limit = BigInteger.ONE.negate();
    for (int i = 0; i < 10; i++) {
      BigInteger b = drng.nextBigInteger(limit);
      assertInRange(b, BigInteger.ZERO, limit);
    }
  }

  /**
   * Asserting that a number is in a given range.
   * @param actual the actual value
   * @param lower lower bound on the range (inclusive)
   * @param upper upper bound on the range (exclusive)
   */
  private void assertInRange(long actual, long lower, long upper) {
    assertTrue("Expected be in range [" +  lower + ",..., (" + upper + " -1)], but was " + actual,
        lower <= actual && actual < upper);
  }

  /**
   * Asserting that a number is in a given range.
   * @param actual the actual value
   * @param lower lower bound on the range (inclusive)
   * @param upper upper bound on the range (exclusive)
   */
  private void assertInRange(BigInteger actual, BigInteger lower, BigInteger upper) {
    assertTrue("Expected be in range [" +  lower + ",..., (" + upper + " -1)], but was " + actual,
        lower.compareTo(actual) <= 0 && actual.compareTo(upper) < 0);
  }

}
