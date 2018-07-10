package dk.alexandra.fresco.framework.network;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class TestTlsSocketNetwork extends AbstractCloseableNetworkTest {

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf) {
    return newCloseableNetwork(conf, SocketNetwork.DEFAULT_CONNECTION_TIMEOUT);
  }

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf, Duration timeout) {
    try {
      SSLContext context = SSLContext.getInstance("TLSv1.2");
      context.init(null, null, null);
      SSLSocketFactory socketFactory = context.getSocketFactory();
      SSLServerSocketFactory serverFactory = context.getServerSocketFactory();
      NetworkConnector connector = new Connector(conf, timeout,
          socketFactory,
          serverFactory);
      return new SocketNetwork(conf, connector);
    } catch (Exception e) {
      fail("Failed to setup TLS connection");
      return null;
    }
  }

}
