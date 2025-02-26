package dk.alexandra.fresco.tools.commitment;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Class representing a hash-based commitment. Secure assuming that SHA-256 is a
 * random oracle. An instantiated object represents a commitment by itself and
 * does <b>not</b> contain any secret information. An object gets instantiated
 * by calling the commit command.
 * <p>
 * The scheme itself is based on the ROM folklore scheme where the message to
 * commit to is concatenated with a random string and then hashed. The hash
 * digest serves as the commitment itself and the opening is the randomness and
 * the message committed to.
 * </p>
 *
 */
public class HashBasedCommitment {

  private static final String HASH_ALGORITHM = "SHA-256";
  /**
   * The length of the hash digest along with the randomness used.
   */
  public static final int DIGEST_LENGTH = 32; // 256 / 8 bytes

  /**
   * The length of the party ID prepended to the Hash
   * to prevent the commitment from being reused by another party.
   */
  private static final int ID_LENGTH = Integer.BYTES;
  /**
   * The actual value representing the commitment.
   */
  private byte[] commitmentVal = null;
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
   * @param myId
   *          The ID of the party performing the commitment.
   * @param rand
   *          A cryptographically secure randomness generator.
   * @param value
   *          The element to commit to.
   * @return The opening information needed to open the commitment.
   */
  public byte[] commit(int myId, Drbg rand, byte[] value) {
    if (commitmentVal != null) {
      throw new IllegalStateException("Already committed");
    }
    // Sample a sufficient amount of random bits
    byte[] randomness = new byte[DIGEST_LENGTH];
    rand.nextBytes(randomness);
    // Construct an array to contain the bytes to hash
    byte[] openingInfo = new byte[ID_LENGTH + value.length + randomness.length];
    System.arraycopy(integerToBytes(myId), 0, openingInfo, 0, ID_LENGTH);
    System.arraycopy(value, 0, openingInfo, ID_LENGTH, value.length);
    System.arraycopy(randomness, 0, openingInfo, value.length + ID_LENGTH,
        randomness.length);
    commitmentVal = digest.digest(openingInfo);
    return openingInfo;
  }

  /**
   * Serializes {@code value} in {@link ByteOrder#BIG_ENDIAN} order.
   *
   * <p> Inverse of {@code bytesToInteger}.
   *
   * @param value to serialize
   * @return value serialized in big endian order.
   */
  private static byte[] integerToBytes(int value) {
    return ByteBuffer.allocate(4).putInt(value).order(ByteOrder.BIG_ENDIAN).array();
  }

  /**
   * Deserializes {@code bytes} into an integer using {@link ByteOrder#BIG_ENDIAN} order.
   *
   * <p> Inverse of {@code integerToBytes}.
   *
   * @param bytes to deserialize
   * @return int deserialized in big endian order.
   */
  private static int bytesToInteger(byte[] bytes) {
    return ByteBuffer.allocate(4).put(bytes).order(ByteOrder.BIG_ENDIAN).getInt(0);
  }

  /**
   * Opens a committed object using information returned from the {@code commit} command.
   *
   * @param partyId
   *          The party ID of the commitment.
   * @param openingInfo
   *          The data needed to open this given commitment.
   * @return The value that was committed to.
   */
  public byte[] open(int partyId, byte[] openingInfo) {
    if (commitmentVal == null) {
      throw new IllegalStateException("No commitment to open");
    }
    if (openingInfo.length < DIGEST_LENGTH) {
      throw new MaliciousException(
          "The opening info is too small to be a commitment.");
    }
    // Hash the opening info and verify that it matches the value stored in
    // "commitmentValue" and the sending party id matches the party id in the commitment.
    byte[] digestValue = digest.digest(openingInfo);
    int commitmentPartyId = bytesToInteger(Arrays.copyOf(openingInfo, ID_LENGTH));
    if (partyId != commitmentPartyId) {
      throw new MaliciousException("The party id does not match with the commitment party id.");
    }
    if (Arrays.equals(digestValue, commitmentVal)) {
      // Extract the randomness and the value committed to from the openingInfo
      // The value comes first
      byte[] value = new byte[openingInfo.length - DIGEST_LENGTH - ID_LENGTH];
      System.arraycopy(openingInfo, ID_LENGTH, value, 0, value.length);
      return value;
    } else {
      throw new MaliciousException("The opening info does not match the commitment.");
    }
  }

  /**
   * Returns the byte array value that is the actual commitment.
   *
   * @return the byte array commitment
   */
  byte[] getCommitmentValue() {
    return commitmentVal;
  }

  /**
   * Sets the commitment value.
   *
   * @param commitmentValue
   *          the commitment value
   */
  void setCommitmentValue(byte[] commitmentValue) {
    this.commitmentVal = commitmentValue;
  }
}
