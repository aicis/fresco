package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.network.Network;

public class DummyOT implements OT<BigInteger> {
  private int otherID;
  Network network;

  public DummyOT(int otherID, Network network) {
    super();
    this.otherID = otherID;
    this.network = network;
  }

  @Override
  public BigInteger receive(Boolean choiceBit) {
    byte[] messageZeroRaw = this.network.receive(this.otherID);
    byte[] messageOneRaw = this.network.receive(this.otherID);
    return !choiceBit ? new BigInteger(1, messageZeroRaw)
        : new BigInteger(1, messageOneRaw);
  }

  @Override
  public void send(BigInteger messageZero, BigInteger messageOne) {
    this.network.send(otherID, messageZero.toByteArray());
    this.network.send(otherID, messageOne.toByteArray());
  }

}
