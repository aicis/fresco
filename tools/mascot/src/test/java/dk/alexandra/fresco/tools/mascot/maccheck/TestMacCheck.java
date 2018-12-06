package dk.alexandra.fresco.tools.mascot.maccheck;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public class TestMacCheck extends NetworkedTest {

  private Pair<Boolean, Exception> runSinglePartyMacCheck(MascotTestContext ctx,
      MascotFieldElement opened, MascotFieldElement macKeyShare, MascotFieldElement macShare) {
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

  private void maliciousPartyOne(MascotFieldElement opened, MascotFieldElement macKeyShare,
      MascotFieldElement macShare) {
    // two parties run this
    initContexts(2);

    // the "honest" party inputs consistent values
    MascotFieldElement openedCorrect = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShareCorrect = new MascotFieldElement(7719, getModulus());
    MascotFieldElement macShareCorrect = new MascotFieldElement(5204, getModulus());

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

  private void maliciousPartyTwo(MascotFieldElement opened, MascotFieldElement macKeyShare,
      MascotFieldElement macShare) {
    // two parties run this
    initContexts(2);

    // the "honest" party inputs consistent values
    MascotFieldElement openedCorrect = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShareCorrect = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macShareCorrect = new MascotFieldElement(4444, getModulus());

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

  private void maliciousPartyThree(MascotFieldElement opened, MascotFieldElement macKeyShare,
      MascotFieldElement macShare) {
    initContexts(3);

    MascotFieldElement openedOne = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macShareOne = new MascotFieldElement(4444, getModulus());

    MascotFieldElement openedTwo = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    MascotFieldElement macShareTwo = new MascotFieldElement(5204, getModulus());

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
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement openedOne = new MascotFieldElement(42, getModulus());
    MascotFieldElement macShareOne = new MascotFieldElement(9000, getModulus());

    // right party inputs
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    MascotFieldElement openedTwo = new MascotFieldElement(42, getModulus());
    MascotFieldElement macShareTwo = new MascotFieldElement(672, getModulus());

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

    MascotFieldElement openedOne = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macShareOne = new MascotFieldElement(9000, getModulus());

    MascotFieldElement openedTwo = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    MascotFieldElement macShareTwo = new MascotFieldElement(700, getModulus());

    MascotFieldElement openedThree = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShareThree = new MascotFieldElement(1, getModulus());
    MascotFieldElement macShareThree = new MascotFieldElement(14, getModulus());

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
    MascotFieldElement opened = new MascotFieldElement(41, getModulus()); // tamper
    MascotFieldElement macKeyShare = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macShare = new MascotFieldElement(4444, getModulus());
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyOneTampersWithMacKeyShare() {
    MascotFieldElement opened = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShare = new MascotFieldElement(11031, getModulus()); // tamper
    MascotFieldElement macShare = new MascotFieldElement(4444, getModulus());
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyOneTampersWithMacShare() {
    MascotFieldElement opened = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShare = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macShare = new MascotFieldElement(4442, getModulus()); // tamper
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithOpened() {
    MascotFieldElement opened = new MascotFieldElement(41, getModulus()); // tamper
    MascotFieldElement macKeyShare = new MascotFieldElement(7719, getModulus());
    MascotFieldElement macShare = new MascotFieldElement(5204, getModulus());
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithMacKeyShare() {
    MascotFieldElement opened = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShare = new MascotFieldElement(77, getModulus()); // tamper
    MascotFieldElement macShare = new MascotFieldElement(5204, getModulus());
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithMacShare() {
    MascotFieldElement opened = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShare = new MascotFieldElement(7719, getModulus());
    MascotFieldElement macShare = new MascotFieldElement(4204, getModulus()); // tamper
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithOpened() {
    MascotFieldElement opened = new MascotFieldElement(41, getModulus()); // tamper
    MascotFieldElement macKeyShare = new MascotFieldElement(1, getModulus());
    MascotFieldElement macShare = new MascotFieldElement(42, getModulus());
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithMacKeyShare() {
    MascotFieldElement opened = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShare = new MascotFieldElement(3, getModulus()); // tamper
    MascotFieldElement macShare = new MascotFieldElement(42, getModulus());
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithMacShare() {
    MascotFieldElement opened = new MascotFieldElement(42, getModulus());
    MascotFieldElement macKeyShare = new MascotFieldElement(1, getModulus());
    MascotFieldElement macShare = new MascotFieldElement(34, getModulus()); // tamper
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

}
