package dk.alexandra.fresco.tools.mascot.maccheck;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestMacCheck extends NetworkedTest {

  // TODO as part of new testing framework, re-think how to test for exceptions
  private Pair<Boolean, Exception> runSinglePartyMacCheck(MascotContext ctx, FieldElement opened,
      FieldElement macKeyShare, FieldElement macShare) throws Exception {
    MacCheck macChecker = new MacCheck(ctx);
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

  public void maliciousPartyOne(FieldElement opened, FieldElement macKeyShare,
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

  public void maliciousPartyTwo(FieldElement opened, FieldElement macKeyShare,
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

  @Test
  public void testTwoPartiesValidMacCheck() {
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

}
