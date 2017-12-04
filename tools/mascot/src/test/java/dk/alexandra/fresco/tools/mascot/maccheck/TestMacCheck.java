package dk.alexandra.fresco.tools.mascot.maccheck;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;

import dk.alexandra.fresco.framework.MPCException;
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
    } catch (MPCException e) {
      exception = e;
      thrown = true;
    }
    return new Pair<>(thrown, exception);
  }

  @Test
  public void testTwoPartiesValidMacCheck() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

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
          () -> runSinglePartyMacCheck(partyOneCtx, openedOne, macKeyShareOne, macShareOne);
      Callable<Pair<Boolean, Exception>> partyTwoTask =
          () -> runSinglePartyMacCheck(partyTwoCtx, openedTwo, macKeyShareTwo, macShareTwo);

      List<Pair<Boolean, Exception>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // the above mac check fails since 4444 + 5204 = (11231 + 7719) * 42
      for (Pair<Boolean, Exception> res : results) {
        assertEquals(res.getFirst(), false);
      }
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testTwoPartiesInvalidMacCheck() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // left party inputs
      FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
      FieldElement openedOne = new FieldElement(42, modulus, modBitLength);
      FieldElement macShareOne = new FieldElement(4442, modulus, modBitLength);

      // right party inputs
      FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
      FieldElement openedTwo = new FieldElement(42, modulus, modBitLength);
      FieldElement macShareTwo = new FieldElement(5204, modulus, modBitLength);


      // define task each party will run
      Callable<Pair<Boolean, Exception>> partyOneTask =
          () -> runSinglePartyMacCheck(partyOneCtx, openedOne, macKeyShareOne, macShareOne);
      Callable<Pair<Boolean, Exception>> partyTwoTask =
          () -> runSinglePartyMacCheck(partyTwoCtx, openedTwo, macKeyShareTwo, macShareTwo);

      List<Pair<Boolean, Exception>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // the above mac check fails since 4442 + 5204 != (11231 + 7719) * 42
      for (Pair<Boolean, Exception> res : results) {
        boolean didThrow = res.getFirst();
        Exception exception = res.getSecond();
        assertEquals(didThrow, true);
        assertEquals(exception.getClass(), MPCException.class);
        assertEquals(exception.getMessage(), "Mac check failed!");
      }
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

}
