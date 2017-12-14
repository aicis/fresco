package dk.alexandra.fresco.tools.mascot.broadcast;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;


public class BroadcastValidation extends MultiPartyProtocol {

  public BroadcastValidation(MascotResourcePool resourcePool, Network network) {
    super(resourcePool, network);
  }

  MessageDigest getMessageDigest() {
    return resourcePool.getMessageDigest();
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
    network.sendToAll(digest);
    // receive others' digests
    List<byte[]> digests = network.receiveFromAll();
    // validate digests
    validateDigests(digest, digests);
  }

}
