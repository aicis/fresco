package dk.alexandra.fresco.commitment;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Class representing a hash-based commitment. Secure assuming that SHA-256 is a
 * random oracle. An instantiated object represents a commitment by itself and
 * does <b>not</b> contain any secret information. An object gets instantiated
 * by calling the commit command. <br/>
 * The scheme itself is based on the ROM folklore scheme where the message to
 * commit to is concatenated with a random string and then hashed. The hash
 * digest serves as the commitment itself and the opening is the randomness and
 * the message committed to.
 *
 * @author jot2re
 */
public class HashBasedCommitment {

  private static final String HASH_ALGORITHM = "SHA-256";
  /**
   * The length of the hash digest along with the randomness used.
   */
  public static final int DIGEST_LENGTH = 32; // 256 / 8 bytes
  /**
   * The actual value representing the commitment.
   */
  byte[] commitmentVal = null;
  private final MessageDigest digest;

  /**
   * Constructs a new commitment, not yet committed to any value.
   */
  public HashBasedCommitment() {
    digest = ExceptionConverter.safe(
        () -> MessageDigest.getInstance(HASH_ALGORITHM),
        "Missing secure, hash function which is dependent in this library");
  }

  /**
   * Initializes the commitment to commit to a specific value and returns the opening information.
   *
   * @param rand
   *          A cryptographically secure randomness generator.
   * @param value
   *          The element to commit to.
   * @return The opening information needed to open the commitment.
   */
  public byte[] commit(Drbg rand, byte[] value) {
    if (commitmentVal != null) {
      throw new IllegalStateException("Already committed");
    }
    // Sample a sufficient amount of random bits
    byte[] randomness = new byte[DIGEST_LENGTH];
    rand.nextBytes(randomness);
    // Construct an array to contain the bytes to hash
    byte[] openingInfo = new byte[value.length + randomness.length];
    System.arraycopy(value, 0, openingInfo, 0, value.length);
    System.arraycopy(randomness, 0, openingInfo, value.length,
        randomness.length);
    commitmentVal = digest.digest(openingInfo);
    return openingInfo;
  }

  /**
   * Opens a committed object using information returned from the {@code commit} command.
   *
   * @param openingInfo
   *          The data needed to open this given commitment.
   * @return The value that was committed to.
   */
  public byte[] open(byte[] openingInfo) {
    if (commitmentVal == null) {
      throw new IllegalStateException("No commitment to open");
    }
    if (openingInfo.length < DIGEST_LENGTH) {
      throw new MaliciousException(
          "The opening info is too small to be a commitment.");
    }
    // Hash the opening info and verify that it matches the value stored in
    // "commitmentValue"
    byte[] digestValue = digest.digest(openingInfo);
    if (Arrays.equals(digestValue, commitmentVal)) {
      // Extract the randomness and the value committed to from the openingInfo
      // The value comes first
      byte[] value = new byte[openingInfo.length - DIGEST_LENGTH];
      System.arraycopy(openingInfo, 0, value, 0,
          openingInfo.length - DIGEST_LENGTH);
      return value;
    } else {
      throw new MaliciousException("The opening info does not match the commitment.");
    }
  }
}