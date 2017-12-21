package dk.alexandra.fresco.tools.mascot.broadcast;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.BaseProtocol;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import java.util.Arrays;
import java.util.List;

/**
 * Actively-secure protocol for performing hash-based broadcast validation. Allows participating
 * parties to ensure that a list of messages is consistent across all parties.
 */
public class BroadcastValidation extends BaseProtocol {

  public BroadcastValidation(MascotResourcePool resourcePool, Network network) {
    super(resourcePool, network);
  }

  /**
   * Computes digest of messages of messages and resets md.
   * 
   * @param messages
   * @return
   */
  byte[] computeDigest(List<byte[]> messages) {
    for (byte[] message : messages) {
      getMessageDigest().update(message);
    }
    byte[] digest = getMessageDigest().digest();
    getMessageDigest().reset();
    return digest;
  }

  /**
   * Compares all other digests with own digest and throws if any are not equal.
   * 
   * @param ownDigest hash of messages sent
   * @param otherDigests hashes received from other parties
   */
  void validateDigests(byte[] ownDigest, List<byte[]> otherDigests) {
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
    getNetwork().sendToAll(digest);
    // receive others' digests
    List<byte[]> digests = getNetwork().receiveFromAll();
    // validate digests
    validateDigests(digest, digests);
  }

}