package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.MPCException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class implements the HMAC-DRBG as specified in
 * https://csrc.nist.gov/publications/detail/sp/800-90a/rev-1/final
 * 
 * <p>
 * Java's SecureRandom is not 'deterministic' in the sense that calls to setSeed (at least using
 * some crypto providers) only adds to the initial seed, that is taken from system state when
 * SecureRandom was created.
 * </p>
 * <p>
 * Often, in our protocols, we need a SecureRandom that can be seeded 'determinitically', e.g. in
 * the sense that two instances created with the same seed yields the same sequence of random bytes.
 * </p>
 * <p>
 * Note that DetermSecureRandom is not threadsafe.
 * </p>
 * <p>
 * Note also that reseeding (calling {@link #setSeed(byte[])}) should occur at least every 2^42
 * calls to {@link #nextBytes(byte[])} to be secure. No signal will be send about this.
 * </p>
 */
public class DetermSecureRandom extends SecureRandom {

  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_ALGORITHM = "HmacSHA256";
  private final String algorithm;
  private Mac mac = null;
  private byte[] val = null; // value - internally used
  private byte[] key = null; // key material

  /**
   * Convenience constructor. It calls {@link #DetermSecureRandom(byte[], String)} with the
   * parameters (new byte[]{0x00}, "HmacSHA256"). Note that the seed will be 0, and the security is
   * virtually non-existent at this point until the class is reseeded.
   * 
   * @throws NoSuchAlgorithmException If the default SHA-256 hash function is not found on the
   *         system.
   */
  public DetermSecureRandom() throws NoSuchAlgorithmException {
    this(new byte[] {0x00});
  }

  /**
   * Start with a seed and the default algorithm.
   * 
   * @param seed The starting seed
   * @throws NoSuchAlgorithmException If the default SHA-256 hash function is not found on the
   *         system.
   */
  public DetermSecureRandom(byte[] seed) throws NoSuchAlgorithmException {
    this(seed, DEFAULT_ALGORITHM);
  }

  /**
   * Deterministic secure random means that given a seed, it is deterministic what the output
   * becomes next time. This differs from Java's original SecureRandom in that if you give a seed to
   * this, it merely adds it to the entropy.
   *
   * @param seed The initial seed to use. This implicitly calls {@link #setSeed(byte[])} with this
   *        argument.
   * @param algorithm The algorithm to be used as the HMac hash function. Default is HMacSHA256.
   * @throws NoSuchAlgorithmException If the <code>algorithm</code> is not found on the system.
   */
  public DetermSecureRandom(byte[] seed, String algorithm) throws NoSuchAlgorithmException {
    this.algorithm = algorithm;
    this.mac = Mac.getInstance(algorithm);
    this.key = new byte[64];
    this.val = new byte[64];
    for (int i = 0; i < this.val.length; i++) {
      this.val[i] = 1;
    }
    initializeMac(this.key);
    setSeed(seed);
  }



  @Override
  public synchronized void nextBytes(byte[] bytes) {
    int pos = 0;
    while (pos < bytes.length) {
      this.val = this.mac.doFinal(this.val);
      int length = val.length;
      //Ensure that we don't break boundries
      if (length > bytes.length - pos) {
        length = bytes.length - pos;
      }
      System.arraycopy(val, 0, bytes, pos, length);
      pos += length;
    }
    update(null);
    // If we at some point want to introduce an indicator that a reseed should happen, we need to
    // have a reseedCounter counted here. The reseed interval is 2^48 as per the NIST details.
    // reseed_counter++;
  }

  private void update(byte[] providedData) {
    this.mac.update(val);
    this.key = this.mac.doFinal(new byte[] {0x00});
    initializeMac(this.key);
    this.val = this.mac.doFinal(this.val);
    if (providedData == null) {
      return;
    }
    this.mac.update(this.val);
    this.mac.update(new byte[] {0x01});
    this.key = this.mac.doFinal(providedData);
    initializeMac(this.key);
    this.val = mac.doFinal(this.val);
  }

  private void initializeMac(byte[] key) {
    try {
      this.mac.init(new SecretKeySpec(key, this.algorithm));
    } catch (InvalidKeyException e) {
      throw new MPCException("Key could not be generated from given data", e);
    }
  }

  @Override
  public void setSeed(byte[] seed) {
    update(seed);
  }
}
