package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.socket.Connector;
import dk.alexandra.fresco.framework.network.socket.NetworkConnector;
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

/**
 * A {@link NetworkConnector} used to demonstrate how a network can be connected with sockets using
 * TLS.
 *
 * <p>
 * Below we describe some of the implementation details of this example:
 * </p>
 * <p>
 * This works by simply handing a {@link SSLServerSocketFactory} and {@link SSLSocketFactory}
 * factories to regular {@link Connector} class. In order to force the created socket to use client
 * authentication (without changing the Connector implementation) we, however, cannot use the plain
 * SSLServerSocketFactory class. Instead we use {@link ClientAuthSslSocketFactory} as a decorator on
 * top of the plain SSLServerSocketFactory.
 *
 * An important responsibility of this class is to validate the SSL/TLS sessions once a network is
 * connected. I.e., since the SSL/TLS implementation accepts connections from any trusted peer we
 * need to verify that the peers are not trying to impersonate a different party (for example in the
 * case where P1 in a protocol tries impersonate P2). Here this is done in the
 * <code>verifyPeers</code> method by checking that the peers certificate conforms to the
 * pre-determined format {@value #NAME_FORMAT} (with %d substitutet by the party id).
 * </p>
 */
public class TlsTestConnector implements NetworkConnector {

  /**
   * The certificate name format.
   */
  public static final String NAME_FORMAT = "CN=P%d, OU=TLSTest, O=FRESCO, C=DK";
  private final Map<Integer, Socket> socketMap;

  /**
   * A new TLS connector based on a network configuration and connection timeout.
   *
   * <p>
   * This will use the default SSLSocketFactory and SSLServerSocketFactory factories.
   * </p>
   *
   * @param conf the network configuration
   * @param timeout the connection timeout duration
   */
  TlsTestConnector(NetworkConfiguration conf, Duration timeout) {
    this(conf, timeout, (SSLSocketFactory) SSLSocketFactory.getDefault(),
        (SSLServerSocketFactory) SSLServerSocketFactory.getDefault());
  }

  /**
   * A new TLS connector.
   *
   * @param conf the network configuration
   * @param timeout the connection timeout duration
   * @param socketFactory a factory for SSLSockets
   * @param serverFactory a factory for SSLServerSockets (note this factory will be wrapped to
   *        ensure client authenticated sockets.)
   */
  TlsTestConnector(NetworkConfiguration conf, Duration timeout, SSLSocketFactory socketFactory,
      SSLServerSocketFactory serverFactory) {
    Connector connector =
        new Connector(conf, timeout, socketFactory, new ClientAuthSslSocketFactory(serverFactory));
    verifyPeers(connector.getSocketMap());
    socketMap = connector.getSocketMap();
  }

  @Override
  public Map<Integer, Socket> getSocketMap() {
    return socketMap;
  }

  /**
   * Given a mapping from party id's to SSLSocket's checks that the certificate of the peer is
   * consistent with the party id.
   *
   * @param socketMap a mapping from party id to SSLSocket
   */
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
    return new X500Principal(String.format(NAME_FORMAT, i));
  }

}
