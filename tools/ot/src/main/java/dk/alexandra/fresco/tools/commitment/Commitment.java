package dk.alexandra.fresco.tools.commitment;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;

/**
 * Class representing a hash-based commitment. Secure assuming that SHA-256 is a
 * random oracle. An instantiated object represents a commitment by itself and
 * does *not* contain any secret information. An object gets instantiated by
 * calling the commit command.
 * 
 * The scheme itself is based on the ROM folklore scheme where the message to
 * commit to is concatenated with a random string and then hashed. The hash
 * digest serves as the commitment itself and the opening is the randomness and
 * the message committed to.
 * 
 * @author jot2re
 *
 */
public class Commitment implements Serializable {

  private static final long serialVersionUID = 1L;
  // The actual value representing the commitment
  private byte[] commitmentVal = null;
  // The computational security parameter
  private int kbitLength;

  /**
   * Constructs a new commitment, not yet committed to any value.
   * 
   * @param kbitLength
   *          The computational security parameter.
   */
  public Commitment(int kbitLength) {
    if (kbitLength < 1) {
      throw new IllegalArgumentException("Illegal constructor parameters");
    }
    this.kbitLength = kbitLength;
  }

  /**
   * Initializes the commitment to commit to a specific value and returns the
   * opening information.
   * 
   * @param rand
   *          A cryptographically secure randomness generator.
   * @param value
   *          The element to commit to
   * @return The opening information needed to open the commitment
   * @throws FailedCommitmentException
   *           Gets thrown if something internally, *not* malicious, goes wrong.
   */
  public Serializable commit(Random rand, Serializable value)
      throws FailedCommitmentException {
    if (commitmentVal != null) {
      throw new IllegalStateException("Already committed");
    }
    // Sample a sufficient amount of random bits
    byte[] randomness = new byte[kbitLength / 8];
    rand.nextBytes(randomness);
    commitmentVal = computeDigest(randomness, value);
    return new OpenInfo(randomness, value);
  }

  /**
   * Opens a committed object.
   * 
   * @param open
   *          The data needed to open this given commitment
   * @return The value that was committed to
   * @throws MaliciousCommitmentException
   *           Exception is thrown if the opening information does not match
   *           this commitment
   * @throws FailedCommitmentException
   *           Exception is thrown if something, *not* malicious, goes wrong.
   */
  public Serializable open(Serializable open)
      throws MaliciousCommitmentException, FailedCommitmentException {
    if (commitmentVal == null) {
      throw new IllegalStateException("No commitment to open");
    }
    try {
      OpenInfo openInfo = (OpenInfo) open;
      byte[] digest = computeDigest(openInfo.randomness, openInfo.value);
      if (Arrays.equals(digest, commitmentVal)) {
        return openInfo.value;
      } else {
        throw new MaliciousCommitmentException(
            "Opening does not match commitment.");
      }
    } catch (ClassCastException e) {
      throw new MaliciousCommitmentException(
          "The object given to the open method is not a proper commitment opening object.");
    }
  }

  /**
   * Computes a hash digest of "value" concatenated with "randomness".
   * 
   * @param randomness
   *          The randomness used to ensure the hiding property
   * @param value
   *          The value to commit to.
   * @return The digest as a byte array
   * @throws FailedCommitmentException
   *           Thrown if an internal, non-malicious, error occurs.
   */
  private byte[] computeDigest(byte[] randomness, Serializable value)
      throws FailedCommitmentException {
    try {
      byte[] valBytes = ByteArrayHelper.serialize(value);
      // Construct an array to contain the byte to hash
      byte[] toHash = new byte[valBytes.length + randomness.length];
      System.arraycopy(valBytes, 0, toHash, 0, valBytes.length);
      System.arraycopy(randomness, 0, toHash, valBytes.length,
          randomness.length);
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(valBytes);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new FailedCommitmentException(
          "Commitment failed. No malicious behaviour detected. "
              + "Failure was caused by the following internal error: "
              + e.getMessage());
    }
  }

  /**
   * Serialize and send a commitment.
   * 
   * @param otherId
   *          The ID of the party to send to
   * @param network
   *          The network to send the commitment over
   * @param comm
   *          The commitment to send
   * @throws IOException
   *           An internal, non-malicious, error occurred.
   */
  public static void sendCommitment(Commitment comm, int otherId,
      Network network) throws IOException {
    byte[] serializedComm = ByteArrayHelper.serialize(comm);
    network.send(otherId, serializedComm);
  }

  /**
   * Receive a commitment and deserialize it.
   * 
   * @param otherId
   *          The ID of the party to send to
   * @param network
   *          The network to send the commitment over
   * @return The deserialized commitment
   * @throws IOException
   *           An internal, non-malicious, error occurred.
   * @throws ClassNotFoundException
   *           An internal, non-malicious, error occurred.
   */
  public static Commitment receiveCommitment(int otherId, Network network)
      throws IOException, ClassNotFoundException {
    byte[] serializedComm = network.receive(otherId);
    return (Commitment) ByteArrayHelper.deserialize(serializedComm);
  }

  // TODO OpenInfo should be public class with its own serializer
  
  /**
   * Internal class representing the data needed to open a commitment.
   * 
   * @author jot2re
   *
   */
  private class OpenInfo implements Serializable {
    private static final long serialVersionUID = -5212255616479757356L;

    private Serializable value;
    private byte[] randomness;

    public OpenInfo(byte[] randomness, Serializable value) {
      this.value = value;
      this.randomness = randomness;
    }
  }
}
