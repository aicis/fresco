package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.junit.Test;

public class TestDetermSecureRandom {

  @Test
  public void testNextBytes() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[] { 0x10, 0x09, 0x01 };
    DetermSecureRandom rand1 = new DetermSecureRandom(bytes);
    DetermSecureRandom rand2 = new DetermSecureRandom(bytes);
    DetermSecureRandom rand3 = new DetermSecureRandom();
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
    DetermSecureRandom rand1 = new DetermSecureRandom();    
    byte[] bsBeforeSeed = new byte[10];
    byte[] bsAfterSeed = new byte[10];
    rand1.nextBytes(bsBeforeSeed);
    rand1.setSeed(new byte[]{0x04, 0x06});
    rand1.nextBytes(bsAfterSeed);
    assertFalse(Arrays.equals(bsBeforeSeed, bsAfterSeed));
  }
  
  @Test(expected = NoSuchAlgorithmException.class)
  public void testNonExistingAlgorithm() throws NoSuchAlgorithmException {
    new DetermSecureRandom(new byte[]{0x01}, "Bla");        
  }
  
  @Test
  public void testLargeAmount() throws NoSuchAlgorithmException {
    byte[] bytes = new byte[] { 0x10, 0x09, 0x01 };
    DetermSecureRandom rand1 = new DetermSecureRandom(bytes);
    DetermSecureRandom rand2 = new DetermSecureRandom(bytes);  
    byte[] randBytes1 = new byte[1000000];
    byte[] randBytes2 = new byte[1000000];
    rand1.nextBytes(randBytes1);
    rand2.nextBytes(randBytes2);
    assertArrayEquals(randBytes1, randBytes2);
  }
}
