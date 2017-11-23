package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class DummyOt implements Ot<StrictBitVector> {

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
  public StrictBitVector receive(Boolean choiceBit) {
    byte[] messageZeroRaw = this.network.receive(this.otherId);
    byte[] messageOneRaw = this.network.receive(this.otherId);
    return !choiceBit ? new StrictBitVector(messageZeroRaw, messageBitLength)
        : new StrictBitVector(messageOneRaw, messageBitLength);
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    this.network.send(otherId, messageZero.toByteArray());
    this.network.send(otherId, messageOne.toByteArray());
  }

}
