package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.junit.Test;

public class AesCtrDrbgFactoryTest {

  @Test
  public void testFromDerivedSeed() throws NoSuchAlgorithmException {
    // Note this test is implementation specific and may fail if we change how the seed is hashed
    MessageDigest md = MessageDigest.getInstance(AesCtrDrbgFactory.HASH_ALGORITHM);
    Drbg drbgA = AesCtrDrbgFactory.fromDerivedSeed((byte) 0x01, (byte) 0x02);
    byte[] seed = md.digest(new byte[] { 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x02 });
    assertEquals(AesCtrDrbg.SEED_LENGTH, seed.length);
    Drbg drbgB = new AesCtrDrbg(seed);
    byte[] bytesA = new byte[10000];
    byte[] bytesB = new byte[10000];
    drbgA.nextBytes(bytesA);
    drbgB.nextBytes(bytesB);
    assertArrayEquals(bytesA, bytesB);
  }

  @Test
  public void testFromRandomSeed() {
    byte[] seed = new byte[AesCtrDrbg.SEED_LENGTH];
    for (int i = 0; i < AesCtrDrbg.SEED_LENGTH; i++) {
      seed[i] = (byte) i;
    }
    Drbg drbgA = AesCtrDrbgFactory.fromRandomSeed(seed);
    Drbg drbgB = new AesCtrDrbg(seed);
    byte[] bytesA = new byte[10000];
    byte[] bytesB = new byte[10000];
    drbgA.nextBytes(bytesA);
    drbgB.nextBytes(bytesB);
    assertArrayEquals(bytesA, bytesB);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromRandomSeedShortSeed() {
    AesCtrDrbgFactory.fromRandomSeed(new byte[] { 0x42 });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromRandomSeedLongSeed() {
    byte[] bytes = new byte[33];
    AesCtrDrbgFactory.fromRandomSeed(bytes);
  }

  @Test
  public void testFromSampledSeed() {
    Drbg drbg = AesCtrDrbgFactory.fromSampledSeed();
    byte[] bytes = new byte[100];
    drbg.nextBytes(bytes);
    // By design these bytes are unpredictable, lets just test that they
    // at least changed.
    assertFalse(Arrays.equals(bytes, new byte[100]));

  }

}
