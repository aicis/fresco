package dk.alexandra.fresco.tools.mascot.broadcast;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;

public class TestBroadcastingNetworkDecorator {

  final static Network validBroadcastNetwork = new BroadcastingNetworkDecorator(new Network() {

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

  @Test
  public void testConstructForLessThanThree() {
    boolean thrown = false;
    try {
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
    } catch (IllegalArgumentException e) {
      assertEquals("Broadcast only needed for three or more parties", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testSendUnsupported() {
    boolean thrown = false;
    try {
      validBroadcastNetwork.send(1, null);
    } catch (UnsupportedOperationException e) {
      assertEquals("Broadcast network can only send to all", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testReceiveUnsupported() {
    boolean thrown = false;
    try {
      validBroadcastNetwork.receive(1);
    } catch (UnsupportedOperationException e) {
      assertEquals("Broadcast network can only receive from all", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

}
