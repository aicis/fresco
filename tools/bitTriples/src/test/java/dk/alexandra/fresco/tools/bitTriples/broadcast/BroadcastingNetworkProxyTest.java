package dk.alexandra.fresco.tools.bitTriples.broadcast;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.network.Network;
import org.junit.Before;
import org.junit.Test;

public class BroadcastingNetworkProxyTest {

  private BroadcastingNetworkProxy broadcastingNetworkProxy;
  private Network network;
  private BroadcastValidation validator;

  @Before
  public void setup() {
    network = mock(Network.class);
    when(network.receive(anyInt())).thenReturn(new byte[] {23});
    when(network.getNoOfParties()).thenReturn(3);
    validator = mock(BroadcastValidation.class);
    broadcastingNetworkProxy = new BroadcastingNetworkProxy(network, validator);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void sendThrowsCorrectException() {
    broadcastingNetworkProxy.send(1, new byte[] {23});
  }

  @Test
  public void receive() {
    broadcastingNetworkProxy.receive(2);
    verify(network).receive(2);
    verify(validator).validate(any());
  }

  @Test
  public void getNoOfParties() {
    clearInvocations(network);
    broadcastingNetworkProxy.getNoOfParties();
    verify(network).getNoOfParties();
  }

  @Test (expected = IllegalArgumentException.class)
  public void testBroadcastNeedThreeOrMoreParties(){
    network = mock(Network.class);
    when(network.getNoOfParties()).thenReturn(2);
    broadcastingNetworkProxy = new BroadcastingNetworkProxy(network, validator);
  }
}
