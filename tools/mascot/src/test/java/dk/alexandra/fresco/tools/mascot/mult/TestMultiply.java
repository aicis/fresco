package dk.alexandra.fresco.tools.mascot.mult;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.arithm.ArithmeticCollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class TestMultiply extends NetworkedTest {

  private List<FieldElement> runLeftMult(MascotTestContext ctx, Integer otherId,
      List<FieldElement> inputs) {
    MultiplyLeft mult =
        new MultiplyLeft(ctx.getResourcePool(), ctx.getNetwork(), otherId, inputs.size());
    return mult.multiply(inputs);
  }

  private List<FieldElement> runRightMult(MascotTestContext ctx, Integer otherId,
      List<FieldElement> inputs, int numLeftFactors) {
    MultiplyRight mult =
        new MultiplyRight(ctx.getResourcePool(), ctx.getNetwork(), otherId, numLeftFactors);
    return mult.multiply(inputs);
  }

  @Test
  public void testSingleMult() {
    initContexts(Arrays.asList(1, 2));

    // left parties input (can be multiple)
    FieldElement leftInput = new FieldElement(12, modulus, modBitLength);
    List<FieldElement> leftInputs = Arrays.asList(leftInput);

    // single right party input element
    FieldElement rightInput = new FieldElement(7, modulus, modBitLength);
    List<FieldElement> rightInputs = Arrays.asList(rightInput);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask = () -> runLeftMult(contexts.get(1), 2, leftInputs);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runRightMult(contexts.get(2), 1, rightInputs, 1);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // get per party results
    List<FieldElement> leftResults = results.get(0);
    List<FieldElement> rightResults = results.get(1);

    FieldElement left = leftResults.get(0);
    FieldElement right = rightResults.get(0);

    FieldElement actual = left.add(right);
    FieldElement expected = leftInput.multiply(rightInput);

    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testMultiLeftFactorSingleMult() {
    initContexts(Arrays.asList(1, 2));

    // left parties input (can be multiple)
    FieldElement leftInputOne = new FieldElement(70, modulus, modBitLength);
    FieldElement leftInputTwo = new FieldElement(12, modulus, modBitLength);
    FieldElement leftInputThree = new FieldElement(123, modulus, modBitLength);
    List<FieldElement> leftInputs = Arrays.asList(leftInputOne, leftInputTwo, leftInputThree);

    // single right party input element
    FieldElement rightInput = new FieldElement(7, modulus, modBitLength);
    List<FieldElement> rightInputs = Arrays.asList(rightInput);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask = () -> runLeftMult(contexts.get(1), 2, leftInputs);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runRightMult(contexts.get(2), 1, rightInputs, 3);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // get per party results
    List<FieldElement> leftResults = results.get(0);
    List<FieldElement> rightResults = results.get(1);

    List<FieldElement> expected = Arrays.asList(leftInputOne.multiply(rightInput),
        leftInputTwo.multiply(rightInput), leftInputThree.multiply(rightInput));
    List<FieldElement> actual =
        IntStream.range(0, 3).mapToObj(idx -> leftResults.get(idx).add(rightResults.get(idx)))
            .collect(Collectors.toList());
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testBatchedMultSingleLeftFactor() {
    // two parties run this
    initContexts(Arrays.asList(1, 2));
    // left parties input
    int[] leftRows = {70, 12, 123};
    List<FieldElement> leftInputs =
        MascotTestUtils.generateSingleRow(leftRows, modulus, modBitLength);
    // right party input
    int[] rightRows = {1, 2, 3};
    List<FieldElement> rightInputs =
        MascotTestUtils.generateSingleRow(rightRows, modulus, modBitLength);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask = () -> runLeftMult(contexts.get(1), 2, leftInputs);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runRightMult(contexts.get(2), 1, rightInputs, 1);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // get per party results
    List<FieldElement> leftResults = results.get(0);
    List<FieldElement> rightResults = results.get(1);

    // expected result is pair-wise products
    int[] prods = {70 * 1, 12 * 2, 123 * 3};
    List<FieldElement> expected = MascotTestUtils.generateSingleRow(prods, modulus, modBitLength);
    List<FieldElement> actual =
        IntStream.range(0, 3).mapToObj(idx -> leftResults.get(idx).add(rightResults.get(idx)))
            .collect(Collectors.toList());

    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testBatchedMultMultipleLeftFactor() {
    // two parties run this
    initContexts(Arrays.asList(1, 2));

    // left parties input
    int[] leftRows = {70, 71, 72, 12, 13, 14, 123, 124, 125};
    List<FieldElement> leftInputs =
        MascotTestUtils.generateSingleRow(leftRows, modulus, modBitLength);
    // right party input
    int[] rightRows = {1, 2, 3};
    List<FieldElement> rightInputs =
        MascotTestUtils.generateSingleRow(rightRows, modulus, modBitLength);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask = () -> runLeftMult(contexts.get(1), 2, leftInputs);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runRightMult(contexts.get(2), 1, rightInputs, 3);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // get per party results
    List<FieldElement> leftResults = results.get(0);
    List<FieldElement> rightResults = results.get(1);

    int[] prods = {70 * 1, 71 * 1, 72 * 1, 12 * 2, 13 * 2, 14 * 2, 123 * 3, 124 * 3, 125 * 3};
    List<FieldElement> expected = MascotTestUtils.generateSingleRow(prods, modulus, modBitLength);
    assertEquals(expected.size(), leftResults.size());
    assertEquals(expected.size(), rightResults.size());
    List<FieldElement> actual = IntStream.range(0, expected.size())
        .mapToObj(idx -> leftResults.get(idx).add(rightResults.get(idx)))
        .collect(Collectors.toList());

    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testMultReversedRoles() {
    // two parties run this
    initContexts(Arrays.asList(1, 2));

    // left parties input (can be multiple)
    FieldElement leftInputOne = new FieldElement(70, modulus, modBitLength);
    FieldElement leftInputTwo = new FieldElement(12, modulus, modBitLength);
    FieldElement leftInputThree = new FieldElement(123, modulus, modBitLength);
    List<FieldElement> leftInputs = Arrays.asList(leftInputOne, leftInputTwo, leftInputThree);

    // single right party input element
    FieldElement rightInput = new FieldElement(7, modulus, modBitLength);
    List<FieldElement> rightInputs = Arrays.asList(rightInput);

    // roles are flipped
    Callable<List<FieldElement>> partyOneTask = () -> runLeftMult(contexts.get(2), 1, leftInputs);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runRightMult(contexts.get(1), 2, rightInputs, 3);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // get per party results
    List<FieldElement> leftResults = results.get(0);
    List<FieldElement> rightResults = results.get(1);

    List<FieldElement> expected = Arrays.asList(leftInputOne.multiply(rightInput),
        leftInputTwo.multiply(rightInput), leftInputThree.multiply(rightInput));
    List<FieldElement> actual =
        IntStream.range(0, 3).mapToObj(idx -> leftResults.get(idx).add(rightResults.get(idx)))
            .collect(Collectors.toList());
    CustomAsserts.assertEquals(expected, actual);
  }

  void testManyMults(int numMults, int lambdaSecurityParam) {
    // two parties run this
    this.lambdaSecurityParam = lambdaSecurityParam; // change lambda security
    initContexts(Arrays.asList(1, 2));
    // left parties input
    List<FieldElement> leftInputs = new ArrayList<>(numMults);
    // right party input
    List<FieldElement> rightInputs = new ArrayList<>(numMults);
    for (int i = 1; i <= numMults; i++) {
      leftInputs.add(new FieldElement(i, modulus, modBitLength));
      rightInputs.add(new FieldElement(i, modulus, modBitLength));
    }

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask = () -> runLeftMult(contexts.get(1), 2, leftInputs);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runRightMult(contexts.get(2), 1, rightInputs, 1);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    List<FieldElement> actual = new ArithmeticCollectionUtils<FieldElement>().sumRows(results);
    List<FieldElement> expected =
        new FieldElementUtils(modulus, modBitLength).pairWiseMultiply(leftInputs, rightInputs);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testManyMults() {
    testManyMults(2, lambdaSecurityParam);
  }

  @Test
  public void testManyMultsLambdaNotEqualModBitLength() {
    testManyMults(2, 24);
  }

}
