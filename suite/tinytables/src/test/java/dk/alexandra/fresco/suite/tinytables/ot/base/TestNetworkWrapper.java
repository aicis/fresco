package dk.alexandra.fresco.suite.tinytables.ot.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.network.CloseableNetwork;
import java.io.IOException;
import org.junit.Test;

public class TestNetworkWrapper {

  @Test
  public void testClose() {

    NetworkWrapper nw = new NetworkWrapper(new CloseableNetwork() {

      @Override
      public void close() throws IOException {
        // Ignore
      }

      @Override
      public void send(int partyId, byte[] data) {
        // Ignore
      }

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return -1;
      }
    }, 1);
    assertFalse(nw.isClosed());
    nw.close();
    assertTrue(nw.isClosed());
  }

}
