package dk.alexandra.fresco.tools.mascot.triple;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.utils.BatchArithmetic;

public class TestTripleGen extends NetworkedTest {

  private List<List<FieldElement>> runSinglePartyMult(MascotContext ctx, FieldElement macKeyShare,
      List<List<FieldElement>> leftFactorGroups, List<FieldElement> rightFactors) throws Exception {
    int numLeftFactors = leftFactorGroups.get(0)
        .size();
    TripleGen tripleGen = new TripleGen(ctx, macKeyShare, numLeftFactors);
    tripleGen.initialize();
    List<List<FieldElement>> productGroups = tripleGen.multiply(leftFactorGroups, rightFactors);
    return productGroups;
  }

  private List<MultTriple> runSinglePartyTriple(MascotContext ctx, FieldElement macKeyShare,
      int numLeftFactors, int numTriples) throws Exception {
    TripleGen tripleGen = new TripleGen(ctx, macKeyShare, numLeftFactors);
    tripleGen.initialize();
    List<MultTriple> triples = tripleGen.triple(numTriples);
    return triples;
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
      int[][] leftArrOne = {{12}};
      List<List<FieldElement>> leftFactorsOne =
          MascotTestUtils.generateLeftInput(leftArrOne, modulus, modBitLength);
      int[] rightArrOne = {11};
      List<FieldElement> rightFactorsOne =
          MascotTestUtils.generateSingleRow(rightArrOne, modulus, modBitLength);

      // party two input
      int[][] leftArrTwo = {{123}};
      List<List<FieldElement>> leftFactorsTwo =
          MascotTestUtils.generateLeftInput(leftArrTwo, modulus, modBitLength);
      int[] rightArrTwo = {2222};
      List<FieldElement> rightFactorsTwo =
          MascotTestUtils.generateSingleRow(rightArrTwo, modulus, modBitLength);

      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runSinglePartyMult(partyOneCtx, macKeyShareOne, leftFactorsOne, rightFactorsOne);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runSinglePartyMult(partyTwoCtx, macKeyShareTwo, leftFactorsTwo, rightFactorsTwo);

      // wow much list amaze
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
      FieldElement left = results.get(0)
          .get(0)
          .get(0);
      FieldElement right = results.get(1)
          .get(0)
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
      int[][] leftArrOne = {{1, 2, 3}, {4, 5, 6}};
      List<List<FieldElement>> leftFactorsOne =
          MascotTestUtils.generateLeftInput(leftArrOne, modulus, modBitLength);
      int[] rightArrOne = {7, 8};
      List<FieldElement> rightFactorsOne =
          MascotTestUtils.generateSingleRow(rightArrOne, modulus, modBitLength);

      // party two input
      int[][] leftArrTwo = {{9, 10, 11}, {12, 13, 14}};
      List<List<FieldElement>> leftFactorsTwo =
          MascotTestUtils.generateLeftInput(leftArrTwo, modulus, modBitLength);
      int[] rightArrTwo = {15, 16};
      List<FieldElement> rightFactorsTwo =
          MascotTestUtils.generateSingleRow(rightArrTwo, modulus, modBitLength);

      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runSinglePartyMult(partyOneCtx, macKeyShareOne, leftFactorsOne, rightFactorsOne);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runSinglePartyMult(partyTwoCtx, macKeyShareTwo, leftFactorsTwo, rightFactorsTwo);

      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // we expected that for each input pair of factors the result is (a1 + a2 + ...) * (b1 + b2 +
      // ...)
      List<List<FieldElement>> expectedLeftFactors =
          BatchArithmetic.pairWiseAdd(Arrays.asList(leftFactorsOne, leftFactorsTwo));
      List<FieldElement> expectedRightFactors =
          combineRight(Arrays.asList(rightFactorsOne, rightFactorsTwo));
      List<List<FieldElement>> expected =
          BatchArithmetic.pairWiseMultiply(expectedLeftFactors, expectedRightFactors);

      // actual results, recombined
      List<List<FieldElement>> actual = BatchArithmetic.pairWiseAdd(results);
      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testTwoPartiesSingleTriple() throws Exception {
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

      // define task each party will run
      Callable<List<MultTriple>> partyOneTask =
          () -> runSinglePartyTriple(partyOneCtx, macKeyShareOne, 3, 1);
      Callable<List<MultTriple>> partyTwoTask =
          () -> runSinglePartyTriple(partyTwoCtx, macKeyShareTwo, 3, 1);

      List<List<MultTriple>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
      List<MultTriple> combined = BatchArithmetic.pairWiseAddRows(results);
      for (MultTriple multTriple : combined) {
        FieldElement leftValue = multTriple.getLeft()
            .getShare();
        FieldElement rightValue = multTriple.getRight()
            .getShare();
        FieldElement productValue = multTriple.getProduct()
            .getShare();
        assertEquals(leftValue.multiply(rightValue), productValue);
      }
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  // helpers

  private List<FieldElement> combineRight(List<List<FieldElement>> rightFactors) {
    return rightFactors.stream()
        .reduce((top, bottom) -> {
          return BatchArithmetic.addGroups(top, bottom);
        })
        .get();
  }

}
