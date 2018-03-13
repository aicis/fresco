package dk.alexandra.fresco.tools.ot.otextension;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

final class LengthAdjustment {

  private LengthAdjustment() {
    // Should not be instantiated
  }

  /**
   * Generates a key of the desired length, either by truncating the key, or stretching it using a
   * hash-function and a counter.
   *
   * @param candidate the candidate key
   * @param byteLength the desired key length
   * @return a key of the desired length
   */
  static byte[] adjust(byte[] candidate, int byteLength) {
    byte[] key;
    if (candidate.length >= byteLength) {
      key = Arrays.copyOf(candidate, byteLength);
    } else {
      key = new byte[byteLength];
      int offset = 0;
      int counter = 0;
      while (offset < byteLength) {
        MessageDigest digest;
        try {
          digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
          throw new RuntimeException("SHA-256 not supported", e1);
        }
        digest.update(intToBytes(counter++));
        digest.update(candidate);
        try {
          digest.digest(key, offset, Math.min(digest.getDigestLength(), byteLength - offset));
        } catch (DigestException e) {
          throw new RuntimeException("Error computing digest", e);
        }
        offset += Math.min(digest.getDigestLength(), byteLength - offset);
      }
    }
    return key;
  }

  /**
   * Generates a byte array representation of an integer.
   * @param i an integer
   * @return a corresponding byte array
   */
  static private byte[] intToBytes(int i) {
    byte[] result = new byte[Integer.BYTES];
    result[0] = (byte) (i >> 24);
    result[1] = (byte) (i >> 16);
    result[2] = (byte) (i >> 8);
    result[3] = (byte) (i);
    return result;
  }

}
