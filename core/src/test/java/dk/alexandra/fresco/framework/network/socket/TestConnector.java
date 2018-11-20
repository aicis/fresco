package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import org.junit.Test;

/**
 * Note: this test simply covers code not tested in {@link TestSocketNetwork}.
 */
public class TestConnector {

  @Test(expected = InterruptedException.class)
  public void testInterruptWhileConnecting() throws Throwable {
    ExecutorService es = Executors.newFixedThreadPool(2);
    Map<Integer, NetworkConfiguration> confs = NetworkUtil.getNetworkConfigurations(2);
    List<Future<?>> futures = new ArrayList<>(2);
    for (NetworkConfiguration conf : confs.values()) {
      futures.add(es.submit(() -> new Connector(conf, Connector.DEFAULT_CONNECTION_TIMEOUT)));
    }
    es.shutdownNow();
    try {
      futures.get(0).get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof RuntimeException
          && e.getCause().getCause() instanceof InterruptedException) {
        throw e.getCause().getCause();
      } else {
        throw e;
      }

    }
  }

}
