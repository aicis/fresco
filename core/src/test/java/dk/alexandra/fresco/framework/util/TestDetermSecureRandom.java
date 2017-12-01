package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import dk.alexandra.fresco.framework.MPCException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.crypto.SecretKey;
import org.junit.Test;

public class TestDetermSecureRandom {

  @Test
  public void testNextBytes() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[] {0x10, 0x09, 0x01};
    HmacDeterministicRandomBitGeneratorImpl rand1 =
        new HmacDeterministicRandomBitGeneratorImpl(bytes);
    HmacDeterministicRandomBitGeneratorImpl rand2 =
        new HmacDeterministicRandomBitGeneratorImpl(bytes);
    HmacDeterministicRandomBitGeneratorImpl rand3 = new HmacDeterministicRandomBitGeneratorImpl();
    byte[] randBytes1 = new byte[10];
    byte[] randBytes2 = new byte[10];
    byte[] randBytes3 = new byte[10];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    rand3.nextBytes(randBytes3);
    assertArrayEquals(randBytes1, randBytes2);
    assertFalse(Arrays.equals(randBytes1, randBytes3));
  }

  @Test
  public void testSetSeed() throws NoSuchAlgorithmException {
    HmacDeterministicRandomBitGeneratorImpl rand1 = new HmacDeterministicRandomBitGeneratorImpl();
    byte[] bsBeforeSeed = new byte[10];
    byte[] bsAfterSeed = new byte[10];
    rand1.nextBytes(bsBeforeSeed);
    rand1.setSeed(new byte[] {0x04, 0x06});
    rand1.nextBytes(bsAfterSeed);
    assertFalse(Arrays.equals(bsBeforeSeed, bsAfterSeed));
  }

  @Test(expected = NoSuchAlgorithmException.class)
  public void testNonExistingAlgorithm() throws NoSuchAlgorithmException {
    new HmacDeterministicRandomBitGeneratorImpl(new byte[] {0x01}, "Bla");
  }

  @Test(expected = MPCException.class)
  public void testInvalidKey() throws NoSuchAlgorithmException {
    HmacDeterministicRandomBitGeneratorImpl m = new HmacDrbgFakeKeySpec();
    byte[] randBytes1 = new byte[10];
    m.nextBytes(randBytes1);
  }
  
  @Test
  public void testLargeAmount() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[] {0x10, 0x09, 0x01};
    HmacDeterministicRandomBitGeneratorImpl rand1 =
        new HmacDeterministicRandomBitGeneratorImpl(bytes);
    HmacDeterministicRandomBitGeneratorImpl rand2 =
        new HmacDeterministicRandomBitGeneratorImpl(bytes);
    byte[] randBytes1 = new byte[1000000];
    byte[] randBytes2 = new byte[1000000];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    assertArrayEquals(randBytes1, randBytes2);
  }

  private class HmacDrbgFakeKeySpec extends HmacDeterministicRandomBitGeneratorImpl {

    public HmacDrbgFakeKeySpec() throws NoSuchAlgorithmException {
      super();
    }

    @Override
    protected SecretKey getSafeKey(Supplier<SecretKey> keySupplier) {
      SecretKey key = new SecretKey() {
        private static final long serialVersionUID = 1L;

        @Override
        public String getFormat() {
          return "Fake";
        }

        @Override
        public byte[] getEncoded() {
          return new byte[] {0x00};
        }

        @Override
        public String getAlgorithm() {
          return "Fake";
        }
      };
      return key;
    }
  }
}
