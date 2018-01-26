package dk.alexandra.fresco.tools.mascot.broadcast;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.junit.Test;


public class TestNetworkedBroadcastingNetworkProxy extends NetworkedTest {

  private byte[] runSender(MascotTestContext ctx, byte[] toSend) {
    BroadcastValidation validator =
        new BroadcastValidation(ctx.getResourcePool(), ctx.getNetwork());
    BroadcastingNetworkProxy broadcaster =
        new BroadcastingNetworkProxy(ctx.getNetwork(), validator);
    broadcaster.sendToAll(toSend);
    return broadcaster.receive(ctx.getMyId());
  }

  private byte[] runReceiver(MascotTestContext ctx, Integer inputterId) {
    BroadcastValidation validator =
        new BroadcastValidation(ctx.getResourcePool(), ctx.getNetwork());
    BroadcastingNetworkProxy broadcaster =
        new BroadcastingNetworkProxy(ctx.getNetwork(), validator);
    return broadcaster.receive(inputterId);
  }

  private MaliciousException runInconsistentSender(MascotTestContext ctx,
      Map<Integer, byte[]> toSend) {
    Network network = ctx.getNetwork();
    BroadcastValidation validator =
        new BroadcastValidation(ctx.getResourcePool(), ctx.getNetwork());
    BroadcastingNetworkProxy broadcaster =
        new BroadcastingNetworkProxy(ctx.getNetwork(), validator);
    for (int partyId = 1; partyId <= ctx.getNoOfParties(); partyId++) {
      network.send(partyId, toSend.get(partyId));
    }
    try {
      broadcaster.receive(ctx.getMyId()); // need to self-receive
    } catch (MaliciousException e) {
      return e;
    }
    return null;
  }

  private MaliciousException runReceiverAgainstInconsistentSender(MascotTestContext ctx,
      Integer inputterId) {
    BroadcastValidation validator =
        new BroadcastValidation(ctx.getResourcePool(), ctx.getNetwork());
    BroadcastingNetworkProxy broadcaster =
        new BroadcastingNetworkProxy(ctx.getNetwork(), validator);
    try {
      broadcaster.receive(inputterId);
    } catch (MaliciousException e) {
      return e;
    }
    return null;
  }

  @Test
  public void testValidBroadcastReceive() {
    initContexts(3);

    List<Callable<byte[]>> tasks = new ArrayList<>();
    byte[] toSend = new byte[] {0x01, 0x02};
    tasks.add(() -> runSender(contexts.get(1), new byte[] {0x01, 0x02}));
    tasks.add(() -> runReceiver(contexts.get(2), 1));
    tasks.add(() -> runReceiver(contexts.get(3), 1));

    List<byte[]> actuals = testRuntime.runPerPartyTasks(tasks);
    for (byte[] actual : actuals) {
      assertArrayEquals(toSend, actual);
    }
  }

  @Test
  public void testIncosistentBroadcastSender() {
    initContexts(3);

    final List<Callable<MaliciousException>> tasks = new ArrayList<>();
    Map<Integer, byte[]> toSend = new HashMap<>();
    toSend.put(1, new byte[] {0x01, 0x02}); // self-send
    toSend.put(2, new byte[] {0x01, 0x02});
    toSend.put(3, new byte[] {0x01, 0x03}); // incosistent

    tasks.add(() -> runInconsistentSender(contexts.get(1), toSend));
    tasks.add(() -> runReceiverAgainstInconsistentSender(contexts.get(2), 1));
    tasks.add(() -> runReceiverAgainstInconsistentSender(contexts.get(3), 1));

    List<MaliciousException> actuals = testRuntime.runPerPartyTasks(tasks);
    for (MaliciousException actual : actuals) {
      assertEquals("Broadcast validation failed", actual.getMessage());
    }
  }

}
