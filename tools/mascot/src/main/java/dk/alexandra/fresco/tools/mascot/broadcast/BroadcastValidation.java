package dk.alexandra.fresco.tools.mascot.broadcast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;

public class BroadcastValidation extends MultiPartyProtocol {

  private MessageDigest messageDigest;

  public BroadcastValidation(MascotContext ctx) {
    super(ctx);
    try {
      this.messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Computes digest of messages of messages and resets md.
   * 
   * @param messages
   * @return
   */
  byte[] computeDigest(List<byte[]> messages) {
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
   * @param ownDigest
   * @param otherDigests
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
   * @param messages
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
