package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility for running an actively-secure broadcast channel.
 */
public class Broadcast {

  private final Network network;
  private final MessageDigest md;

  public Broadcast(Network network) {
    this(network, ExceptionConverter.safe(() -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Broadcast"));
  }

  private Broadcast(Network network, MessageDigest md) {
    this.network = network;
    this.md = md;
  }

  /**
   * Computes digest of messages and resets md.
   */
  private byte[] computeDigest(List<byte[]> messages) {
    for (byte[] message : messages) {
      md.update(message);
    }
    byte[] digest = md.digest();
    md.reset();
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

  public byte[] computeAndSendDigests(byte[] message) {
    return computeAndSendDigests(Collections.singletonList(message));
  }

  /**
   * Receives digests from other parties and checks that these are consistent with own digest.
   */
  public void receiveAndValidateDigests(byte[] ownDigest) {
    List<byte[]> received = network.receiveFromAll();
    validateDigests(ownDigest, received);
  }

}
