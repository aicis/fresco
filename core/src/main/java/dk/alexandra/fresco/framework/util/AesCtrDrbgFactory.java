package dk.alexandra.fresco.framework.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Factory class for {@link AesCtrDrbg} providing various ways to seed the DRBG.
 */
public class AesCtrDrbgFactory {

  /**
   * The hash used to for derived seeds.
   */
  public static final String HASH_ALGORITHM = "SHA-256";

  private AesCtrDrbgFactory() {
    // Should not be instantiated
  }

  /**
   * Creates a new DRBG with a seed derived from a given sequence of bytes (of any length).
   *
   * <p>
   * Internally this this essentially works by computing the seed as
   * <code> seed = hash(bytes)</code>
   * where <code>hash</code> is the hash function {@value #HASH_ALGORITHM}.
   * </p>
   * <p>
   * Note: This method should be used with caution as, depending on the context, a seed derived
   * from a low entropy sequence of bytes can be insecure to use.
   * </p>
   *
   * @param bytes a sequence of bytes from which to derive the seed
   * @return a new DRBG
   * @throws NoSuchAlgorithmException if the hash algorithm {@value #HASH_ALGORITHM} is not
   *      available on the system
   */
  public static Drbg fromDerivedSeed(byte... bytes) throws NoSuchAlgorithmException {
    return fromRandomSeed(hash(bytes));
  }

  /**
   * Creates a new DRBG using a given array of bytes as seed directly.
   *
   * <p>
   * Note: the seeds should be an array of exactly {@link AesCtrDrbg#SEED_LENGTH} uniformly random
   * bytes. If the length constraint is not met an {@link IllegalArgumentException} will be thrown.
   * </p>
   *
   * @param seed {@link AesCtrDrbg#SEED_LENGTH} uniformly random bytes
   * @return a new DRBG
   */
  public static Drbg fromRandomSeed(byte[] seed) {
    return new AesCtrDrbg(seed);
  }

  /**
   * Creates a new DRBG with a securely sampled random seed.
   * @return a new DRBG
   */
  public static Drbg fromSampledSeed() {
    return new AesCtrDrbg();
  }

  private static byte[] hash(final byte[] bytes) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
    // This prefix is specified in NIST SP 800-90A rev. 1
    // Not sure why should be needed, but likely will not hurt either
    md.update(new byte[] { 0x01, 0x00, 0x00, 0x01, 0x00});
    md.update(bytes);
    return md.digest();
  }

}
