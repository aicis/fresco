package dk.alexandra.fresco.framework.util;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of a deterministic random bit generator (DRBG) using AES in counter mode.
 */
public class AesCtrDrbg implements Drbg {

  private static final long RESEED_LIMIT = 1L << 48;
  private static final int UPDATE_LIMIT = 1 << 16;
  private Cipher cipher;
  private int generatedBytes;
  private int reseedCounter;

  /**
   * Creates a new DRBG based on AES in counter mode.
   *
   * @param seed the seed for the DRBG. This must be a valid AES-128 key (i.e., must be 32 bytes
   *        long)
   * @throws IllegelArgumentException if seed is not of the correct length (32 bytes)
   */
  public AesCtrDrbg(byte[] seed) {
    if (seed.length != 32) {
      throw new IllegalArgumentException(
          "Seed must be exactly 32 bytes, but the given seed is " + seed.length + " bytes long");
    }
    byte[] key = new byte[16];
    byte[] iv = new byte[16];
    System.arraycopy(seed, 0, key, 0, 16);
    System.arraycopy(seed, 16, iv, 0, 16);
    this.cipher = initCipher(key, iv);
    reseedCounter = 0;
    generatedBytes = 0;
  }

  @Override
  public void nextBytes(byte[] bytes) {
    if (bytes.length <= UPDATE_LIMIT) {
      nextBytesBounded(new byte[bytes.length], bytes);
    } else {
      int offset = 0;
      byte[] temp = new byte[UPDATE_LIMIT];
      byte[] zeroes = new byte[temp.length];
      while (bytes.length - offset > UPDATE_LIMIT) {
        nextBytesBounded(zeroes, temp);
        System.arraycopy(temp, 0, bytes, offset, temp.length);
        offset += UPDATE_LIMIT;
      }
      temp = new byte[bytes.length - offset];
      zeroes = new byte[bytes.length - offset];
      nextBytesBounded(zeroes, temp);
      System.arraycopy(temp, 0, bytes, offset, temp.length);
    }
  }

  /**
   * Generates enough pseudo-random bytes to fill a given array.
   * <p>
   * Note: this method is unsafe the sense that it expects two array of equal size at most
   * {@value #UPDATE_LIMIT} and does not check for this.
   * </p>
   *
   * @param zeroes an array of zero bytes of length equal to output array.
   * @param output an array of size at most {@value #UPDATE_LIMIT} which will be filled with pseudo
   *        random bytes.
   */
  void nextBytesBounded(byte[] zeroes, byte[] output) {
    if (generatedBytes + output.length > UPDATE_LIMIT) {
      update();
    }
    try {
      this.cipher.update(zeroes, 0, zeroes.length, output);
    } catch (ShortBufferException e) {
      throw new IllegalArgumentException("Exception generating bits", e);
    }
    generatedBytes += output.length;
  }

  /**
   * Initializes an AES cipher with a given key and iv.
   *
   * @param key the key
   * @param iv the iv
   * @param c the cipher
   */
  Cipher initCipher(byte[] key, byte[] iv) {
    SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    try {
      Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
      return c;
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException("Exception initilizing cipher", e);
    }
  }

  /**
   * Increment the reseed counter of this DRBG.
   */
  void incrementReseedCounter(long increment) {
    if (increment < 0) {
      throw new IllegalArgumentException("Negative increment.");
    }
    if (reseedCounter + increment > RESEED_LIMIT) {
      throw new IllegalStateException(
          "Exceeded limit on generation requests. " + "A DRBG with a fresh seed should be used.");
    }
    reseedCounter += increment;
  }

  private void update() {
    incrementReseedCounter(1);
    generatedBytes = 0;
    byte[] key = new byte[16];
    byte[] iv = new byte[16];
    nextBytes(iv);
    nextBytes(key);
    this.cipher = initCipher(key, iv);
  }
}
