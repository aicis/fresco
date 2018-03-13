package dk.alexandra.fresco.tools.ot.otextension;

/**
 * Pseudo <i>One-Time-Pad</i> (OTP) encryption. I.e., OTP where short keys are deterministically
 * <i>stretched</i> to match the size of messages if necessary.
 */
interface PseudoOtp {

  /**
   * Given a candidate key OTP encrypts a message using either using the key directly or a key
   * pseudo randomly derived from the candidate, in order to match the length of the message.
   *
   * <p>
   * The key candidate is assumed to be uniformly random. Thus, if its size longer or equal to the
   * message, the candidate key will be used directly as a OTP key. Otherwise, the key will be
   * deterministically <i>stretched</i> to match the length of the message in some secure way (e.g.,
   * using a hash function).
   * </p>
   *
   * @param message the message to be encrypted
   * @param keyCandidate the candidate key
   * @return the resulting cipher text
   */
  byte[] encrypt(byte[] message, byte[] keyCandidate);

  /**
   * Given a candidate key OTP encrypts a message using either the key directly or a key
   * pseudo randomly derived from the candidate, in order to match the length of the message.
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
  byte[] encrypt(byte[] message, byte[] keyCandidate, int messageLength);

  /**
   * Given a candidate key OTP decrypts a cipher text using either the key directly or a key
   * pseudo randomly derived from the candidate, in order to match the length of the cipher text.
   *
   * <p>
   * The key candidate is assumed to be uniformly random. Thus, if its size longer or equal to the
   * cipher text, the candidate key will be used directly as a OTP key. Otherwise, the key will be
   * deterministically <i>stretched</i> to match the length of the cipher text in some secure way (e.g.,
   * using a hash function).
   * </p>
   *
   * @param ciphterText the cipher text to be decrypted
   * @param keyCandidate the candidate key for OTP decryption
   * @return the resulting message
   */
  byte[] decrypt(byte[] cipherText, byte[] keyCandidate);

  /**
   * Given a candidate key OTP decrypts a cipher text using either the key directly or a key
   * pseudo randomly derived from the candidate, in order to match the length of the cipher text.
   *
   * <p>
   * The key candidate is assumed to be uniformly random. Thus, if its size longer or equal to the
   * cipher text, the candidate key will be used directly as a OTP key. Otherwise, the key will be
   * deterministically <i>stretched</i> to match the length of the cipher text in some secure way (e.g.,
   * using a hash function).
   * </p>
   *
   * @param ciphterText the cipher text to be decrypted
   * @param keyCandidate the candidate key for OTP decryption
   * @param messageLength fixes the length of the message to decrypt, the given <code>cipherText</code>
   *        will be either truncated or padded with zeroes to match this length.
   * @return the resulting message
   */
  byte[] decrypt(byte[] cipherText, byte[] keyCandidate, int messageLength);

}
