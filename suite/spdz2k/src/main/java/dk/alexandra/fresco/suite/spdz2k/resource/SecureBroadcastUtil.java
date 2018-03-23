package dk.alexandra.fresco.suite.spdz2k.resource;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for running secure broadcast.
 */
public class SecureBroadcastUtil {

  private final Network network;
  private final MessageDigest messageDigest;

  /**
   * Creates new {@link SecureBroadcastUtil}. <p>Requires SHA-256 message digest.</p>
   */
  public SecureBroadcastUtil(Network network) {
    this(network, ExceptionConverter.safe(() -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for SecureBroadcastUtil"));
  }

  private SecureBroadcastUtil(Network network, MessageDigest messageDigest) {
    this.network = network;
    this.messageDigest = messageDigest;
  }

  /**
   * Computes digest of messages and resets md.
   */
  private byte[] computeDigest(List<byte[]> messages) {
    for (byte[] message : messages) {
      messageDigest.update(message);
    }
    byte[] digest = messageDigest.digest();
    messageDigest.reset();
    return digest;
  }

  /**
   * Compares all other digests with own digest and throws if any are not equal.
   *
   * @param ownDigest hash of messages sent
   * @param otherDigests hashes received from other parties
   * @throws MaliciousException if validation fails
   */
  private void validateDigests(byte[] ownDigest, List<byte[]> otherDigests) {
    for (byte[] otherDigest : otherDigests) {
      if (!Arrays.equals(ownDigest, otherDigest)) {
        throw new MaliciousException("Broadcast validation failed");
      }
    }
  }

  /**
   * Computes digest of received messages and sends digest to other parties.
   */
  public byte[] computeAndSendDigests(List<byte[]> messages) {
    byte[] digest = computeDigest(messages);
    network.sendToAll(digest);
    return digest;
  }

  /**
   * Receives digests from other parties and checks that these are consistent with own digest.
   */
  public void receiveAndValidateDigests(byte[] ownDigest) {
    List<byte[]> received = network.receiveFromAll();
    validateDigests(ownDigest, received);
  }

}
