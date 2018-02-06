package dk.alexandra.fresco.logging;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.network.Network;
import org.junit.Test;

public class NetworkLoggingDecoratorTest {

  @Test
  public void getNoOfParties() throws Exception {
    NetworkLoggingDecorator networkLoggingDecorator = new NetworkLoggingDecorator(new Network() {
      @Override
      public void send(int partyId, byte[] data) {

      }

      @Override
      public byte[] receive(int partyId) {
        return new byte[0];
      }

      @Override
      public int getNoOfParties() {
        return 22;
      }
    });
    assertThat(networkLoggingDecorator.getNoOfParties(), is(22));
    // This should be a nil operation since my network does not implement closeable
    networkLoggingDecorator.close();
  }
}