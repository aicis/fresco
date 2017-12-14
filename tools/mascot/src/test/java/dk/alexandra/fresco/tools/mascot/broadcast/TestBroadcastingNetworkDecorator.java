package dk.alexandra.fresco.tools.mascot.broadcast;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;

public class TestBroadcastingNetworkDecorator {

  private final Network validBroadcastNetwork = new BroadcastingNetworkDecorator(new Network() {

    @Override
    public void send(int partyId, byte[] data) {}

    @Override
    public byte[] receive(int partyId) {
      return null;
    }

    @Override
    public int getNoOfParties() {
      return 3;
    }
  }, null);

  @Test
  public void testGetNoParties() {
    assertEquals(3, validBroadcastNetwork.getNoOfParties());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructForLessThanThree() {
    new BroadcastingNetworkDecorator(new Network() {
      @Override
      public void send(int partyId, byte[] data) {}

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 2;
      }
    }, null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSendUnsupported() {
    validBroadcastNetwork.send(1, null);
  }

}
