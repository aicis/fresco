package dk.alexandra.fresco.tools.mascot.maccheck;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
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
    initContexts(Arrays.asList(1, 2));

    // the "honest" party inputs consistent values
    FieldElement openedCorrect = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShareCorrect = new FieldElement(7719, modulus, modBitLength);
    FieldElement macShareCorrect = new FieldElement(5204, modulus, modBitLength);

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
    initContexts(Arrays.asList(1, 2));

    // the "honest" party inputs consistent values
    FieldElement openedCorrect = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShareCorrect = new FieldElement(11231, modulus, modBitLength);
    FieldElement macShareCorrect = new FieldElement(4444, modulus, modBitLength);

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
    initContexts(Arrays.asList(1, 2, 3));

    FieldElement openedOne = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement macShareOne = new FieldElement(4444, modulus, modBitLength);

    FieldElement openedTwo = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    FieldElement macShareTwo = new FieldElement(5204, modulus, modBitLength);

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
    modulus = new BigInteger("65521");
    // two parties run this
    initContexts(Arrays.asList(1, 2));

    // left party inputs
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement openedOne = new FieldElement(42, modulus, modBitLength);
    FieldElement macShareOne = new FieldElement(4444, modulus, modBitLength);

    // right party inputs
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    FieldElement openedTwo = new FieldElement(42, modulus, modBitLength);
    FieldElement macShareTwo = new FieldElement(5204, modulus, modBitLength);

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyMacCheck(contexts.get(1), openedOne, macKeyShareOne, macShareOne);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyMacCheck(contexts.get(2), openedTwo, macKeyShareTwo, macShareTwo);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // the above mac check fails since 4444 + 5204 = (11231 + 7719) * 42
    for (Pair<Boolean, Exception> res : results) {
      assertEquals(res.getFirst(), false);
    }
  }

  @Test
  public void testThreePartyValidMacCheck() {
    modulus = new BigInteger("65521");
    initContexts(Arrays.asList(1, 2, 3));

    FieldElement openedOne = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement macShareOne = new FieldElement(4444, modulus, modBitLength);

    FieldElement openedTwo = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    FieldElement macShareTwo = new FieldElement(5204, modulus, modBitLength);

    FieldElement openedThree = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShareThree = new FieldElement(1, modulus, modBitLength);
    FieldElement macShareThree = new FieldElement(42, modulus, modBitLength);

    // define task each party will run
    Callable<Pair<Boolean, Exception>> partyOneTask =
        () -> runSinglePartyMacCheck(contexts.get(1), openedOne, macKeyShareOne, macShareOne);
    Callable<Pair<Boolean, Exception>> partyTwoTask =
        () -> runSinglePartyMacCheck(contexts.get(2), openedTwo, macKeyShareTwo, macShareTwo);
    Callable<Pair<Boolean, Exception>> partyThreeTask =
        () -> runSinglePartyMacCheck(contexts.get(3), openedThree, macKeyShareThree, macShareThree);

    List<Pair<Boolean, Exception>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));

    // the above mac check fails since 4444 + 5204 + 42 = (11231 + 7719 + 1) * 42
    for (Pair<Boolean, Exception> res : results) {
      assertEquals(false, res.getFirst());
    }
  }

  @Test
  public void testPartyOneTampersWithOpened() {
    FieldElement opened = new FieldElement(41, modulus, modBitLength); // tamper
    FieldElement macKeyShare = new FieldElement(11231, modulus, modBitLength);
    FieldElement macShare = new FieldElement(4444, modulus, modBitLength);
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyOneTampersWithMacKeyShare() {
    FieldElement opened = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShare = new FieldElement(11031, modulus, modBitLength); // tamper
    FieldElement macShare = new FieldElement(4444, modulus, modBitLength);
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyOneTampersWithMacShare() {
    FieldElement opened = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShare = new FieldElement(11231, modulus, modBitLength);
    FieldElement macShare = new FieldElement(4442, modulus, modBitLength); // tamper
    maliciousPartyOne(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithOpened() {
    FieldElement opened = new FieldElement(41, modulus, modBitLength); // tamper
    FieldElement macKeyShare = new FieldElement(7719, modulus, modBitLength);
    FieldElement macShare = new FieldElement(5204, modulus, modBitLength);
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithMacKeyShare() {
    FieldElement opened = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShare = new FieldElement(77, modulus, modBitLength); // tamper
    FieldElement macShare = new FieldElement(5204, modulus, modBitLength);
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyTwoTampersWithMacShare() {
    FieldElement opened = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShare = new FieldElement(7719, modulus, modBitLength);
    FieldElement macShare = new FieldElement(4204, modulus, modBitLength); // tamper
    maliciousPartyTwo(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithOpened() {
    FieldElement opened = new FieldElement(41, modulus, modBitLength); // tamper
    FieldElement macKeyShare = new FieldElement(1, modulus, modBitLength);
    FieldElement macShare = new FieldElement(42, modulus, modBitLength);
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithMacKeyShare() {
    FieldElement opened = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShare = new FieldElement(3, modulus, modBitLength); // tamper
    FieldElement macShare = new FieldElement(42, modulus, modBitLength);
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

  @Test
  public void testPartyThreeTampersWithMacShare() {
    FieldElement opened = new FieldElement(42, modulus, modBitLength);
    FieldElement macKeyShare = new FieldElement(1, modulus, modBitLength);
    FieldElement macShare = new FieldElement(34, modulus, modBitLength); // tamper
    maliciousPartyThree(opened, macKeyShare, macShare);
  }

}
