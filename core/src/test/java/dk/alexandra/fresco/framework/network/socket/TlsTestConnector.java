package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.net.Socket;
import java.security.Principal;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.x500.X500Principal;

public class TlsConnector implements NetworkConnector {

  private static final String NAME_FORMAT = "CN=P%d, OU=TLSTest, O=FRESCO, C=DK";
  private final Map<Integer, Socket> socketMap;

  TlsConnector(NetworkConfiguration conf, Duration timeout) {
    this(conf, timeout, (SSLSocketFactory) SSLSocketFactory.getDefault(),
        (SSLServerSocketFactory) SSLServerSocketFactory.getDefault());
  }

  TlsConnector(NetworkConfiguration conf, Duration timeout, SSLSocketFactory socketFactory,
      SSLServerSocketFactory serverFactory) {
    Connector connector = new Connector(conf, timeout, socketFactory,
        new ClientAuthSslSocketFactory(serverFactory));
    verifyPeers(connector.getSocketMap());
    socketMap = connector.getSocketMap();
  }

  @Override
  public Map<Integer, Socket> getSocketMap() {
    return socketMap;
  }

  private void verifyPeers(Map<Integer, Socket> socketMap) {
    for (Entry<Integer, Socket> entry : socketMap.entrySet()) {
      SSLSocket sock = (SSLSocket) entry.getValue();
      Principal principal;
      try {
        principal = sock.getSession().getPeerPrincipal();
      } catch (SSLPeerUnverifiedException e) {
        String message = String.format("Unable to validate P%d)", entry.getKey());
        throw new RuntimeException(message, e);
      }
      Principal expected = getExpectedX500Principal(entry.getKey());
      if (!principal.equals(expected)) {
        String message =
            String.format("Unable to validate P%d. Unexpected principal %s, expected %s",
                entry.getKey(), principal, expected);
        throw new MaliciousException(message);
      }
    }
  }

  private static Principal getExpectedX500Principal(int i) {
    return new X500Principal(
        String.format(NAME_FORMAT, i));
  }

}
