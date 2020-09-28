package dk.alexandra.fresco.logging;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator.PartyStats;
import java.util.HashMap;
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

  @Test
  public void reset() {
    HashMap<Integer, PartyStats> partyStatsMap = mock(HashMap.class);
    NetworkLoggingDecorator decorator = new NetworkLoggingDecorator(mock(Network.class), partyStatsMap);
    decorator.reset();

    verify(partyStatsMap, times(1)).clear();
  }
}
