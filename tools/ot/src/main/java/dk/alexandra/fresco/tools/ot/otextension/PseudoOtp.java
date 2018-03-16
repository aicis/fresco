package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import java.util.Arrays;
import java.util.Objects;

/**
 * Pseudo <i>One-Time-Pad</i> (OTP) encryption. I.e., a variant of OTP where keys shorter than the
 * message are deterministically <i>stretched</i> to match the size of messages, and keys longer
 * than the message is truncated to match the message length.
 */
public final class PseudoOtp {

  private PseudoOtp() {
    // Should not be instantiated
  }

  /**
   * Given a candidate key OTP encrypts a message using either using the key directly or a key
   * pseudo randomly derived from the candidate, in order to match the length of the message.
   *
   * <p>
   * The key candidate is assumed to be uniformly random. Thus, if its size is longer or equal to
   * the message, the candidate key will be used directly as a OTP key. Otherwise, the key will be
   * deterministically <i>stretched</i> to match the length of the message in some secure way (e.g.,
   * using a hash function).
   * </p>
   *
   * @param message the message to be encrypted
   * @param keyCandidate the candidate key
   * @return the resulting cipher text
   */
  public static byte[] encrypt(byte[] message, byte[] keyCandidate) {
    return encrypt(message, keyCandidate, message.length);
  }

  /**
   * Given a candidate key OTP encrypts a message using either the key directly or a key pseudo
   * randomly derived from the candidate, in order to match the length of the message.
   *
   * <p>
   * The key candidate is assumed to be uniformly random. Thus, if its size longer or equal to the
   * message, the candidate key will be used directly as a OTP key. Otherwise, the key will be
   * deterministically <i>stretched</i> to match the length of the message in some secure way (e.g.,
   * using a hash function).
   * </p>
   *
   * @param message the message to be encrypted
   * @param keyCandidate the candidate key for OTP encryption
   * @param messageLength fixes the length of the message to encrypt, the given <code>message</code>
   *        will be either truncated or padded with zeroes to match this length.
   * @return the resulting cipher text
   */
  public static byte[] encrypt(byte[] message, byte[] keyCandidate, int messageLength) {
    Objects.requireNonNull(message);
    Objects.requireNonNull(keyCandidate);
    byte[] cipherText = LengthAdjustment.adjust(keyCandidate, messageLength);
    ByteArrayHelper.xor(cipherText, Arrays.copyOf(message, messageLength));
    return cipherText;
  }

  /**
   * Given a candidate key OTP decrypts a cipher text using either the key directly or a key pseudo
   * randomly derived from the candidate, in order to match the length of the cipher text.
   *
   * <p>
   * The key candidate is assumed to be uniformly random. Thus, if its size longer or equal to the
   * cipher text, the candidate key will be used directly as a OTP key. Otherwise, the key will be
   * deterministically <i>stretched</i> to match the length of the cipher text in some secure way
   * (e.g., using a hash function).
   * </p>
   *
   * @param cipherText the cipher text to be decrypted
   * @param keyCandidate the candidate key for OTP decryption
   * @return the resulting message
   */
  public static byte[] decrypt(byte[] cipherText, byte[] keyCandidate) {
    return decrypt(cipherText, keyCandidate, cipherText.length);
  }

  /**
   * Given a candidate key OTP decrypts a cipher text using either the key directly or a key pseudo
   * randomly derived from the candidate, in order to match the length of the cipher text.
   *
   * <p>
   * The key candidate is assumed to be uniformly random. Thus, if its size longer or equal to the
   * cipher text, the candidate key will be used directly as a OTP key. Otherwise, the key will be
   * deterministically <i>stretched</i> to match the length of the cipher text in some secure way
   * (e.g., using a hash function).
   * </p>
   *
   * @param cipherText the cipher text to be decrypted
   * @param keyCandidate the candidate key for OTP decryption
   * @param messageLength fixes the length of the message to decrypt, the given
   *        <code>cipherText</code> will be either truncated or padded with zeroes to match this
   *        length.
   * @return the resulting message
   */
  public static byte[] decrypt(byte[] cipherText, byte[] keyCandidate, int messageLength) {
    Objects.requireNonNull(cipherText);
    Objects.requireNonNull(keyCandidate);
    byte[] key = LengthAdjustment.adjust(keyCandidate, messageLength);
    ByteArrayHelper.xor(key, Arrays.copyOf(cipherText, messageLength));
    return key;
  }

}
