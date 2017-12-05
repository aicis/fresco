package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.Test;

public class TestAesCtrDrbg {

  @Test
  public void testAesCtrDrbg() {
    try {
      Random rand = new Random(42);
      byte[] seed = new byte[16];
      rand.nextBytes(seed);
      new AesCtrDrbg(seed);
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      fail("Constructor threw unexcepted exception");
    }

    try {
      new AesCtrDrbg(new byte[18]);
      fail("Constructor fail to throw exception on too long seed");
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      fail("Constructor threw unexcepted exception");
    } catch (RuntimeException f) {
      // Do nothing
    }

    try {
      new AesCtrDrbg(new byte[1]);
      fail("Constructor fail to throw exception on too short seed");
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      fail("Constructor threw unexcepted exception");
    } catch (RuntimeException f) {
      // Do nothing
    }

  }

  @Test
  public void testNextBytesEqualSeed() {
    try {
      Random rand = new Random(42);
      final byte[] seed = new byte[16];
      rand.nextBytes(seed);
      AesCtrDrbg drbg1 = new AesCtrDrbg(seed);
      AesCtrDrbg drbg2 = new AesCtrDrbg(seed);
      final int arraySize = 1500;
      byte[] bytes1 = new byte[arraySize];
      byte[] bytes2 = new byte[arraySize];
      drbg1.nextBytes(bytes1);
      drbg2.nextBytes(bytes2);
      assertArrayEquals(bytes1, bytes2); // Bytes should be equal when seed is equal
      assertFalse(Arrays.equals(bytes1, new byte[arraySize])); // The zero-array is very unlikely
      drbg1.nextBytes(bytes1);
      assertFalse(Arrays.equals(bytes1, bytes2)); // Each call gives new bytes
      drbg2.nextBytes(bytes2);
      assertArrayEquals(bytes1, bytes2); // The arays should now be back in sync
      assertFalse(Arrays.equals(bytes1, new byte[arraySize])); // Still the zero-array is unlikely
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      fail("Test threw unexcepted security exception");
    }
  }

  @Test
  public void testNextBytesUnequalSeed() {
    try {
      Random rand = new Random(42);
      final byte[] seed1 = new byte[16];
      final byte[] seed2 = new byte[16];
      rand.nextBytes(seed1);
      rand.nextBytes(seed2);
      AesCtrDrbg drbg1 = new AesCtrDrbg(seed1);
      AesCtrDrbg drbg2 = new AesCtrDrbg(seed2);
      final int arraySize = 150;
      byte[] bytes1 = new byte[arraySize];
      byte[] bytes2 = new byte[arraySize];
      drbg1.nextBytes(bytes1);
      drbg2.nextBytes(bytes2);
      // Bytes are unlikely to be equal when seed is unequal
      assertFalse(Arrays.equals(bytes1, bytes2));
      // The zero-array is very unlikely
      assertFalse(Arrays.equals(bytes1, new byte[arraySize]));
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      fail("Test threw unexcepted security exception");
    }
  }

}
