package dk.alexandra.fresco.tools.ot.otextension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * A static utility class commonly used to adjust the length of a candidate byte array by either
 * truncating the array or stretching it using a PRG using the candidate byte array as a seed. The
 * PRG implemented in this using a cryptographic hash-function and a counter.
 *
 */
final class LengthAdjustment {

  static final String DIGEST_ALGO = "SHA-256";

  private LengthAdjustment() {
    // Should not be instantiated
  }

  /**
   * Generates an array of the desired length, either by truncating a candidate array, or stretching
   * it using a hash-function and a counter.
   *
   * @param candidate the candidate key
   * @param byteLength the desired key length
   * @return a key of the desired length
   */
  static byte[] adjust(byte[] candidate, int byteLength) {
    Objects.requireNonNull(candidate);
    if (byteLength < 0) {
      throw new IllegalArgumentException("Can not adjust length to negative length: " + byteLength);
    }
    byte[] key;
    if (candidate.length >= byteLength) {
      key = Arrays.copyOf(candidate, byteLength);
    } else {
      key = new byte[byteLength];
      int offset = 0;
      int counter = 0;
      while (offset < byteLength) {
        MessageDigest digest = getDigest(DIGEST_ALGO);
        digest.update(intToBytes(counter++));
        digest.update(candidate);
        int len = Math.min(digest.getDigestLength(), byteLength - offset);
        System.arraycopy(digest.digest(), 0, key, offset, len);
        offset += len;
      }
    }
    return key;
  }

  private static MessageDigest getDigest(String algo) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(algo);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(algo + " not supported", e);
    }
    return digest;
  }

  /**
   * Generates a byte array representation of an integer.
   *
   * @param i an integer
   * @return a corresponding byte array
   */
  private static byte[] intToBytes(int i) {
    byte[] result = new byte[Integer.BYTES];
    result[0] = (byte) (i >> 24);
    result[1] = (byte) (i >> 16);
    result[2] = (byte) (i >> 8);
    result[3] = (byte) (i);
    return result;
  }

}
