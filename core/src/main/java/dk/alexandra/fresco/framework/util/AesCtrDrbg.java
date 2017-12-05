package dk.alexandra.fresco.framework.util;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesCtrDrbg {

  private final Cipher cipher;
  private static final long LIMIT = 1L << 45;
  private int generatedBytes;

  /**
   * Creates a new DRBG based on AES in counter mode.
   *
   * @param seed the seed for the DRBG. This must be a valid AES-128 key (i.e., must be 16 bytes
   *        long)
   */
  public AesCtrDrbg(byte[] seed) throws GeneralSecurityException {
    if (seed.length != 16) {
      throw new RuntimeException(
          "Seed must be exactly 16 bytes, but the given seed is " + seed.length + " bytes long");
    }
    this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
    SecretKeySpec spec = new SecretKeySpec(seed, "AES");
    this.cipher.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(new byte[16]));
    generatedBytes = 0;
  }

  /**
   * Generate a given amount of pseudo random bytes.
   *
   * <p>
   * This DRBG implementation can only generate <i>2<sup>45</sup></i> bytes in total. An exception
   * will be thrown after that point.
   * </p>
   *
   * @param bytes an array to be filled with random bytes
   */
  public void nextBytes(byte[] bytes) throws GeneralSecurityException {
    if (generatedBytes + bytes.length < LIMIT) {
      cipher.update(new byte[bytes.length], 0, bytes.length, bytes);
      generatedBytes += bytes.length;
    } else {
      throw new RuntimeException("Too may bits generated");
    }
  }
}
