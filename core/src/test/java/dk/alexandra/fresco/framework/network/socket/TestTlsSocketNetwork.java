package dk.alexandra.fresco.framework.network;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Demonstrates how to set up a socket network with SSLSockets (to get TLS protected channels).
 * <p>
 * For these tests we have generated a keystore for each party in the test and add their certificate
 * a single truststore that will be used by all parties.
 *
 * To generate these we used the script in <code>fresco/core/src/test/resource/genstores.sh</code>
 * </p>
 */
public class TestTlsSocketNetwork extends AbstractCloseableNetworkTest {

  private NetworkFactory factory;

  @Before
  public void setupFactory() {
    factory = new NetworkFactoryImpl();
  }

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf) {
    return newCloseableNetwork(conf, Connector.DEFAULT_CONNECTION_TIMEOUT);
  }

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf, Duration timeout) {
    return factory.newCloseableNetwork(conf, timeout);
  }

  /**
   * This tests the case where a trusted party (i.e., one which certificate is in our truststore),
   * is giving wrong id. I.e., the party is not participating in the protocol in a way that is
   * consistent with the party's certificate. Concretely, a party with a certificate corresponding
   * to an id of 3 will behave attempt to connect as party 2.
   */
  @Test(expected = MaliciousException.class)
  public void testMismatchedIdAndKey() throws InterruptedException, ExecutionException {
    NetworkFactory fact = new NetworkFactoryImpl() {

      @Override
      protected KeyManager[] getKeyStoreManager(int id) throws KeyStoreException, IOException,
          NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        String storeName = "keystore" + id;
        if (id == 2) {
          storeName = "keystore3";
        }
        try (InputStream is = classloader.getResourceAsStream(storeName)) {
          ks.load(is, "testpass".toCharArray());
        }
        KeyManagerFactory kmf =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "testpass".toCharArray());
        return kmf.getKeyManagers();
      }
    };
    int numParties = 2;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    Map<Integer, Future<CloseableNetwork>> futureMap = new HashMap<>(numParties);
    for (NetworkConfiguration conf : confs) {
      Future<CloseableNetwork> f =
          es.submit(() -> fact.newCloseableNetwork(conf, Connector.DEFAULT_CONNECTION_TIMEOUT));
      futureMap.put(conf.getMyId(), f);
    }
    try {
      futureMap.get(1).get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof MaliciousException) {
        // The exception we were expecting
        throw (MaliciousException) e.getCause();
      } else {

        throw e;
      }
    } finally {
      es.shutdownNow();
    }
  }

  /**
   * Tests the case where a party attempts to connect with an untrusted certificate.
   */
  @Test(expected = SSLHandshakeException.class)
  public void testUntrustedParty() throws Throwable {
    NetworkFactory fact = new NetworkFactoryImpl() {

      @Override
      protected KeyManager[] getKeyStoreManager(int id) throws KeyStoreException, IOException,
          NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        String storeName = "keystore" + id;
        if (id == 2) {
          storeName = "keystore-bad";
        }
        try (InputStream is = classloader.getResourceAsStream(storeName)) {
          ks.load(is, "testpass".toCharArray());
        }
        KeyManagerFactory kmf =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "testpass".toCharArray());
        return kmf.getKeyManagers();
      }
    };
    int numParties = 2;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    Map<Integer, Future<CloseableNetwork>> futureMap = new HashMap<>(numParties);
    for (NetworkConfiguration conf : confs) {
      Future<CloseableNetwork> f =
          es.submit(() -> fact.newCloseableNetwork(conf, Duration.ofSeconds(1)));
      futureMap.put(conf.getMyId(), f);
    }
    try {
      futureMap.get(1).get();
    } catch (ExecutionException e) {
      if (e.getCause() != null && e.getCause().getCause() instanceof SSLHandshakeException) {
        // The exception we were expecting
        throw e.getCause().getCause();
      } else {
        // Some other exception indicating test failed
        throw e;
      }
    }
  }


  /**
   * Provides TLS enabled closeable networks.
   */
  private interface NetworkFactory {

    CloseableNetwork newCloseableNetwork(NetworkConfiguration conf, Duration timeout);

  }

  private static class NetworkFactoryImpl implements NetworkFactory {

    @Override
    public CloseableNetwork newCloseableNetwork(NetworkConfiguration conf, Duration timeout) {
      SSLContext context = null;
      try {
        context = SSLContext.getInstance("TLSv1.2");
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        fail("Unable to create SSL context");
      }
      KeyManager[] kms = null;
      try {
        kms = getKeyStoreManager(conf.getMyId());
      } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
          | CertificateException | IOException e) {
        e.printStackTrace();
        fail("Unable to create keystore manager");
      }
      TrustManager[] tms = null;
      try {
        tms = getTrustStoreManager(conf.getMyId());
      } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
          | CertificateException | IOException e) {
        e.printStackTrace();
        fail("Unable to create truststore manager");
      }
      try {
        context.init(kms, tms, null);
      } catch (KeyManagementException e) {
        e.printStackTrace();
        fail("Unable to initiallize SSL context");
      }
      SSLSocketFactory socketFactory = context.getSocketFactory();
      SSLServerSocketFactory serverFactory = context.getServerSocketFactory();
      NetworkConnector connector = new TlsConnector(conf, timeout, socketFactory, serverFactory);
      return new SocketNetwork(conf, connector.getSocketMap());
    }

    protected TrustManager[] getTrustStoreManager(int id) throws KeyStoreException, IOException,
        NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      KeyStore ks = KeyStore.getInstance("PKCS12");
      try (InputStream is = classloader.getResourceAsStream("truststore")) {
        ks.load(is, "testpass".toCharArray());
      }
      TrustManagerFactory tmf =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);
      return tmf.getTrustManagers();
    }

    protected KeyManager[] getKeyStoreManager(int id) throws KeyStoreException, IOException,
        NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      KeyStore ks = KeyStore.getInstance("PKCS12");
      try (InputStream is = classloader.getResourceAsStream("keystore" + id)) {
        ks.load(is, "testpass".toCharArray());
      }
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, "testpass".toCharArray());
      return kmf.getKeyManagers();
    }

  }

}
