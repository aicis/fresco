package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

public class TestAesCtrDrbg {

  @Test
  public void testAesCtrDrbg() {
    Random rand = new Random(42);
    byte[] seed = new byte[32];
    rand.nextBytes(seed);
    new AesCtrDrbg(seed);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAesCtrDrbgLongSeed() {
    new AesCtrDrbg(new byte[33]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAesCtrDrbgShortSeed() {
      new AesCtrDrbg(new byte[1]);
  }

  @Test
  public void testInitCipher() {
    Random rand = new Random(42);
    byte[] seed = new byte[32];
    rand.nextBytes(seed);
    AesCtrDrbg drbg = new AesCtrDrbg(seed);
    drbg.initCipher(new byte[16], new byte[16]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInitCipherIllegalArguments() {
    AesCtrDrbg drbg = new AesCtrDrbg(new byte[32]);
    drbg.initCipher(new byte[16], new byte[1]);
  }

  @Test(expected = IllegalStateException.class)
  public void testIncrementReseedCounterException() {
    AesCtrDrbg drbg = new AesCtrDrbg(new byte[32]);
    drbg.incrementReseedCounter((1L << 48) + 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncrementReseedNegativeIncrement() {
    AesCtrDrbg drbg = new AesCtrDrbg(new byte[32]);
    drbg.incrementReseedCounter(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNextBytesBounded() {
    AesCtrDrbg drbg = new AesCtrDrbg(new byte[32]);
    drbg.nextBytesBounded(new byte[20], new byte[10]);
  }

  @Test
  public void testNextBytesEqualSeed() {
    Random rand = new Random(42);
    final byte[] seed = new byte[32];
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
  }

  @Test
  public void testNextBytesUnequalSeed() {
    Random rand = new Random(42);
    final byte[] seed1 = new byte[32];
    final byte[] seed2 = new byte[32];
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
  }

  @Test
  public void testNextBytesManyBytes() {
    Random rand = new Random(42);
    final byte[] seed1 = new byte[32];
    rand.nextBytes(seed1);
    rand.nextBytes(seed1);
    AesCtrDrbg drbg1 = new AesCtrDrbg(seed1);
    AesCtrDrbg drbg2 = new AesCtrDrbg(seed1);
    // Setting the array size to ~1 million should force state updates
    final int arraySize = (1 << 20);
    byte[] bytes1 = new byte[arraySize];
    byte[] bytes2 = new byte[arraySize];
    drbg1.nextBytes(bytes1);
    drbg2.nextBytes(bytes2);
    // Bytes should be equal
    assertArrayEquals(bytes1, bytes2);
    // The zero-array is very unlikely
    assertFalse(Arrays.equals(bytes1, new byte[arraySize]));

  }

}
