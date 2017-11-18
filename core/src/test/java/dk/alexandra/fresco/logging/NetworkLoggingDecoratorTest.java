package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.network.Network;
import org.hamcrest.core.Is;
import org.junit.Assert;
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
    Assert.assertThat(networkLoggingDecorator.getNoOfParties(), Is.is(22));
    // This should be a nil operation since my network does not implement closeable
    networkLoggingDecorator.close();
  }
}