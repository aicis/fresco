package dk.alexandra.fresco.tools.mascot.mock;

import dk.alexandra.fresco.framework.network.Network;

public class MockNetwork implements Network {

  @Override
  public void send(int partyId, byte[] data) {}

  @Override
  public byte[] receive(int partyId) {
    return null;
  }

  @Override
  public int getNoOfParties() {
    return 0;
  }

}
