package dk.alexandra.fresco.tools.mascot.triple;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementCollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;

public class TestTripleGen extends NetworkedTest {

  private List<FieldElement> runSinglePartyMult(MascotContext ctx, FieldElement macKeyShare,
      List<FieldElement> leftFactorGroups, List<FieldElement> rightFactors) throws Exception {
    int numLeftFactors = leftFactorGroups.size() / rightFactors.size();
    TripleGen tripleGen = new TripleGen(ctx, macKeyShare, numLeftFactors);
    tripleGen.initialize();
    List<FieldElement> productGroups = tripleGen.multiply(leftFactorGroups, rightFactors);
    return productGroups;
  }

  private List<MultTriple> runSinglePartyTriple(MascotContext ctx, FieldElement macKeyShare,
      int numLeftFactors, int numTriples) throws Exception {
    TripleGen tripleGen = new TripleGen(ctx, macKeyShare, numLeftFactors);
    tripleGen.initialize();
    List<MultTriple> triples = tripleGen.triple(numTriples);
    return triples;
  }

  private List<MultTriple> runSinglePartyTripleRepeated(MascotContext ctx, FieldElement macKeyShare,
      int numLeftFactors, int numTriples, int numIterations) throws Exception {
    TripleGen tripleGen = new TripleGen(ctx, macKeyShare, numLeftFactors);
    tripleGen.initialize();
    List<MultTriple> triples = new ArrayList<>();
    for (int r = 0; r < numIterations; r++) {
      triples.addAll(tripleGen.triple(numTriples));
    }
    return triples;
  }

  private void checkTriple(MultTriple triple, FieldElement macKey) {
    AuthenticatedElement left = triple.getLeft();
    AuthenticatedElement right = triple.getRight();
    AuthenticatedElement product = triple.getProduct();

    // check values
    FieldElement leftValue = left.getShare();
    FieldElement rightValue = right.getShare();
    FieldElement productValue = product.getShare();
    assertEquals(leftValue.multiply(rightValue), productValue);

    // check macs
    FieldElement leftMac = left.getMac();
    FieldElement rightMac = right.getMac();
    FieldElement productMac = product.getMac();
    assertEquals(leftMac.multiply(rightMac), productMac.multiply(macKey));
  }

  @Test
  public void testTwoPartiesSingleMult() throws Exception {
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
      FieldElement macKeyShareOne =
          new FieldElement(new BigInteger("11231"), modulus, modBitLength);

      // right party mac key share
      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

      // party one input
      int[] leftArrOne = {12};
      List<FieldElement> leftFactorsOne =
          MascotTestUtils.generateSingleRow(leftArrOne, modulus, modBitLength);
      int[] rightArrOne = {11};
      List<FieldElement> rightFactorsOne =
          MascotTestUtils.generateSingleRow(rightArrOne, modulus, modBitLength);

      // party two input
      int[] leftArrTwo = {123};
      List<FieldElement> leftFactorsTwo =
          MascotTestUtils.generateSingleRow(leftArrTwo, modulus, modBitLength);
      int[] rightArrTwo = {2222};
      List<FieldElement> rightFactorsTwo =
          MascotTestUtils.generateSingleRow(rightArrTwo, modulus, modBitLength);

      // define task each party will run
      Callable<List<FieldElement>> partyOneTask =
          () -> runSinglePartyMult(partyOneCtx, macKeyShareOne, leftFactorsOne, rightFactorsOne);
      Callable<List<FieldElement>> partyTwoTask =
          () -> runSinglePartyMult(partyTwoCtx, macKeyShareTwo, leftFactorsTwo, rightFactorsTwo);

      List<List<FieldElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
      FieldElement left = results.get(0)
          .get(0);
      FieldElement right = results.get(1)
          .get(0);

      // (12 + 123) * (11 + 2222) % 65521
      FieldElement expected = new FieldElement(39371, modulus, modBitLength);
      FieldElement actual = left.add(right);
      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testTwoPartiesBatchedMult() throws Exception {
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
      FieldElement macKeyShareOne =
          new FieldElement(new BigInteger("11231"), modulus, modBitLength);

      // right party mac key share
      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

      // party one input
      int[] leftArrOne = {1, 2, 3, 4, 5, 6};
      List<FieldElement> leftFactorsOne =
          MascotTestUtils.generateSingleRow(leftArrOne, modulus, modBitLength);
      int[] rightArrOne = {7, 8};
      List<FieldElement> rightFactorsOne =
          MascotTestUtils.generateSingleRow(rightArrOne, modulus, modBitLength);

      // party two input
      int[] leftArrTwo = {9, 10, 11, 12, 13, 14};
      List<FieldElement> leftFactorsTwo =
          MascotTestUtils.generateSingleRow(leftArrTwo, modulus, modBitLength);
      int[] rightArrTwo = {15, 16};
      List<FieldElement> rightFactorsTwo =
          MascotTestUtils.generateSingleRow(rightArrTwo, modulus, modBitLength);

      // define task each party will run
      Callable<List<FieldElement>> partyOneTask =
          () -> runSinglePartyMult(partyOneCtx, macKeyShareOne, leftFactorsOne, rightFactorsOne);
      Callable<List<FieldElement>> partyTwoTask =
          () -> runSinglePartyMult(partyTwoCtx, macKeyShareTwo, leftFactorsTwo, rightFactorsTwo);

      List<List<FieldElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // for each input pair of factors the result is (a1 + a2 + ...) * (b1 + b2 + ...)
      List<FieldElement> expectedLeftFactors =
          CollectionUtils.pairWiseSum(Arrays.asList(leftFactorsOne, leftFactorsTwo));
      List<FieldElement> expectedRightFactors = FieldElementCollectionUtils
          .stretch(CollectionUtils.pairWiseSum(Arrays.asList(rightFactorsOne, rightFactorsTwo)), 3);

      List<FieldElement> expected =
          FieldElementCollectionUtils.pairWiseMultiply(expectedLeftFactors, expectedRightFactors);

      // actual results, recombined
      List<FieldElement> actual = CollectionUtils.pairWiseSum(results);
      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  public void testMultiplePartiesTriple(List<FieldElement> macKeyShares, int numTriples,
      BigInteger modulus, int modBitLength) throws Exception {
    try {
      // define parties, one per mac key shares
      List<Integer> partyIds = new ArrayList<>(macKeyShares.size());
      for (int pid = 1; pid <= macKeyShares.size(); pid++) {
        partyIds.add(pid);
      }

      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      // define per party task with params
      List<Callable<List<MultTriple>>> tasks = new ArrayList<>();
      for (int pid = 1; pid <= macKeyShares.size(); pid++) {
        MascotContext partyCtx = contexts.get(pid);
        FieldElement macKeyShare = macKeyShares.get(pid - 1);
        Callable<List<MultTriple>> partyTask =
            () -> runSinglePartyTriple(partyCtx, macKeyShare, 3, numTriples);
        tasks.add(partyTask);
      }

      List<List<MultTriple>> results = testRuntime.runPerPartyTasks(tasks);
      List<MultTriple> combined = CollectionUtils.pairWiseSum(results);
      for (MultTriple triple : combined) {
        checkTriple(triple, CollectionUtils.sum(macKeyShares));
      }
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  public void testMultiplePartiesTripleRepeated(List<FieldElement> macKeyShares, int numTriples,
      int numIterations, BigInteger modulus, int modBitLength) throws Exception {
    try {
      // define parties, one per mac key shares
      List<Integer> partyIds = new ArrayList<>(macKeyShares.size());
      for (int pid = 1; pid <= macKeyShares.size(); pid++) {
        partyIds.add(pid);
      }

      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      // define per party task with params
      List<Callable<List<MultTriple>>> tasks = new ArrayList<>();
      for (int pid = 1; pid <= macKeyShares.size(); pid++) {
        MascotContext partyCtx = contexts.get(pid);
        FieldElement macKeyShare = macKeyShares.get(pid - 1);
        Callable<List<MultTriple>> partyTask =
            () -> runSinglePartyTripleRepeated(partyCtx, macKeyShare, 3, numTriples, numIterations);
        tasks.add(partyTask);
      }

      List<List<MultTriple>> results = testRuntime.runPerPartyTasks(tasks);
      List<MultTriple> combined = CollectionUtils.pairWiseSum(results);
      for (MultTriple triple : combined) {
        checkTriple(triple, CollectionUtils.sum(macKeyShares));
      }
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testTwoPartiesSingleTriple() throws Exception {
    BigInteger modulus = new BigInteger("65521");
    int modBitLength = 16;
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo), 1, modulus,
        modBitLength);
  }

  @Test
  public void testTwoPartiesMultipleTriple() throws Exception {
    BigInteger modulus = new BigInteger("65521");
    int modBitLength = 16;
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo), 10, modulus,
        modBitLength);
  }

  @Test
  public void testTwoPartiesMultipleTripleRepeated() throws Exception {
    BigInteger modulus = new BigInteger("65521");
    int modBitLength = 16;
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    int triplesPerBatch = 2;
    int numIterations = 5;
    testMultiplePartiesTripleRepeated(Arrays.asList(macKeyShareOne, macKeyShareTwo),
        triplesPerBatch, numIterations, modulus, modBitLength);
  }

  @Test
  public void testThreePartiesSingleTriple() throws Exception {
    BigInteger modulus = new BigInteger("65521");
    int modBitLength = 16;
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    FieldElement macKeyShareThree = new FieldElement(4444, modulus, modBitLength);
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo, macKeyShareThree), 1,
        modulus, modBitLength);
  }

  @Test
  public void testThreePartiesMultTriple() throws Exception {
    BigInteger modulus = new BigInteger("65521");
    int modBitLength = 16;
    FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
    FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);
    FieldElement macKeyShareThree = new FieldElement(4444, modulus, modBitLength);
    testMultiplePartiesTriple(Arrays.asList(macKeyShareOne, macKeyShareTwo, macKeyShareThree), 3,
        modulus, modBitLength);
  }

}
