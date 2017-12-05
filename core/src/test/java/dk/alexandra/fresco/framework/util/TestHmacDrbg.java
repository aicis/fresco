package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import org.junit.Assert;
import org.junit.Test;

public class TestHmacDrbg {

  @Test
  public void testNextBytes() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[]{0x10, 0x09, 0x01};
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
  
  @Test
  public void testDifferentAlgorithm() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[]{0x10, 0x09, 0x01};
    HmacDrbg rand1 = new HmacDrbg(bytes);    
    HmacDrbg rand2 = new HmacDrbg(new Supplier<Mac>() {
      
      @Override
      public Mac get() {
        try {
          return Mac.getInstance("HmacSHA512");
        } catch (NoSuchAlgorithmException e) {
          return null;
        }
      }
    }, bytes);
    byte[] randBytes1 = new byte[10];
    byte[] randBytes2 = new byte[10];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    assertFalse(Arrays.equals(randBytes1, randBytes2));
  }

  @Test
  public void testNonExistingAlgorithm() throws NoSuchAlgorithmException {
    String defaultAlg = HmacDrbg.DEFAULT_ALGORITHM;  
    HmacDrbg.DEFAULT_ALGORITHM = "BLA";
    try {
      new HmacDrbg();
      Assert.fail();
    } catch (NoSuchAlgorithmException ex) {
      HmacDrbg.DEFAULT_ALGORITHM = defaultAlg;
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testInvalidKey() throws NoSuchAlgorithmException {
    HmacDrbg m = new HmacDrbg();
    m.safeInitialize(new SecretKey() {
      private static final long serialVersionUID = 1L;

      @Override
      public String getFormat() {
        return "Fake";
      }

      @Override
      public byte[] getEncoded() {
        return null;
      }

      @Override
      public String getAlgorithm() {
        return "Fake";
      }
    });
    byte[] randBytes1 = new byte[10];
    m.nextBytes(randBytes1);
  }

  @Test(expected = IllegalStateException.class)
  public void testReseedException() throws NoSuchAlgorithmException {
    HmacDrbg m = new HmacDrbg(new byte[]{0x01});
    m.reseedCounter = HmacDrbg.MAX_RESEED_COUNT - 1;
    double max = 2;
    for (double i = 0; i < max; i++) {
      byte[] randBytes1 = new byte[0];
      m.nextBytes(randBytes1);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testReseedException2() throws NoSuchAlgorithmException {
    HmacDrbg m = new HmacDrbg(new byte[]{0x01}, new byte[]{0x02});
    m.reseedCounter = HmacDrbg.MAX_RESEED_COUNT - 1;
    byte[] randBytes1 = new byte[0];
    for (double i = 0; i < 2; i++) {
      m.nextBytes(randBytes1);
    }
    //New seed is now used. Reset again
    m.reseedCounter = HmacDrbg.MAX_RESEED_COUNT - 1;
    for (double i = 0; i < 2; i++) {
      m.nextBytes(randBytes1);
    }
  }

  @Test
  public void testLargeAmount() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[]{0x10, 0x09, 0x01};
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
}
