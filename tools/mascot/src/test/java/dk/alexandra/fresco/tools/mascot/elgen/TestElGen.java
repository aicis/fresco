package dk.alexandra.fresco.tools.mascot.elgen;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestElGen extends NetworkedTest {

  private List<AuthenticatedElement> runInputterMultipleRounds(MascotContext ctx,
      FieldElement macKeyShare, List<List<FieldElement>> inputs) throws Exception {
    ElGen elGen = new ElGen(ctx, macKeyShare);
    elGen.initialize();
    int perRoundInputs = inputs.get(0)
        .size();
    List<AuthenticatedElement> elements = new ArrayList<>(perRoundInputs * inputs.size());
    for (List<FieldElement> roundInput : inputs) {
      List<AuthenticatedElement> thisRoundResult = elGen.input(roundInput);
      elements.addAll(thisRoundResult);
    }
    return elements;
  }

  private List<AuthenticatedElement> runOtherMultipleRounds(MascotContext ctx, Integer inputterId,
      FieldElement macKeyShare, int numInputsPerRound, int numRounds) {
    ElGen elGen = new ElGen(ctx, macKeyShare);
    elGen.initialize();
    List<AuthenticatedElement> elements = new ArrayList<>(numInputsPerRound * numRounds);
    for (int r = 0; r < numRounds; r++) {
      List<AuthenticatedElement> thisRoundResult = elGen.input(inputterId, numInputsPerRound);
      elements.addAll(thisRoundResult);
    }
    return elements;
  }

  private List<AuthenticatedElement> runInputter(MascotContext ctx, FieldElement macKeyShare,
      List<FieldElement> inputs) throws Exception {
    ElGen elGen = new ElGen(ctx, macKeyShare);
    elGen.initialize();
    return elGen.input(inputs);
  }

  private List<AuthenticatedElement> runOther(MascotContext ctx, Integer inputterId,
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
      int modBitLength = partyOneCtx.getkBitLength();

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
      Callable<List<AuthenticatedElement>> partyOneTask =
          () -> runInputter(partyOneCtx, macKeyShareOne, inputs);
      Callable<List<AuthenticatedElement>> partyTwoTask =
          () -> runOther(partyTwoCtx, 1, macKeyShareTwo, inputs.size());

      // run tasks and get ordered list of results
      List<List<AuthenticatedElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // retrieve per-party results
      List<AuthenticatedElement> leftShares = results.get(0);
      AuthenticatedElement leftShare = leftShares.get(0);
      List<AuthenticatedElement> rightShares = results.get(1);
      AuthenticatedElement rightShare = rightShares.get(0);

      FieldElement expectedRecomb = new FieldElement(7, modulus, modBitLength);
      FieldElement expectedMacRecomb = new FieldElement(1608, modulus, modBitLength); // (keyShareA
                                                                                      // +
                                                                                      // keyShareB)
                                                                                      // * input
      // bit of a shortcut
      AuthenticatedElement expected =
          new AuthenticatedElement(expectedRecomb, expectedMacRecomb, modulus, modBitLength);
      AuthenticatedElement actual = leftShare.add(rightShare);

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testTwoPartiesSingleInputReverseRoles() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

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
      Callable<List<AuthenticatedElement>> partyOneTask =
          () -> runInputter(partyTwoCtx, macKeyShareOne, inputs);
      Callable<List<AuthenticatedElement>> partyTwoTask =
          () -> runOther(partyOneCtx, 2, macKeyShareTwo, inputs.size());

      // run tasks and get ordered list of results
      List<List<AuthenticatedElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // retrieve per-party results
      List<AuthenticatedElement> leftShares = results.get(0);
      AuthenticatedElement leftShare = leftShares.get(0);
      List<AuthenticatedElement> rightShares = results.get(1);
      AuthenticatedElement rightShare = rightShares.get(0);

      FieldElement expectedRecomb = new FieldElement(7, modulus, modBitLength);
      FieldElement expectedMacRecomb = new FieldElement(1608, modulus, modBitLength); // (keyShareA
                                                                                      // +
                                                                                      // keyShareB)
                                                                                      // * input
      // bit of a shortcut
      AuthenticatedElement expected =
          new AuthenticatedElement(expectedRecomb, expectedMacRecomb, modulus, modBitLength);
      AuthenticatedElement actual = leftShare.add(rightShare);

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

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // party mac key shares
      FieldElement macKeyShareOne =
          new FieldElement(new BigInteger("11231"), modulus, modBitLength);

      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

      FieldElement macKeyShareThree =
          new FieldElement(new BigInteger("40401"), modulus, modBitLength);


      // single right party input element
      FieldElement input = new FieldElement(7, modulus, modBitLength);
      List<FieldElement> inputs = Arrays.asList(input);

      // define task each party will run
      Callable<List<AuthenticatedElement>> partyOneTask =
          () -> runInputter(partyOneCtx, macKeyShareOne, inputs);
      Callable<List<AuthenticatedElement>> partyTwoTask =
          () -> runOther(partyTwoCtx, 1, macKeyShareTwo, 1);
      Callable<List<AuthenticatedElement>> partyThreeTask =
          () -> runOther(partyThreeCtx, 1, macKeyShareThree, 1);

      // run tasks and get ordered list of results
      List<List<AuthenticatedElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));
      List<AuthenticatedElement> actual = computeActual(results);
      List<FieldElement> macKeyShares =
          Arrays.asList(macKeyShareOne, macKeyShareTwo, macKeyShareThree);
      List<AuthenticatedElement> expected =
          computeExpected(inputs, macKeyShares, modulus, modBitLength);

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testTwoPartiesMultInputs() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // party mac key shares
      FieldElement macKeyShareOne =
          new FieldElement(new BigInteger("11231"), modulus, modBitLength);
      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

      // inputs
      int[] inputArr = {7, 444, 112, 11};
      List<FieldElement> inputs =
          MascotTestUtils.generateSingleRow(inputArr, modulus, modBitLength);

      // define task each party will run
      Callable<List<AuthenticatedElement>> partyOneTask =
          () -> runInputter(partyOneCtx, macKeyShareOne, inputs);
      Callable<List<AuthenticatedElement>> partyTwoTask =
          () -> runOther(partyTwoCtx, 1, macKeyShareTwo, inputs.size());

      // run tasks and get ordered list of results
      List<List<AuthenticatedElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      List<AuthenticatedElement> actual = computeActual(results);
      List<FieldElement> macKeyShares = Arrays.asList(macKeyShareOne, macKeyShareTwo);
      List<AuthenticatedElement> expected =
          computeExpected(inputs, macKeyShares, modulus, modBitLength);

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testTwoPartiesSingleInputMultRounds() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // party mac key shares
      FieldElement macKeyShareOne =
          new FieldElement(new BigInteger("11231"), modulus, modBitLength);
      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

      // inputs
      int[][] inputArr = {{70}, {12}, {123}};
      List<List<FieldElement>> inputs =
          MascotTestUtils.generateLeftInput(inputArr, modulus, modBitLength);
      int numInputsPerRound = inputs.get(0)
          .size();
      int numRounds = inputs.size();

      // define task each party will run
      Callable<List<AuthenticatedElement>> partyOneTask =
          () -> runInputterMultipleRounds(partyOneCtx, macKeyShareOne, inputs);
      Callable<List<AuthenticatedElement>> partyTwoTask = () -> runOtherMultipleRounds(partyTwoCtx,
          1, macKeyShareTwo, numInputsPerRound, numRounds);

      // run tasks and get ordered list of results
      List<List<AuthenticatedElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      List<AuthenticatedElement> actual = computeActual(results);
      List<FieldElement> macKeyShares = Arrays.asList(macKeyShareOne, macKeyShareTwo);
      List<FieldElement> flatInputs = inputs.stream()
          .flatMap(l -> l.stream())
          .collect(Collectors.toList());
      List<AuthenticatedElement> expected =
          computeExpected(flatInputs, macKeyShares, modulus, modBitLength);

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  // util methods

  private List<AuthenticatedElement> computeExpected(List<FieldElement> inputs,
      List<FieldElement> macKeyShares, BigInteger modulus, int modBitLength) {
    FieldElement macKey = CollectionUtils.sum(macKeyShares);
    Stream<AuthenticatedElement> expected = inputs.stream()
        .map(fe -> {
          FieldElement mac = fe.multiply(macKey);
          return new AuthenticatedElement(fe, mac, modulus, modBitLength);
        });
    return expected.collect(Collectors.toList());
  }

  private AuthenticatedElement sum(List<AuthenticatedElement> summands) {
    return summands.stream()
        .reduce((l, r) -> l.add(r))
        .get();
  }

  private List<AuthenticatedElement> computeActual(
      List<List<AuthenticatedElement>> sharesPerParty) {
    // TODO debatable...
    List<List<AuthenticatedElement>> perValue = CollectionUtils.transpose(sharesPerParty);
    List<AuthenticatedElement> actual = perValue.stream()
        .map(shares -> sum(shares))
        .collect(Collectors.toList());
    return actual;
  }

}