package dk.alexandra.fresco.tools.mascot.broadcast;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import java.util.Arrays;
import java.util.List;

/**
 * Actively-secure protocol for performing hash-based broadcast validation. Allows participating
 * parties to ensure that a list of messages is consistent across all parties.
 */
public class BroadcastValidation {

  private final MascotResourcePool resourcePool;
  private final Network network;

  public BroadcastValidation(MascotResourcePool resourcePool, Network network) {
    this.resourcePool = resourcePool;
    this.network = network;
  }

  /**
   * Computes digest of messages and resets md.
   */
  private byte[] computeDigest(List<byte[]> messages) {
    for (byte[] message : messages) {
      resourcePool.getMessageDigest().update(message);
    }
    byte[] digest = resourcePool.getMessageDigest().digest();
    resourcePool.getMessageDigest().reset();
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
   * Performs broadcast validation on a list of messages.
   *
   * @param messages messages to validate
   */
  public void validate(List<byte[]> messages) {
    // compute digest
    byte[] digest = computeDigest(messages);
    // send it to others
    network.sendToAll(digest);
    // receive others' digests
    List<byte[]> digests = network.receiveFromAll();
    // validate digests
    validateDigests(digest, digests);
  }

}
