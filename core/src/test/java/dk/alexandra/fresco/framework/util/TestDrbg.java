package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import dk.alexandra.fresco.framework.MPCException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.crypto.SecretKey;
import org.junit.Test;

public class TestDrbg {

  @Test
  public void testNextBytes() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[] {0x10, 0x09, 0x01};
    HmacDrbg rand1 =
        new HmacDrbg(bytes);
    HmacDrbg rand2 =
        new HmacDrbg(bytes);
    HmacDrbg rand3 = new HmacDrbg();
    byte[] randBytes1 = new byte[10];
    byte[] randBytes2 = new byte[10];
    byte[] randBytes3 = new byte[10];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    rand3.nextBytes(randBytes3);
    assertArrayEquals(randBytes1, randBytes2);
    assertFalse(Arrays.equals(randBytes1, randBytes3));
  }

  @Test(expected = NoSuchAlgorithmException.class)
  public void testNonExistingAlgorithm() throws NoSuchAlgorithmException {
    new HmacDrbg("Bla", new byte[] {0x01});
  }

  @Test(expected = MPCException.class)
  public void testInvalidKey() throws NoSuchAlgorithmException {
    HmacDrbg m = new HmacDrbgFakeKeySpec();
    byte[] randBytes1 = new byte[10];
    m.nextBytes(randBytes1);
  }
  
  @Test(expected = MPCException.class)
  public void testReseedException() throws NoSuchAlgorithmException {
    HmacDrbg m = new HmacDrbgLowReseedCount();
    double max = 12;
    for (double i = 0; i < max; i++) {
      byte[] randBytes1 = new byte[0];
      m.nextBytes(randBytes1);
    }
  }
  
  @Test(expected = MPCException.class)
  public void testReseedException2() throws NoSuchAlgorithmException {
    HmacDrbg m =
        new HmacDrbgLowReseedCount(new byte[] {0x01}, new byte[] {0x02});
    double max = 10;
    byte[] randBytes1 = new byte[0];
    for (double i = 0; i < max * 2; i++) {
      m.nextBytes(randBytes1);
    }
    m.nextBytes(randBytes1);
  }
  
  @Test
  public void testLargeAmount() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[] {0x10, 0x09, 0x01};
    HmacDrbg rand1 =
        new HmacDrbg(bytes);
    HmacDrbg rand2 =
        new HmacDrbg(bytes);
    byte[] randBytes1 = new byte[1000000];
    byte[] randBytes2 = new byte[1000000];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    assertArrayEquals(randBytes1, randBytes2);
  }

  private class HmacDrbgLowReseedCount extends HmacDrbg {

    public HmacDrbgLowReseedCount(byte[]...bs) throws NoSuchAlgorithmException {      
      super(bs);
      MAX_RESEED_COUNT = 10;
    }
  }
  
  private class HmacDrbgFakeKeySpec extends HmacDrbg {

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
