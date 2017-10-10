package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.MPCException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Java's SecureRandom is not 'deterministic' in the sense that calls to setSeed (at least using
 * some crypto providers) only adds to the initial seed, that is taken from system state when
 * SecureRandom was created.
 * <p>
 * Often, in our protocols, we need a SecureRandom that can be seeded 'determinitically', e.g. in
 * the sense that two instances created with the same seed yields the same sequence of random bytes.
 * </p>
 * <p>
 * Note that DetermSecureRandom is not threadsafe.
 * </p>
 */
public class DetermSecureRandom extends SecureRandom {

  private static final long serialVersionUID = 1L;
  private MessageDigest md = null;
  private byte[] seed = null;
  private int amount;

  /**
   * Detministic secure random means that given a seed, it is deterministic what the output becomes
   * next time. This differs from Java's original SecureRandom in that if you give a seed to this,
   * it merely adds it to the entropy.
   * 
   * @param amount is the amount of bytes used from each iteration of the hash-function. The
   *        hashfunction returns 32 bytes, so setting amount to e.g. 10 reduces the security to 22
   *        bytes.
   * @param seed The initial seed to use. This implicitly calls {@link #setSeed(byte[])} with this
   *        argument.
   * @throws MPCException if the algorithm is not found.
   */
  public DetermSecureRandom(int amount, byte[] seed) throws MPCException {
    try {
      md = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      throw new MPCException("Error while instantiating DetermSecureRandom", e);
    }
    if (amount >= md.getDigestLength()) {
      throw new MPCException("amount is not allowed to surpass the length of the digest");
    }
    this.amount = amount;
    setSeed(seed);
  }

  /**
   * Convenience constructor. It does the same as a call to {@link #DetermSecureRandom(int, byte[])}
   * with the parameters (1, new byte[]{0x00}) would do.
   */
  public DetermSecureRandom() throws MPCException {
    this(1, new byte[] {0x00});
  }

  @Override
  public synchronized void nextBytes(byte[] bytes) {
    byte[] res = null;
    this.md.update(this.seed);
    res = this.md.digest();
    int index = 0;
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = res[index++];
      if (index >= amount) {
        this.md.reset(); // ?
        byte[] newEntropy = new byte[md.getDigestLength() - amount];
        this.md.update(newEntropy);
        res = md.digest();
        index = 0;
      }
    }
  }

  /*
   * old version without parameter amount public synchronized void nextBytes(byte[] bytes) { byte[]
   * res = null; this.md.update(this.seed); for (int i = 0; i < bytes.length; i++) { res =
   * this.md.digest(); bytes[i] = res[0]; this.md.reset(); // ? this.md.update(res); } this.seed =
   * res; // ? }
   */

  public void setSeed(byte[] seed) {
    this.md.reset();
    this.seed = seed;
  }


}
