package dk.alexandra.fresco.framework.util;

/**
 * <p>
 * Java's SecureRandom is not 'deterministic' in the sense that calls to setSeed (at least using
 * some crypto providers) only adds to the initial seed, that is taken from system state when
 * SecureRandom was created.
 * </p>
 * <p>
 * Often, in our protocols, we need a SecureRandom that can be seeded 'determinitically', e.g. in
 * the sense that two instances created with the same seed yields the same sequence of random bytes,
 * but can still not be easily guessed by an adversary.
 * </p>
 * <p>
 * Implementations of this class will have the property that data generated is pseudorandom and if
 * parties calls {@link #setSeed(byte[])} with the same argument, subsequent calls to
 * {@link #nextBytes(byte[])} will be deterministic and secure. For actual security guarantees, we
 * refer to the individual implementations.
 * </p>
 */
public interface DeterministicSecureRandom {

  /**
   * Has the same functionality as {@link java.security.SecureRandom#nextBytes(byte[])}
   * 
   * @param bytes The byte array which will be overwritten with random data.
   */
  public void nextBytes(byte[] bytes);

  /**
   * Sets the seed used for generating randomness. If parties need to agree on the randomness
   * output, then the seed must be identical for all parties.
   * 
   * @param seed The initial randomness.
   */
  public void setSeed(byte[] seed);
}
