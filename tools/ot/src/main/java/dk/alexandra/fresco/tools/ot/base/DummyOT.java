package dk.alexandra.fresco.tools.ot.base;

import java.io.IOException;
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
    try {
      byte[] messageZeroRaw = this.network.receive(0, this.otherID);
      byte[] messageOneRaw = this.network.receive(0, this.otherID);
      return !choiceBit ? new BigInteger(1, messageZeroRaw)
          : new BigInteger(1, messageOneRaw);
    } catch (IOException e) {
      System.out.println("Broke while receiving " + e);
      return null;
    }
  }

  @Override
  public void send(BigInteger messageZero, BigInteger messageOne) {
    try {
      this.network.send(0, otherID, messageZero.toByteArray());
      this.network.send(0, otherID, messageOne.toByteArray());
    } catch (IOException e) {
      System.out.println("Broke while sending " + e);
    }
  }

}
