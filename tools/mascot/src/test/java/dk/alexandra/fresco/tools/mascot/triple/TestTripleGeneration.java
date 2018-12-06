package dk.alexandra.fresco.tools.mascot.triple;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotSecurityParameters;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Test;

public class TestTripleGeneration extends NetworkedTest {

  private FieldElementPrg getJointPrg(int prgSeedLength) {
    return new FieldElementPrgImpl(new StrictBitVector(prgSeedLength));
  }

  private List<MascotFieldElement> runSinglePartyMult(MascotTestContext ctx, MascotFieldElement macKeyShare,
      List<MascotFieldElement> leftFactorGroups, List<MascotFieldElement> rightFactors) {
    TripleGeneration tripleGen = new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(),
        getJointPrg(ctx.getPrgSeedLength()), macKeyShare);
    return tripleGen.multiply(leftFactorGroups, rightFactors);
  }

  private List<MultiplicationTriple> runSinglePartyTriple(MascotTestContext ctx, MascotFieldElement macKeyShare,
      int numTriples) {
    TripleGeneration tripleGen = new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(),
        getJointPrg(ctx.getPrgSeedLength()), macKeyShare);
    return tripleGen.triple(numTriples);
  }

  private List<MultiplicationTriple> runSinglePartyTripleRepeated(MascotTestContext ctx,
      MascotFieldElement macKeyShare, int numTriples, int numIterations) {
    TripleGeneration tripleGen = new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(),
        getJointPrg(ctx.getPrgSeedLength()), macKeyShare);
    List<MultiplicationTriple> triples = new ArrayList<>();
    for (int r = 0; r < numIterations; r++) {
      triples.addAll(tripleGen.triple(numTriples));
    }
    return triples;
  }

  @Test
  public void testTwoPartiesSingleMult() {
    // configure number of left factors
    initContexts(2, new MascotSecurityParameters(getDefaultParameters().getModBitLength(),
        getDefaultParameters().getLambdaSecurityParam(), getDefaultParameters().getPrgSeedLength(), 1));

    // left party mac key share
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());

    // right party mac key share
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // party one input
    int[] leftArrOne = {12};
    List<MascotFieldElement> leftFactorsOne =
        MascotTestUtils.generateSingleRow(leftArrOne, getModulus());
    int[] rightArrOne = {11};
    List<MascotFieldElement> rightFactorsOne =
        MascotTestUtils.generateSingleRow(rightArrOne, getModulus());

    // party two input
    int[] leftArrTwo = {123};
    List<MascotFieldElement> leftFactorsTwo =
        MascotTestUtils.generateSingleRow(leftArrTwo, getModulus());
    int[] rightArrTwo = {2222};
    List<MascotFieldElement> rightFactorsTwo =
        MascotTestUtils.generateSingleRow(rightArrTwo, getModulus());

    // define task each party will run
    Callable<List<MascotFieldElement>> partyOneTask =
        () -> runSinglePartyMult(contexts.get(1), macKeyShareOne, leftFactorsOne, rightFactorsOne);
    Callable<List<MascotFieldElement>> partyTwoTask =
        () -> runSinglePartyMult(contexts.get(2), macKeyShareTwo, leftFactorsTwo, rightFactorsTwo);

    List<List<MascotFieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    MascotFieldElement left = results.get(0).get(0);
    MascotFieldElement right = results.get(1).get(0);

    // (12 + 123) * (11 + 2222) % 65519
    MascotFieldElement expected = new MascotFieldElement(39379, getModulus());
    MascotFieldElement actual = left.add(right);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testTwoPartiesBatchedMult() {
    // two parties run this
    initContexts(2);

    // left party mac key share
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());

    // right party mac key share
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // party one input
    int[] leftArrOne = {1, 2, 3, 4, 5, 6};
    List<MascotFieldElement> leftFactorsOne =
        MascotTestUtils.generateSingleRow(leftArrOne, getModulus());
    int[] rightArrOne = {7, 8};
    List<MascotFieldElement> rightFactorsOne =
        MascotTestUtils.generateSingleRow(rightArrOne, getModulus());

    // party two input
    int[] leftArrTwo = {9, 10, 11, 12, 13, 14};
    List<MascotFieldElement> leftFactorsTwo =
        MascotTestUtils.generateSingleRow(leftArrTwo, getModulus());
    int[] rightArrTwo = {15, 16};
    List<MascotFieldElement> rightFactorsTwo =
        MascotTestUtils.generateSingleRow(rightArrTwo, getModulus());

    // define task each party will run
    Callable<List<MascotFieldElement>> partyOneTask =
        () -> runSinglePartyMult(contexts.get(1), macKeyShareOne, leftFactorsOne, rightFactorsOne);
    Callable<List<MascotFieldElement>> partyTwoTask =
        () -> runSinglePartyMult(contexts.get(2), macKeyShareTwo, leftFactorsTwo, rightFactorsTwo);

    List<List<MascotFieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    FieldElementUtils fieldElementUtils = new FieldElementUtils(getModulus());
    // for each input pair of factors the result is (a1 + a2 + ...) * (b1 + b2 + ...)
    List<MascotFieldElement> expectedLeftFactors =
        Addable.sumRows(Arrays.asList(leftFactorsOne, leftFactorsTwo));
    List<MascotFieldElement> expectedRightFactors = fieldElementUtils
        .stretch(Addable.sumRows(Arrays.asList(rightFactorsOne, rightFactorsTwo)), 3);

    List<MascotFieldElement> expected =
        fieldElementUtils.pairWiseMultiply(expectedLeftFactors, expectedRightFactors);

    // actual results, recombined
    List<MascotFieldElement> actual = Addable.sumRows(results);
    CustomAsserts.assertEquals(expected, actual);
  }

  private void testMultiplePartiesTriple(List<MascotFieldElement> macKeyShares, int numTriples) {
    // set up runtime environment and get contexts
    initContexts(macKeyShares.size());

    // define per party task with params
    List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
    for (int pid = 1; pid <= macKeyShares.size(); pid++) {
      MascotTestContext partyCtx = contexts.get(pid);
      MascotFieldElement macKeyShare = macKeyShares.get(pid - 1);
      Callable<List<MultiplicationTriple>> partyTask =
          () -> runSinglePartyTriple(partyCtx, macKeyShare, numTriples);
      tasks.add(partyTask);
    }

    List<List<MultiplicationTriple>> results = testRuntime.runPerPartyTasks(tasks);
    List<MultiplicationTriple> combined = Addable.sumRows(results);
    Assert.assertThat(combined, IsCollectionWithSize.hasSize(numTriples));
    for (MultiplicationTriple triple : combined) {
      CustomAsserts.assertTripleIsValid(triple, Addable.sum(macKeyShares));
    }
  }

  private void testMultiplePartiesTripleRepeated(List<MascotFieldElement> macKeyShares, int numTriples,
      int numIterations) {
    // set up runtime environment and get contexts
    initContexts(macKeyShares.size());

    // define per party task with params
    List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
    for (int pid = 1; pid <= macKeyShares.size(); pid++) {
      MascotTestContext partyCtx = contexts.get(pid);
      MascotFieldElement macKeyShare = macKeyShares.get(pid - 1);
      Callable<List<MultiplicationTriple>> partyTask =
          () -> runSinglePartyTripleRepeated(partyCtx, macKeyShare, numTriples, numIterations);
      tasks.add(partyTask);
    }

    List<List<MultiplicationTriple>> results = testRuntime.runPerPartyTasks(tasks);
    List<MultiplicationTriple> combined = Addable.sumRows(results);
    Assert.assertThat(combined, IsCollectionWithSize.hasSize(numTriples * numIterations));
    for (MultiplicationTriple triple : combined) {
      CustomAsserts.assertTripleIsValid(triple, Addable.sum(macKeyShares));
    }
  }

  @Test
  public void testTwoPartiesSingleTriple() {
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo), 1
    );
  }

  @Test
  public void testTwoPartiesMultipleTriple() {
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo), 10
    );
  }

  @Test
  public void testTwoPartiesMultipleTripleRepeated() {
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    int triplesPerBatch = 2;
    int numIterations = 5;
    testMultiplePartiesTripleRepeated(Arrays.asList(macKeyShareOne, macKeyShareTwo),
        triplesPerBatch, numIterations);
  }

  @Test
  public void testThreePartiesSingleTriple() {
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    MascotFieldElement macKeyShareThree = new MascotFieldElement(4444, getModulus());
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo, macKeyShareThree), 1
    );
  }

  @Test
  public void testThreePartiesMultTriple() {
    MascotFieldElement macKeyShareOne = new MascotFieldElement(11231, getModulus());
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(7719, getModulus());
    MascotFieldElement macKeyShareThree = new MascotFieldElement(4444, getModulus());
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo, macKeyShareThree), 3
    );
  }

}
