package dk.alexandra.fresco.tools.mascot.broadcast;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;


public class TestBroadcastValidation extends NetworkedTest {

  private Pair<Boolean, Exception> runSinglePartyBroadcastValidation(MascotTestContext ctx,
      List<byte[]> messages) {
    BroadcastValidation validator =
        new BroadcastValidation(ctx.getResourcePool(), ctx.getNetwork());
    boolean thrown = false;
    Exception exception = null;
    try {
      validator.validate(messages);
    } catch (MaliciousException e) {
      exception = e;
      thrown = true;
    }
    return new Pair<>(thrown, exception);
  }

  @Test
  public void testThreePartiesValidBroadcast() {
    // two parties run this
    initContexts(3);

    // messages
    List<byte[]> messages =
        Arrays.asList(new byte[]{0x00, 0x01}, new byte[]{0x02}, new byte[]{0x03});

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(1), messages);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(2), messages);
    Callable<Pair<Boolean, Exception>> partyThreeTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(3), messages);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    for (Pair<Boolean, Exception> res : results) {
      assertEquals(res.getFirst(), false);
    }
  }

  @Test
  public void testThreePartiesBroadcastDifferentValues() {
    // three parties run this
    initContexts(3);

    // messages
    List<byte[]> messages =
        Arrays.asList(new byte[]{0x00, 0x01}, new byte[]{0x02}, new byte[]{0x03});
    List<byte[]> badMessages =
        Arrays.asList(new byte[]{0x00, 0x02}, new byte[]{0x02}, new byte[]{0x03});

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(1), messages);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(2), badMessages);
    Callable<Pair<Boolean, Exception>> partyThreeTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(3), messages);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    for (Pair<Boolean, Exception> res : results) {
      boolean didThrow = res.getFirst();
      Exception exception = res.getSecond();
      assertEquals(didThrow, true);
      assertEquals(exception.getClass(), MaliciousException.class);
      assertEquals(exception.getMessage(), "Broadcast validation failed");
    }
  }

  @Test
  public void testThreePartiesBroadcastDifferentSize() {
    // three parties run this
    initContexts(3);

    // messages
    List<byte[]> messages =
        Arrays.asList(new byte[]{0x00, 0x01}, new byte[]{0x02}, new byte[]{0x03});
    List<byte[]> badMessages =
        Arrays.asList(new byte[]{0x00}, new byte[]{0x02}, new byte[]{0x03});

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(1), messages);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(2), badMessages);
    Callable<Pair<Boolean, Exception>> partyThreeTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(3), messages);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    for (Pair<Boolean, Exception> res : results) {
      boolean didThrow = res.getFirst();
      Exception exception = res.getSecond();
      assertEquals(didThrow, true);
      assertEquals(exception.getClass(), MaliciousException.class);
      assertEquals(exception.getMessage(), "Broadcast validation failed");
    }
  }

  @Test
  public void testThreePartiesBroadcastDifferentOrder() {
    // three parties run this
    initContexts(3);

    // messages
    List<byte[]> messages =
        Arrays.asList(new byte[]{0x00, 0x01}, new byte[]{0x02}, new byte[]{0x03});
    List<byte[]> badMessages =
        Arrays.asList(new byte[]{0x00, 0x01}, new byte[]{0x03}, new byte[]{0x02});

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(1), messages);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(2), badMessages);
    Callable<Pair<Boolean, Exception>> partyThreeTask =
        () -> runSinglePartyBroadcastValidation(contexts.get(3), messages);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    for (Pair<Boolean, Exception> res : results) {
      boolean didThrow = res.getFirst();
      Exception exception = res.getSecond();
      assertEquals(didThrow, true);
      assertEquals(exception.getClass(), MaliciousException.class);
      assertEquals(exception.getMessage(), "Broadcast validation failed");
    }
  }

}
