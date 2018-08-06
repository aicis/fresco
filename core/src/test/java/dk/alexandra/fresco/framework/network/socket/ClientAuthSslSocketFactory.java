package dk.alexandra.fresco.framework.network.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Objects;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Decorates a {@link SSLServerSocketFactory} so that all created server sockets are set to require
 * client authentication.
 */
class ClientAuthSslSocketFactory extends ServerSocketFactory {

  private final SSLServerSocketFactory factory;

  /**
   * Creates a server socket factory that delegates all socket creation to a given factory for
   * {@link SSLServerSocket}, but sets the resulting sockets to require client authentication.
   *
   * @param factory a factory for SSLServerSocket's.
   */
  public ClientAuthSslSocketFactory(SSLServerSocketFactory factory) {
    Objects.requireNonNull(factory);
    this.factory = factory;
  }

  @Override
  public ServerSocket createServerSocket() throws IOException {
    SSLServerSocket sock = (SSLServerSocket) factory.createServerSocket();
    sock.setNeedClientAuth(true);
    return sock;
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress)
      throws IOException {
    SSLServerSocket sock = (SSLServerSocket) factory.createServerSocket(port, backlog, ifAddress);
    sock.setNeedClientAuth(true);
    return sock;
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog) throws IOException {
    SSLServerSocket sock = (SSLServerSocket) factory.createServerSocket(port, backlog);
    sock.setNeedClientAuth(true);
    return sock;
  }

  @Override
  public ServerSocket createServerSocket(int port) throws IOException {
    SSLServerSocket sock = (SSLServerSocket) factory.createServerSocket(port);
    sock.setNeedClientAuth(true);
    return sock;
  }

}
