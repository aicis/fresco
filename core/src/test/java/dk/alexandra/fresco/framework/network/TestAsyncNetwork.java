package dk.alexandra.fresco.framework.network;

import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class TestAsyncNetwork extends AbstractCloseableNetworkTest {

  @SuppressWarnings("unchecked")
  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testStopSenderTwice() throws Exception {
    networks = createNetworks(2);
    // Cancel sendfuture to provoke an exception while sending
    Field f1 = networks.get(1).getClass().getDeclaredField("senders");
    f1.setAccessible(true);
    AsyncNetwork.Sender sender =
        ((HashMap<Integer, AsyncNetwork.Sender>)f1.get(networks.get(1))).get(2);
    sender.stop();
    sender.stop();
    f1.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS, expected = CancellationException.class)
  public void testFailedSender() throws Exception {
    networks = createNetworks(2);
    Field f1 = networks.get(1).getClass().getDeclaredField("senders");
    f1.setAccessible(true);
    AsyncNetwork.Sender sender =
        ((HashMap<Integer, AsyncNetwork.Sender>)f1.get(networks.get(1))).get(2);
    Field f2 = sender.getClass().getDeclaredField("future");
    f2.setAccessible(true);
    Future<Object> future = ((Future<Object>)f2.get(sender));
    future.cancel(true);
    try {
      future.get();
    } catch (CancellationException ce) {
      // Ignore
    }
    sender.stop();
    f1.setAccessible(false);
    f2.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS, expected = CancellationException.class)
  public void testFailedReceiver() throws Exception {
    networks = createNetworks(2);
    Field f1 = networks.get(1).getClass().getDeclaredField("receivers");
    f1.setAccessible(true);
    AsyncNetwork.Receiver receiver =
        ((HashMap<Integer, AsyncNetwork.Receiver>)f1.get(networks.get(1))).get(2);
    Field f2 = receiver.getClass().getDeclaredField("future");
    f2.setAccessible(true);
    Future<Object> future = ((Future<Object>)f2.get(receiver));
    future.cancel(true);
    try {
      future.get();
    } catch (CancellationException ce) {
      // Ignore
    }
    receiver.stop();
    f1.setAccessible(false);
    f2.setAccessible(false);
  }

  @SuppressWarnings("unchecked")
  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testReceiver() throws Exception {
    networks = createNetworks(2);
    Field f1 = networks.get(1).getClass().getDeclaredField("receivers");
    f1.setAccessible(true);
    AsyncNetwork.Receiver receiver =
        ((HashMap<Integer, AsyncNetwork.Receiver>)f1.get(networks.get(1))).get(2);
    receiver.stop();
    f1.setAccessible(false);
  }

  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testConnectInterrupt() throws Throwable {
    List<NetworkConfiguration> confs = getNetConfs(2);
    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<?> f = es.submit(() -> newCloseableNetwork(confs.get(1)));
    es.shutdownNow();
    try {
      f.get();
    } catch (InterruptedException e) {
      fail("Test should not be interrupted");
    } catch (ExecutionException e) {
      throw e.getCause();
    }
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS, expected = RuntimeException.class)
  public void testFinishingReceivers() {
    networks = createNetworks(2);
    // Set alive = false in order for the receiver to stop
    try {
      Field f1 = networks.get(1).getClass().getDeclaredField("alive");
      f1.setAccessible(true);
      ((AtomicBoolean) f1.get(networks.get(1))).set(false);
      f1.setAccessible(false);
      // wake up the receiver for it to notice it should stop
      networks.get(2).send(1, new byte[] { 0x01 });
      Field f2 = networks.get(1).getClass().getDeclaredField("receiveFutures");
      f2.setAccessible(true);
      @SuppressWarnings("unchecked")
      Future<Object> future = ((Map<Integer, Future<Object>>) f2.get(networks.get(1))).get(2);
      f2.setAccessible(false);
      future.get();
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
        | IllegalAccessException | InterruptedException | ExecutionException e) {
      fail("Reflection related error");
    }
    try {
      networks.get(1).receive(2);
      fail("The above receive should throw an exception");
    } finally {
      // Set alive = true so we can close the network properly
      try {
        Field f = networks.get(1).getClass().getDeclaredField("alive");
        f.setAccessible(true);
        ((AtomicBoolean) f.get(networks.get(1))).set(true);
        f.setAccessible(false);
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
          | IllegalAccessException e) {
        fail("Reflection related error");
      }
    }
  }

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf) {
    return new AsyncNetwork(conf);
  }

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf, Duration timeout) {
    return new AsyncNetwork(conf, timeout);
  }

}

