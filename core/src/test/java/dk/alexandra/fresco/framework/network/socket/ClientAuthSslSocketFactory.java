package dk.alexandra.fresco.framework.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

class ClientAuthSslSocketFactory extends ServerSocketFactory {

  SSLServerSocketFactory factory;

  public ClientAuthSslSocketFactory(SSLServerSocketFactory factory) {
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

  @Override
  public boolean equals(Object obj) {
    return factory.equals(obj);
  }

  public String[] getDefaultCipherSuites() {
    return factory.getDefaultCipherSuites();
  }

  public String[] getSupportedCipherSuites() {
    return factory.getSupportedCipherSuites();
  }

  @Override
  public int hashCode() {
    return factory.hashCode();
  }

  @Override
  public String toString() {
    return factory.toString();
  }

}
