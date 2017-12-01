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
    byte[] bytes = new byte[] { 0x10, 0x09, 0x01 };
    HMacDRBG rand1 = new HMacDRBG(bytes);
    HMacDRBG rand2 = new HMacDRBG(bytes);
    HMacDRBG rand3 = new HMacDRBG();
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
    HMacDRBG rand1 = new HMacDRBG();    
    byte[] bsBeforeSeed = new byte[10];
    byte[] bsAfterSeed = new byte[10];
    rand1.nextBytes(bsBeforeSeed);
    rand1.setSeed(new byte[]{0x04, 0x06});
    rand1.nextBytes(bsAfterSeed);
    assertFalse(Arrays.equals(bsBeforeSeed, bsAfterSeed));
  }
  
  @Test(expected = NoSuchAlgorithmException.class)
  public void testNonExistingAlgorithm() throws NoSuchAlgorithmException {
    new HMacDRBG(new byte[]{0x01}, "Bla");        
  }
  
  @Test(expected = MPCException.class)
  public void testInvalidKey() throws NoSuchAlgorithmException {
    HMacDRBG m = new HMacDRBGFakeKeySpec();
    byte[] randBytes1 = new byte[10];
    m.nextBytes(randBytes1);
  }
  
  @Test
  public void testLargeAmount() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[] { 0x10, 0x09, 0x01 };
    HMacDRBG rand1 = new HMacDRBG(bytes);
    HMacDRBG rand2 = new HMacDRBG(bytes);  
    byte[] randBytes1 = new byte[1000000];
    byte[] randBytes2 = new byte[1000000];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    assertArrayEquals(randBytes1, randBytes2);
  }
  
  private class HMacDRBGFakeKeySpec extends HMacDRBG {

    public HMacDRBGFakeKeySpec() throws NoSuchAlgorithmException {
      super();
    }
   
    @Override
    protected SecretKey createKey(Supplier<SecretKey> keySupplier){
      SecretKey key = new SecretKey() {
        
        @Override
        public String getFormat() {
          return "Fake";
        }
        
        @Override
        public byte[] getEncoded() {
          return new byte[]{0x00};
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
