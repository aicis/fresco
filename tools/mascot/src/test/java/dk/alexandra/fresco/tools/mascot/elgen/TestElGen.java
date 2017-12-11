package dk.alexandra.fresco.tools.mascot.elgen;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
      FieldElement macKeyShare, List<List<FieldElement>> inputs) {
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
      List<FieldElement> inputs) {
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
  public void testTwoPartiesSingleInput() {
    // two parties run this
    initContexts(Arrays.asList(1, 2));
    // left party mac key share
    FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"), modulus, modBitLength);

    // right party mac key share
    FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

    // single right party input element
    FieldElement input = new FieldElement(7, modulus, modBitLength);
    List<FieldElement> inputs = Collections.singletonList(input);

    // define task each party will run
    Callable<List<AuthenticatedElement>> partyOneTask =
        () -> runInputter(contexts.get(1), macKeyShareOne, inputs);
    Callable<List<AuthenticatedElement>> partyTwoTask =
        () -> runOther(contexts.get(2), 1, macKeyShareTwo, inputs.size());

    // run tasks and get ordered list of results
    List<List<AuthenticatedElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // retrieve per-party results
    List<AuthenticatedElement> leftShares = results.get(0);
    AuthenticatedElement leftShare = leftShares.get(0);
    List<AuthenticatedElement> rightShares = results.get(1);
    AuthenticatedElement rightShare = rightShares.get(0);

    FieldElement expectedRecomb = new FieldElement(7, modulus, modBitLength);
    FieldElement expectedMacRecomb = new FieldElement(1608, modulus, modBitLength); // (keyShareA +
                                                                                    // keyShareB) *
                                                                                    // input
    // bit of a shortcut
    AuthenticatedElement expected =
        new AuthenticatedElement(expectedRecomb, expectedMacRecomb, modulus, modBitLength);
    AuthenticatedElement actual = leftShare.add(rightShare);

    assertEquals(expected, actual);
  }

  @Test
  public void testTwoPartiesSingleInputReverseRoles() {
    // two parties run this
    initContexts(Arrays.asList(1, 2));

    // left party mac key share
    FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"), modulus, modBitLength);

    // right party mac key share
    FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

    // single right party input element
    FieldElement input = new FieldElement(7, modulus, modBitLength);
    List<FieldElement> inputs = Collections.singletonList(input);

    // define task each party will run
    Callable<List<AuthenticatedElement>> partyOneTask =
        () -> runInputter(contexts.get(2), macKeyShareOne, inputs);
    Callable<List<AuthenticatedElement>> partyTwoTask =
        () -> runOther(contexts.get(1), 2, macKeyShareTwo, inputs.size());

    // run tasks and get ordered list of results
    List<List<AuthenticatedElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // retrieve per-party results
    List<AuthenticatedElement> leftShares = results.get(0);
    AuthenticatedElement leftShare = leftShares.get(0);
    List<AuthenticatedElement> rightShares = results.get(1);
    AuthenticatedElement rightShare = rightShares.get(0);

    FieldElement expectedRecomb = new FieldElement(7, modulus, modBitLength);
    FieldElement expectedMacRecomb = new FieldElement(1608, modulus, modBitLength);

    AuthenticatedElement expected =
        new AuthenticatedElement(expectedRecomb, expectedMacRecomb, modulus, modBitLength);
    AuthenticatedElement actual = leftShare.add(rightShare);

    assertEquals(expected, actual);
  }

  @Test
  public void testThreePartiesSingleInput() {
    // two parties run this
    initContexts(Arrays.asList(1, 2, 3));

    // party mac key shares
    FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"), modulus, modBitLength);

    FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

    FieldElement macKeyShareThree =
        new FieldElement(new BigInteger("40401"), modulus, modBitLength);

    // single right party input element
    FieldElement input = new FieldElement(7, modulus, modBitLength);
    List<FieldElement> inputs = Collections.singletonList(input);

    // define task each party will run
    Callable<List<AuthenticatedElement>> partyOneTask =
        () -> runInputter(contexts.get(1), macKeyShareOne, inputs);
    Callable<List<AuthenticatedElement>> partyTwoTask =
        () -> runOther(contexts.get(2), 1, macKeyShareTwo, 1);
    Callable<List<AuthenticatedElement>> partyThreeTask =
        () -> runOther(contexts.get(3), 1, macKeyShareThree, 1);

    // run tasks and get ordered list of results
    List<List<AuthenticatedElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask, partyThreeTask));
    List<AuthenticatedElement> actual = CollectionUtils.pairWiseSum(results);
    List<FieldElement> macKeyShares =
        Arrays.asList(macKeyShareOne, macKeyShareTwo, macKeyShareThree);
    List<AuthenticatedElement> expected =
        computeExpected(inputs, macKeyShares, modulus, modBitLength);

    assertEquals(expected, actual);
  }

  @Test
  public void testTwoPartiesMultInputs() {
    // two parties run this
    initContexts(Arrays.asList(1, 2));

    // party mac key shares
    FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"), modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

    // inputs
    int[] inputArr = {7, 444, 112, 11};
    List<FieldElement> inputs = MascotTestUtils.generateSingleRow(inputArr, modulus, modBitLength);

    // define task each party will run
    Callable<List<AuthenticatedElement>> partyOneTask =
        () -> runInputter(contexts.get(1), macKeyShareOne, inputs);
    Callable<List<AuthenticatedElement>> partyTwoTask =
        () -> runOther(contexts.get(2), 1, macKeyShareTwo, inputs.size());

    // run tasks and get ordered list of results
    List<List<AuthenticatedElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    List<AuthenticatedElement> actual = CollectionUtils.pairWiseSum(results);
    List<FieldElement> macKeyShares = Arrays.asList(macKeyShareOne, macKeyShareTwo);
    List<AuthenticatedElement> expected =
        computeExpected(inputs, macKeyShares, modulus, modBitLength);

    assertEquals(expected, actual);
  }

  @Test
  public void testTwoPartiesSingleInputMultRounds() {
    // two parties run this
    initContexts(Arrays.asList(1, 2));

    // party mac key shares
    FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"), modulus, modBitLength);
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
        () -> runInputterMultipleRounds(contexts.get(1), macKeyShareOne, inputs);
    Callable<List<AuthenticatedElement>> partyTwoTask =
        () -> runOtherMultipleRounds(contexts.get(2), 1, macKeyShareTwo, numInputsPerRound,
            numRounds);

    // run tasks and get ordered list of results
    List<List<AuthenticatedElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    List<AuthenticatedElement> actual = CollectionUtils.pairWiseSum(results);
    List<FieldElement> macKeyShares = Arrays.asList(macKeyShareOne, macKeyShareTwo);
    List<FieldElement> flatInputs = inputs.stream()
        .flatMap(l -> l.stream())
        .collect(Collectors.toList());
    List<AuthenticatedElement> expected =
        computeExpected(flatInputs, macKeyShares, modulus, modBitLength);

    assertEquals(expected, actual);
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

}