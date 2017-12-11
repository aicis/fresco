package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class DummyOt implements Ot {

  private Integer otherId;
  Network network;

  public DummyOt(Integer otherId, Network network) {
    super();
    this.otherId = otherId;
    this.network = network;
  }

  @Override
  public StrictBitVector receive(Boolean choiceBit) {
    byte[] messageZeroRaw = this.network.receive(this.otherId);
    byte[] messageOneRaw = this.network.receive(this.otherId);
    return !choiceBit
        ? new StrictBitVector(messageZeroRaw, messageZeroRaw.length * 8)
        : new StrictBitVector(messageOneRaw, messageOneRaw.length * 8);
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    this.network.send(otherId, messageZero.toByteArray());
    this.network.send(otherId, messageOne.toByteArray());
  }

}
