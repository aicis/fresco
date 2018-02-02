package dk.alexandra.fresco.framework.util;

/**
 * Interface for Deterministic Random Bit Generators.
 * <p>
 * Often, we need a source of randomness that can be seeded 'deterministically',
 * e.g. in the sense that two instances created with the same seed yields the
 * same sequence of pseudo random bytes, but can still not be easily guessed by
 * an adversary. That is, the generator is cryptographically secure if a long
 * enough seed has been given.
 * </p>
 * <p>
 * Implementations of this class will have the property that data generated is
 * pseudo-random and if all parties uses the same seed(s), calls to
 * {@link #nextBytes(byte[])} will be deterministic and secure against third
 * parties trying to guess the resulting randomness. For actual security
 * guarantees, we refer to the individual implementations.
 * </p>
 */
public interface Drbg {

  /**
   * Fills the given byte array with deterministic pseudo-random bytes.
   *
   * @param bytes The byte array which will be overwritten with random data.
   */
  void nextBytes(byte[] bytes);
}
