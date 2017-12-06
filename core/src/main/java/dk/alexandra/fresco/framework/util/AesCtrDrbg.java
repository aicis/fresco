package dk.alexandra.fresco.framework.util;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesCtrDrbg implements Drbg {

  private final Cipher cipher;
  private static final long RESEED_LIMIT = 1L << 45;
  private static final int UPDATE_LIMIT = 1 << 16;
  private int generatedBytes;
  private int reseedCounter;

  /**
   * Creates a new DRBG based on AES in counter mode.
   *
   * @param seed the seed for the DRBG. This must be a valid AES-128 key (i.e., must be 16 bytes
   *        long)
   */
  public AesCtrDrbg(byte[] seed) throws GeneralSecurityException {
    if (seed.length != 32) {
      throw new RuntimeException(
          "Seed must be exactly 32 bytes, but the given seed is " + seed.length + " bytes long");
    }    
    this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
    SecretKeySpec spec = new SecretKeySpec(seed, 0, 16, "AES");
    IvParameterSpec iv = new IvParameterSpec(seed, 16, seed.length);
    this.cipher.init(Cipher.ENCRYPT_MODE, spec, iv);
    reseedCounter = 0;
    generatedBytes = 0;
  }

  @Override
  public void nextBytes(byte[] bytes) {
    nextBytes(bytes, 0);
  }

  private void nextBytes(byte[] bytes, int offset) {
    int length = bytes.length - offset;
    length = length > UPDATE_LIMIT ? UPDATE_LIMIT : length;
    if (generatedBytes + length > UPDATE_LIMIT) {
      update();
    }
    try {
      this.cipher.update(new byte[bytes.length], offset, length, bytes);
      generatedBytes += length;
      offset += length;
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Exception generating bits", e);
    }
    if (offset == bytes.length) {
      nextBytes(bytes, offset);
    }
  }

  private void update() {
    byte[] key = new byte[16];
    byte[] iv = new byte[16];
    reseedCounter++;
    if (reseedCounter > RESEED_LIMIT) {
      throw new RuntimeException("Exceeded ");
    }
    try {
      this.cipher.update(iv, 0, iv.length, iv);
      this.cipher.update(key, 0, key.length, key);
      SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      this.cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Exception updating state", e);
    }
    generatedBytes = 0;
  }
}
