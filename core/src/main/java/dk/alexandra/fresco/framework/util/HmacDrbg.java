package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.MPCException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the HMac-DRBG (HMac-Deterministic Random Bit Generator) as specified in
 * https://csrc.nist.gov/publications/detail/sp/800-90a/rev-1/final
 *
 * <p>
 * Note that this class is not threadsafe.
 * </p>
 * <p>
 * Note also that reseeding (calling {@link #setSeed(byte[])}) should occur at least every 2^42
 * calls to {@link #nextBytes(byte[])} to be secure. No signal will be send about this.
 * </p>
 */
public class HmacDrbg implements Drbg {

  private final Logger logger =
      LoggerFactory.getLogger(HmacDrbg.class);
  private static final String DEFAULT_ALGORITHM = "HmacSHA256";
  private final String algorithm;
  private Mac mac = null;
  private byte[] val = null; // value - internally used
  private byte[] key = null; // key material
  long reseedCounter;
  private List<byte[]> seeds;
  static final long MAX_RESEED_COUNT = (long) Math.pow(2, 48);

  /**
   * Start with a seed list and the default algorithm. See
   * {@link #HmacDeterministicRandomBitGeneratorImpl(String, byte[]...)} for further info.
   *
   * @param seeds The seeds used. If empty, the default 0 seed will be used. NB: This should
   *     happen
   *     only during testing as this is highly insecure.
   * @throws NoSuchAlgorithmException If the default HmacSHA256 hash function is not found on
   *     the
   *     system.
   */
  public HmacDrbg(byte[]... seeds) throws NoSuchAlgorithmException {
    this(DEFAULT_ALGORITHM, seeds);
  }

  /**
   * Deterministic secure random means that given a seed, it is deterministic what the output
   * becomes next time. This differs from Java's original SecureRandom in that if you give a seed to
   * this, it merely adds it to the entropy.
   *
   * @param seeds The seeds to use. Often a single seed will be more than enough as you can
   *     perform
   *     the {@link #nextBytes(byte[])} operation 2^48 times before the security guarantee does
   *     not hold and an exception will be thrown.
   * @param algorithm The algorithm to be used as the HMac hash function. Default is HMacSHA256.
   * @throws NoSuchAlgorithmException If the <code>algorithm</code> is not found on the system.
   */
  public HmacDrbg(String algorithm, byte[]... seeds)
      throws NoSuchAlgorithmException {
    this.algorithm = algorithm;
    this.seeds = new ArrayList<byte[]>();
    Collections.addAll(this.seeds, seeds);
    if (this.seeds.size() < 1) {
      logger.warn("DRBG initialized with no seeds. Using the very insecure 0 seed. "
          + "Should only be used during testing");
      this.seeds.add(new byte[]{0x00});
    }
    this.mac = Mac.getInstance(algorithm);
    this.key = new byte[64];
    this.val = new byte[64];
    for (int i = 0; i < this.val.length; i++) {
      this.val[i] = 1;
    }
    initializeMac(this.key);
    setSeed(this.seeds.remove(0));
  }

  @Override
  public synchronized void nextBytes(byte[] bytes) {
    int pos = 0;
    while (pos < bytes.length) {
      this.val = this.mac.doFinal(this.val);
      int length = val.length;
      // Ensure that we don't break boundaries
      if (length > bytes.length - pos) {
        length = bytes.length - pos;
      }
      System.arraycopy(val, 0, bytes, pos, length);
      pos += length;
    }
    update(null);
    this.reseedCounter++;
    if (this.reseedCounter >= MAX_RESEED_COUNT) {
      if (this.seeds.isEmpty()) {
        throw new MPCException("No more seeds available. Security guarantees no longer holds. "
            + "Please restart the application using more seeds to continue beyond this point.");
      }
      setSeed(this.seeds.remove(0));
    }
  }

  private void update(byte[] providedData) {
    this.mac.update(val);
    this.key = this.mac.doFinal(new byte[]{0x00});
    initializeMac(this.key);
    this.val = this.mac.doFinal(this.val);
    if (providedData == null) {
      return;
    }
    this.mac.update(this.val);
    this.mac.update(new byte[]{0x01});
    this.key = this.mac.doFinal(providedData);
    initializeMac(this.key);
    this.val = mac.doFinal(this.val);
  }

  private void initializeMac(byte[] key) {
    safeInitialize(new SecretKeySpec(key, algorithm));
  }

  void safeInitialize(SecretKey secretKeySpec) {
    try {
      mac.init(secretKeySpec);
    } catch (InvalidKeyException e) {
      throw new MPCException("Key could not be generated from given data", e);
    }
  }

  private void setSeed(byte[] seed) {
    update(seed);
  }
}
