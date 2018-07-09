package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.time.Duration;

public class TestSocketNetwork extends AbstractCloseableNetworkTest {

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf) {
    return new SocketNetwork(conf);
  }

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf, Duration timeout) {
    return new SocketNetwork(conf, timeout);
  }

}
