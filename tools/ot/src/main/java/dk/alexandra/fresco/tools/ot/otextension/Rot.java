package dk.alexandra.fresco.tools.ot.otextension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;

public class Rot {
  protected RotSender sender;
  protected RotReceiver receiver;

  public Rot(Cote cote) {
    this.sender = new RotSender(cote.getSender());
    this.receiver = new RotReceiver(cote.getReceiver());
  }

  public Rot(int myId, int otherId, int kbitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    CoteSender sender = new CoteSender(myId, otherId, kbitLength,
        lambdaSecurityParam, rand, network);
    CoteReceiver receiver = new CoteReceiver(myId, otherId, kbitLength,
        lambdaSecurityParam, rand, network);
    this.sender = new RotSender(sender);
    this.receiver = new RotReceiver(receiver);
  }

  public RotSender getSender() {
    return sender;
  }

  public RotReceiver getReceiver() {
    return receiver;
  }
}
