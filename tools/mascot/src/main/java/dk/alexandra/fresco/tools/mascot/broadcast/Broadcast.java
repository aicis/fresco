package dk.alexandra.fresco.tools.mascot.broadcast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;

public class Broadcast extends MultiPartyProtocol {

  private MessageDigest messageDigest;

  public Broadcast(MascotContext ctx) throws NoSuchAlgorithmException {
    super(ctx);
    this.messageDigest = MessageDigest.getInstance("SHA-256");
  }

  StrictBitVector sendBroadcastValidation(List<StrictBitVector> messages) {
    for (StrictBitVector b : messages) {
      messageDigest.update(b.toByteArray());
    }
    return sendAndReset();
  }

  private StrictBitVector sendAndReset() {
    byte[] digest = messageDigest.digest();
    messageDigest.reset();
    network.sendToAll(digest);
    return new StrictBitVector(digest, digest.length * 8);
  }

  boolean receiveBroadcastValidation(Network network, byte[] digest) {
    // TODO: should we check that we get messages from all players?
    boolean validated = true;
    List<byte[]> digests = network.receiveFromAll();
    for (byte[] d : digests) {
      boolean equals = Arrays.equals(d, digest);
      validated = validated && equals;
    }
    return validated;
  }
  
  byte[] broadcast(List<StrictBitVector> messages) {
    StrictBitVector ownDigest = sendBroadcastValidation(messages);
    return null;
  }

}
