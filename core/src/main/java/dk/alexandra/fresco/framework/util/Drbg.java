package dk.alexandra.fresco.framework.util;

/**
 * <p>
 * Often, in our protocols, we need a source of randomness that can be seeded 'determinitically',
 * e.g. in the sense that two instances created with the same seed yields the same sequence of
 * random bytes, but can still not be easily guessed by an adversary.
 * </p>
 * <p>
 * Implementations of this class will have the property that data generated is pseudorandom and if
 * all parties uses the same seed(s), calls to {@link #nextBytes(byte[])} will be deterministic and
 * secure. For actual security guarantees, we refer to the individual implementations.
 * </p>
 */
public interface Drbg {

  /**
   * Has the same functionality as {@link java.security.SecureRandom#nextBytes(byte[])}
   * 
   * @param bytes The byte array which will be overwritten with random data.
   */
  public void nextBytes(byte[] bytes);
}
