package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class TestElementGeneration extends NetworkedTest {

  private List<AuthenticatedElement> runInputterMultipleRounds(MascotTestContext ctx,
      MascotFieldElement macKeyShare, List<List<MascotFieldElement>> inputs) {
    FieldElementPrg jointSampler =
        new FieldElementPrgImpl(new StrictBitVector(new byte[]{1, 2, 3}));
    ElementGeneration elGen =
        new ElementGeneration(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare, jointSampler);
    int perRoundInputs = inputs.get(0).size();
    List<AuthenticatedElement> elements = new ArrayList<>(perRoundInputs * inputs.size());
    for (List<MascotFieldElement> roundInput : inputs) {
      List<AuthenticatedElement> thisRoundResult = elGen.input(roundInput);
      elements.addAll(thisRoundResult);
    }
    return elements;
  }

  private List<AuthenticatedElement> runOtherMultipleRounds(MascotTestContext ctx,
      Integer inputterId, MascotFieldElement macKeyShare, int numInputsPerRound, int numRounds) {
    FieldElementPrg jointSampler =
        new FieldElementPrgImpl(new StrictBitVector(new byte[]{1, 2, 3}));
    ElementGeneration elGen =
        new ElementGeneration(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare, jointSampler);
    List<AuthenticatedElement> elements = new ArrayList<>(numInputsPerRound * numRounds);
    for (int r = 0; r < numRounds; r++) {
      List<AuthenticatedElement> thisRoundResult = elGen.input(inputterId, numInputsPerRound);
      elements.addAll(thisRoundResult);
    }
    return elements;
  }

  private List<AuthenticatedElement> runInputter(MascotTestContext ctx, MascotFieldElement macKeyShare,
      List<MascotFieldElement> inputs) {
    FieldElementPrg jointSampler =
        new FieldElementPrgImpl(new StrictBitVector(new byte[]{1, 2, 3}));
    ElementGeneration elGen =
        new ElementGeneration(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare, jointSampler);
    return elGen.input(inputs);
  }

  private List<AuthenticatedElement> runOther(MascotTestContext ctx, Integer inputterId,
      MascotFieldElement macKeyShare, int numInputs) {
    FieldElementPrg jointSampler =
        new FieldElementPrgImpl(new StrictBitVector(new byte[]{1, 2, 3}));
    ElementGeneration elGen =
        new ElementGeneration(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare, jointSampler);
    return elGen.input(inputterId, numInputs);
  }

  @Test
  public void testTwoPartiesSingleInput() {
    // two parties run this
    initContexts(2);
    // left party mac key share
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());

    // right party mac key share
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // single right party input element
    MascotFieldElement input = new MascotFieldElement(7, getModulus());
    List<MascotFieldElement> inputs = Collections.singletonList(input);

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

    MascotFieldElement expectedRecomb = new MascotFieldElement(7, getModulus());
    MascotFieldElement expectedMacRecomb = new MascotFieldElement(
        input.multiply(macKeyShareOne.add(macKeyShareTwo)).getValue(), getModulus());

    AuthenticatedElement expected =
        new AuthenticatedElement(expectedRecomb, expectedMacRecomb, getModulus());
    AuthenticatedElement actual = leftShare.add(rightShare);

    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testTwoPartiesSingleInputReverseRoles() {
    // two parties run this
    initContexts(2);

    // left party mac key share
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());

    // right party mac key share
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // single right party input element
    MascotFieldElement input = new MascotFieldElement(7, getModulus());
    List<MascotFieldElement> inputs = Collections.singletonList(input);

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

    MascotFieldElement expectedRecomb = new MascotFieldElement(7, getModulus());
    MascotFieldElement expectedMacRecomb = new MascotFieldElement(
        input.multiply(macKeyShareOne.add(macKeyShareTwo)).getValue(), getModulus());

    AuthenticatedElement expected =
        new AuthenticatedElement(expectedRecomb, expectedMacRecomb, getModulus());
    AuthenticatedElement actual = leftShare.add(rightShare);

    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testThreePartiesSingleInput() {
    // two parties run this
    initContexts(3);

    // party mac key shares
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());

    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    MascotFieldElement macKeyShareThree =
        new MascotFieldElement(new BigInteger("40401"), getModulus());

    // single right party input element
    MascotFieldElement input = new MascotFieldElement(7, getModulus());
    List<MascotFieldElement> inputs = Collections.singletonList(input);

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
    List<AuthenticatedElement> actual = Addable.sumRows(results);
    List<MascotFieldElement> macKeyShares =
        Arrays.asList(macKeyShareOne, macKeyShareTwo, macKeyShareThree);
    List<AuthenticatedElement> expected =
        computeExpected(inputs, macKeyShares, getModulus());

    CustomAsserts.assertEqualsAuth(expected, actual);
  }

  @Test
  public void testTwoPartiesMultInputs() {
    // two parties run this
    initContexts(2);

    // party mac key shares
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // inputs
    int[] inputArr = {7, 444, 112, 11};
    List<MascotFieldElement> inputs = MascotTestUtils.generateSingleRow(inputArr, getModulus());

    // define task each party will run
    Callable<List<AuthenticatedElement>> partyOneTask =
        () -> runInputter(contexts.get(1), macKeyShareOne, inputs);
    Callable<List<AuthenticatedElement>> partyTwoTask =
        () -> runOther(contexts.get(2), 1, macKeyShareTwo, inputs.size());

    // run tasks and get ordered list of results
    List<List<AuthenticatedElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    List<AuthenticatedElement> actual = Addable.sumRows(results);
    List<MascotFieldElement> macKeyShares = Arrays.asList(macKeyShareOne, macKeyShareTwo);
    List<AuthenticatedElement> expected =
        computeExpected(inputs, macKeyShares, getModulus());

    CustomAsserts.assertEqualsAuth(expected, actual);
  }

  @Test
  public void testTwoPartiesSingleInputMultRounds() {
    // two parties run this
    initContexts(2);

    // party mac key shares
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // inputs
    int[][] inputArr = {{70}, {12}, {123}};
    List<List<MascotFieldElement>> inputs =
        MascotTestUtils.generateMatrix(inputArr, getModulus());
    int numInputsPerRound = inputs.get(0).size();
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

    List<AuthenticatedElement> actual = Addable.sumRows(results);
    List<MascotFieldElement> macKeyShares = Arrays.asList(macKeyShareOne, macKeyShareTwo);
    List<MascotFieldElement> flatInputs =
        inputs.stream().flatMap(Collection::stream).collect(Collectors.toList());
    List<AuthenticatedElement> expected =
        computeExpected(flatInputs, macKeyShares, getModulus());

    CustomAsserts.assertEqualsAuth(expected, actual);
  }

  // util methods

  private List<AuthenticatedElement> computeExpected(List<MascotFieldElement> inputs,
      List<MascotFieldElement> macKeyShares, BigInteger modulus) {
    MascotFieldElement macKey = Addable.sum(macKeyShares);
    Stream<AuthenticatedElement> expected = inputs.stream().map(fe -> {
      MascotFieldElement mac = fe.multiply(macKey);
      return new AuthenticatedElement(fe, mac, modulus);
    });
    return expected.collect(Collectors.toList());
  }

}
