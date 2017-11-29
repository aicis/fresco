package dk.alexandra.fresco.tools.mascot.elgen;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.TestRuntime;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestElGen {

  private TestRuntime testRuntime;

  @Before
  public void initializeRuntime() {
    testRuntime = new TestRuntime();
  }

  @After
  public void teardownRuntime() {
    testRuntime.shutdown();
    testRuntime = null;
  }

  private List<SpdzElement> runInputter(MascotContext ctx, FieldElement macKeyShare,
      List<FieldElement> inputs) throws Exception {
    ElGen elGen = new ElGen(ctx, macKeyShare);
    elGen.initialize();
    return elGen.input(inputs);
  }

  private List<SpdzElement> runOther(MascotContext ctx, Integer inputterId,
      FieldElement macKeyShare, int numInputs) {
    ElGen elGen = new ElGen(ctx, macKeyShare);
    elGen.initialize();
    return elGen.input(inputterId, numInputs);
  }

  @Test
  public void testTwoPartiesSingleInput() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      
      // left party mac key share
      FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"),
          partyOneCtx.getModulus(), partyOneCtx.getkBitLength());

      // right party mac key share
      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"),
          partyOneCtx.getModulus(), partyOneCtx.getkBitLength());

      // single right party input element
      FieldElement input =
          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
      List<FieldElement> inputs = Arrays.asList(input);

      // define task each party will run
      Callable<List<SpdzElement>> partyOneTask =
          () -> runInputter(partyOneCtx, macKeyShareOne, inputs);
      Callable<List<SpdzElement>> partyTwoTask = () -> runOther(partyTwoCtx, 1, macKeyShareTwo, 1);

      // run tasks and get ordered list of results
      List<List<SpdzElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // retrieve per-party results
      List<SpdzElement> leftShares = results.get(0);
      SpdzElement leftShare = leftShares.get(0);
      List<SpdzElement> rightShares = results.get(1);
      SpdzElement rightShare = rightShares.get(0);
      
      BigInteger expectedRecomb = BigInteger.valueOf(7);
      BigInteger expectedMacRecomb = macKeyShareOne.add(macKeyShareTwo)
          .multiply(input)
          .toBigInteger();
      // bit of a shortcut
      SpdzElement expected = new SpdzElement(expectedRecomb, expectedMacRecomb, modulus);
      SpdzElement actual = leftShare.add(rightShare);

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testThreePartiesSingleInput() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2, 3);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);
      MascotContext partyThreeCtx = contexts.get(3);

      // party mac key shares
      FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"),
          partyOneCtx.getModulus(), partyOneCtx.getkBitLength());

      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"),
          partyOneCtx.getModulus(), partyOneCtx.getkBitLength());

      FieldElement macKeyShareThree = new FieldElement(new BigInteger("40401"),
          partyOneCtx.getModulus(), partyOneCtx.getkBitLength());


      // single right party input element
      FieldElement input =
          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
      List<FieldElement> inputs = Arrays.asList(input);

      // define task each party will run
      Callable<List<SpdzElement>> partyOneTask =
          () -> runInputter(partyOneCtx, macKeyShareOne, inputs);
      Callable<List<SpdzElement>> partyTwoTask = () -> runOther(partyTwoCtx, 1, macKeyShareTwo, 1);
      Callable<List<SpdzElement>> partyThreeTask =
          () -> runOther(partyThreeCtx, 1, macKeyShareThree, 1);


      // run tasks and get ordered list of results
      List<List<SpdzElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

}
