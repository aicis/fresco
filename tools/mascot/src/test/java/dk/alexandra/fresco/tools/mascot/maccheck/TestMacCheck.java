package dk.alexandra.fresco.tools.mascot.maccheck;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public class TestMacCheck extends NetworkedTest {

  private Pair<Boolean, Exception> runSinglePartyMacCheck(MascotTestContext ctx,
      FieldElement opened, FieldElement macKeyShare, FieldElement macShare) {
    MacCheck macChecker = new MacCheck(ctx.getResourcePool(), ctx.getNetwork());
    boolean thrown = false;
    Exception exception = null;
    try {
      macChecker.check(opened, macKeyShare, macShare);
    } catch (MaliciousException e) {
      exception = e;
      thrown = true;
    }
    return new Pair<>(thrown, exception);
  }

  private void maliciousPartyOne(FieldElement opened, FieldElement macKeyShare,
      FieldElement macShare) {
    // two parties run this
    initContexts(2);

    // the "honest" party inputs consistent values
    FieldElement openedCorrect = getFieldDefinition().createElement(42);
    FieldElement macKeyShareCorrect = getFieldDefinition().createElement(7719);
    FieldElement macShareCorrect = getFieldDefinition().createElement(5204);

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyMacCheck(contexts.get(1), opened, macKeyShare, macShare);
    Callable<Pair<Boolean, Exception>> partyTwoTask = () -> runSinglePartyMacCheck(contexts.get(2),
        openedCorrect, macKeyShareCorrect, macShareCorrect);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    for (Pair<Boolean, Exception> res : results) {
      boolean didThrow = res.getFirst();
      Exception exception = res.getSecond();
      assertEquals(didThrow, true);
      assertEquals(exception.getClass(), MaliciousException.class);
      assertEquals(exception.getMessage(), "Malicious mac forging detected");
    }
  }

  private void maliciousPartyTwo(FieldElement opened, FieldElement macKeyShare,
      FieldElement macShare) {
    // two parties run this
    initContexts(2);

    // the "honest" party inputs consistent values
    FieldElement openedCorrect = getFieldDefinition().createElement(42);
    FieldElement macKeyShareCorrect = getFieldDefinition().createElement(11231);
    FieldElement macShareCorrect = getFieldDefinition().createElement(4444);

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask = () -> runSinglePartyMacCheck(contexts.get(1),
        openedCorrect, macKeyShareCorrect, macShareCorrect);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyMacCheck(contexts.get(2), opened, macKeyShare, macShare);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    for (Pair<Boolean, Exception> res : results) {
      boolean didThrow = res.getFirst();
      Exception exception = res.getSecond();
      assertEquals(didThrow, true);
      assertEquals(exception.getClass(), MaliciousException.class);
      assertEquals(exception.getMessage(), "Malicious mac forging detected");
    }
  }

  private void maliciousPartyThree(FieldElement opened, FieldElement macKeyShare,
      FieldElement macShare) {
    initContexts(3);

    FieldElement openedOne = getFieldDefinition().createElement(42);
    FieldElement macKeyShareOne = getFieldDefinition().createElement(11231);
    FieldElement macShareOne = getFieldDefinition().createElement(4444);

    FieldElement openedTwo = getFieldDefinition().createElement(42);
    FieldElement macKeyShareTwo = getFieldDefinition().createElement(7719);
    FieldElement macShareTwo = getFieldDefinition().createElement(5204);

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyMacCheck(contexts.get(1), openedOne, macKeyShareOne, macShareOne);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyMacCheck(contexts.get(2), openedTwo, macKeyShareTwo, macShareTwo);
    Callable<Pair<Boolean, Exception>> partyThreeTask =
        () -> runSinglePartyMacCheck(contexts.get(3), opened, macKeyShare, macShare);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    for (Pair<Boolean, Exception> res : results) {
      boolean didThrow = res.getFirst();
      Exception exception = res.getSecond();
      assertEquals(didThrow, true);
      assertEquals(exception.getClass(), MaliciousException.class);
      assertEquals(exception.getMessage(), "Malicious mac forging detected");
    }
  }

  @Test
  public void testTwoPartiesValidMacCheck() {
    // two parties run this
    initContexts(2);

    // left party inputs
    FieldElement macKeyShareOne = getFieldDefinition().createElement(11231);
    FieldElement openedOne = getFieldDefinition().createElement(42);
    FieldElement macShareOne = getFieldDefinition().createElement(9000);

    // right party inputs
    FieldElement macKeyShareTwo = getFieldDefinition().createElement(7719);
    FieldElement openedTwo = getFieldDefinition().createElement(42);
    FieldElement macShareTwo = getFieldDefinition().createElement(672);

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyMacCheck(contexts.get(1), openedOne, macKeyShareOne, macShareOne);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyMacCheck(contexts.get(2), openedTwo, macKeyShareTwo, macShareTwo);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    for (Pair<Boolean, Exception> res : results) {
      assertEquals(res.getFirst(), false);
    }
  }

  @Test
  public void testThreePartyValidMacCheck() {
    initContexts(3);

    FieldElement openedOne = getFieldDefinition().createElement(42);
    FieldElement macKeyShareOne = getFieldDefinition().createElement(11231);
    FieldElement macShareOne = getFieldDefinition().createElement(9000);

    FieldElement openedTwo = getFieldDefinition().createElement(42);
    FieldElement macKeyShareTwo = getFieldDefinition().createElement(7719);
    FieldElement macShareTwo = getFieldDefinition().createElement(700);

    FieldElement openedThree = getFieldDefinition().createElement(42);
    FieldElement macKeyShareThree = getFieldDefinition().createElement(1);
    FieldElement macShareThree = getFieldDefinition().createElement(14);

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyMacCheck(contexts.get(1), openedOne, macKeyShareOne, macShareOne);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyMacCheck(contexts.get(2), openedTwo, macKeyShareTwo, macShareTwo);
    Callable<Pair<Boolean, Exception>> partyThreeTask =
        () -> runSinglePartyMacCheck(contexts.get(3), openedThree, macKeyShareThree, macShareThree);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    for (Pair<Boolean, Exception> res : results) {
      assertEquals(false, res.getFirst());
    }
  }

  @Test
  public void testPartyOneTampersWithOpened() {
    FieldElement opened = getFieldDefinition().createElement(41); // tamper
    FieldElement macKeyShare = getFieldDefinition().createElement(11231);
    FieldElement macShare = getFieldDefinition().createElement(4444);
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyOneTampersWithMacKeyShare() {
    FieldElement opened = getFieldDefinition().createElement(42);
    FieldElement macKeyShare = getFieldDefinition().createElement(11031); // tamper
    FieldElement macShare = getFieldDefinition().createElement(4444);
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyOneTampersWithMacShare() {
    FieldElement opened = getFieldDefinition().createElement(42);
    FieldElement macKeyShare = getFieldDefinition().createElement(11231);
    FieldElement macShare = getFieldDefinition().createElement(4442); // tamper
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithOpened() {
    FieldElement opened = getFieldDefinition().createElement(41); // tamper
    FieldElement macKeyShare = getFieldDefinition().createElement(7719);
    FieldElement macShare = getFieldDefinition().createElement(5204);
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithMacKeyShare() {
    FieldElement opened = getFieldDefinition().createElement(42);
    FieldElement macKeyShare = getFieldDefinition().createElement(77); // tamper
    FieldElement macShare = getFieldDefinition().createElement(5204);
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithMacShare() {
    FieldElement opened = getFieldDefinition().createElement(42);
    FieldElement macKeyShare = getFieldDefinition().createElement(7719);
    FieldElement macShare = getFieldDefinition().createElement(4204); // tamper
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithOpened() {
    FieldElement opened = getFieldDefinition().createElement(41); // tamper
    FieldElement macKeyShare = getFieldDefinition().createElement(1);
    FieldElement macShare = getFieldDefinition().createElement(42);
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithMacKeyShare() {
    FieldElement opened = getFieldDefinition().createElement(42);
    FieldElement macKeyShare = getFieldDefinition().createElement(3); // tamper
    FieldElement macShare = getFieldDefinition().createElement(42);
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithMacShare() {
    FieldElement opened = getFieldDefinition().createElement(42);
    FieldElement macKeyShare = getFieldDefinition().createElement(1);
    FieldElement macShare = getFieldDefinition().createElement(34); // tamper
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

}
