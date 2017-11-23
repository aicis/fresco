package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.BitVector;

public class DummyOt implements Ot<BitVector> {

  private Integer otherId;
  private int messageBitLength;
  Network network;

  public DummyOt(Integer otherId, int messageBitLenght, Network network) {
    super();
    this.otherId = otherId;
    this.messageBitLength = messageBitLenght;
    this.network = network;
  }

  @Override
  public BitVector receive(Boolean choiceBit) {
    byte[] messageZeroRaw = this.network.receive(this.otherId);
    byte[] messageOneRaw = this.network.receive(this.otherId);
    return !choiceBit ? new BitVector(messageZeroRaw, messageBitLength)
        : new BitVector(messageOneRaw, messageBitLength);
  }

  @Override
  public void send(BitVector messageZero, BitVector messageOne) {
    this.network.send(otherId, messageZero.asByteArr());
    this.network.send(otherId, messageOne.asByteArr());
  }

}
