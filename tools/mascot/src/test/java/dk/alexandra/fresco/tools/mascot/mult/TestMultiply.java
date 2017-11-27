package dk.alexandra.fresco.tools.mascot.mult;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.TestRuntime;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestMultiply {

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

  private List<FieldElement> generateSingleRow(int[] factors, BigInteger modulus,
      int modBitLength) {
    return Arrays.stream(factors).mapToObj(val -> new FieldElement(val, modulus, modBitLength))
        .collect(Collectors.toList());
  }

  private List<List<FieldElement>> generateLeftInput(int[][] rows, BigInteger modulus,
      int modBitLength) {
    int numMults = rows.length;
    List<List<FieldElement>> input = new ArrayList<>(numMults);
    for (int[] leftFactorRow : rows) {
      List<FieldElement> row =
          Arrays.stream(leftFactorRow).mapToObj(val -> new FieldElement(val, modulus, modBitLength))
              .collect(Collectors.toList());
      input.add(row);
    }
    return input;
  }

  private List<List<FieldElement>> runLeftMult(MascotContext ctx, Integer otherId,
      List<List<FieldElement>> inputs) {
    MultiplyLeft mult = new MultiplyLeft(ctx, otherId, inputs.size());
    return mult.multiply(inputs);
  }

  private List<List<FieldElement>> runRightMult(MascotContext ctx, Integer otherId,
      List<FieldElement> inputs, int numLeftFactors) {
    MultiplyRight mult = new MultiplyRight(ctx, otherId, numLeftFactors);
    return mult.multiply(inputs);
  }

  @Test
  public void testSingleMult() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      // left parties input (can be multiple)
      FieldElement leftInput =
          new FieldElement(12, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      List<List<FieldElement>> leftInputs = Arrays.asList(Arrays.asList(leftInput));

      // single right party input element
      FieldElement rightInput =
          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
      List<FieldElement> rightInputs = Arrays.asList(rightInput);

      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runLeftMult(partyOneCtx, 2, leftInputs);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runRightMult(partyTwoCtx, 1, rightInputs, leftInputs.get(0).size());

      // run tasks and get ordered list of results
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<List<FieldElement>> leftResults = results.get(0);
      List<List<FieldElement>> rightResults = results.get(1);

      FieldElement left = leftResults.get(0).get(0);
      FieldElement right = rightResults.get(0).get(0);

      FieldElement actual = left.add(right);
      FieldElement expected = leftInput.multiply(rightInput);

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testMultiLeftFactorSingleMult() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      // left parties input (can be multiple)
      FieldElement leftInputOne =
          new FieldElement(70, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      FieldElement leftInputTwo =
          new FieldElement(12, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      FieldElement leftInputThree =
          new FieldElement(123, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      List<List<FieldElement>> leftInputs =
          Arrays.asList(Arrays.asList(leftInputOne, leftInputTwo, leftInputThree));

      // single right party input element
      FieldElement rightInput =
          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
      List<FieldElement> rightInputs = Arrays.asList(rightInput);

      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runLeftMult(partyOneCtx, 2, leftInputs);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runRightMult(partyTwoCtx, 1, rightInputs, leftInputs.get(0).size());

      // run tasks and get ordered list of results
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<FieldElement> leftResults = results.get(0).get(0);
      List<FieldElement> rightResults = results.get(1).get(0);

      List<FieldElement> expected = Arrays.asList(leftInputOne.multiply(rightInput),
          leftInputTwo.multiply(rightInput), leftInputThree.multiply(rightInput));
      List<FieldElement> actual =
          IntStream.range(0, 3).mapToObj(idx -> leftResults.get(idx).add(rightResults.get(idx)))
              .collect(Collectors.toList());
      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testBatchedMultSingleLeftFactor() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // left parties input
      int[][] leftRows = {{70}, {12}, {123}};
      List<List<FieldElement>> leftInputs = generateLeftInput(leftRows, modulus, modBitLength);
      // right party input
      int[] rightRows = {1, 2, 3};
      List<FieldElement> rightInputs = generateSingleRow(rightRows, modulus, modBitLength);

      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runLeftMult(partyOneCtx, 2, leftInputs);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runRightMult(partyTwoCtx, 1, rightInputs, leftInputs.get(0).size());

      // run tasks and get ordered list of results
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<List<FieldElement>> leftResults = results.get(0);
      List<FieldElement> leftResultFlat =
          leftResults.stream().flatMap(l -> l.stream()).collect(Collectors.toList());
      List<List<FieldElement>> rightResults = results.get(1);
      List<FieldElement> rightResultFlat =
          rightResults.stream().flatMap(l -> l.stream()).collect(Collectors.toList());

      // expected result is pair-wise products
      int[] prods = {70 * 1, 12 * 2, 123 * 3};
      List<FieldElement> expected = generateSingleRow(prods, modulus, modBitLength);
      List<FieldElement> actual = IntStream.range(0, 3)
          .mapToObj(idx -> leftResultFlat.get(idx).add(rightResultFlat.get(idx)))
          .collect(Collectors.toList());

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testBatchedMultMultipleLeftFactor() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // left parties input
      int[][] leftRows = {{70, 71, 72}, {12, 13, 14}, {123, 124, 125}};
      List<List<FieldElement>> leftInputs = generateLeftInput(leftRows, modulus, modBitLength);
      // right party input
      int[] rightRows = {1, 2, 3};
      List<FieldElement> rightInputs = generateSingleRow(rightRows, modulus, modBitLength);

      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runLeftMult(partyOneCtx, 2, leftInputs);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runRightMult(partyTwoCtx, 1, rightInputs, leftInputs.get(0).size());

      // run tasks and get ordered list of results
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<List<FieldElement>> leftResults = results.get(0);
      List<FieldElement> leftResultFlat =
          leftResults.stream().flatMap(l -> l.stream()).collect(Collectors.toList());
      List<List<FieldElement>> rightResults = results.get(1);
      List<FieldElement> rightResultFlat =
          rightResults.stream().flatMap(l -> l.stream()).collect(Collectors.toList());

      // TODO: should have assertions on results dimensions

      // expected result is pair-wise products
      int[] prods = {70 * 1, 71 * 1, 72 * 1, 12 * 2, 13 * 2, 14 * 2, 123 * 3, 124 * 3, 125 * 3};
      List<FieldElement> expected = generateSingleRow(prods, modulus, modBitLength);
      List<FieldElement> actual = IntStream.range(0, 9)
          .mapToObj(idx -> leftResultFlat.get(idx).add(rightResultFlat.get(idx)))
          .collect(Collectors.toList());

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testMultReversedRoles() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      // left parties input (can be multiple)
      FieldElement leftInputOne =
          new FieldElement(70, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      FieldElement leftInputTwo =
          new FieldElement(12, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      FieldElement leftInputThree =
          new FieldElement(123, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      List<List<FieldElement>> leftInputs =
          Arrays.asList(Arrays.asList(leftInputOne, leftInputTwo, leftInputThree));

      // single right party input element
      FieldElement rightInput =
          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
      List<FieldElement> rightInputs = Arrays.asList(rightInput);

      // roles are flipped
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runLeftMult(partyTwoCtx, 1, leftInputs);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runRightMult(partyOneCtx, 2, rightInputs, leftInputs.get(0).size());

      // run tasks and get ordered list of results
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<FieldElement> leftResults = results.get(0).get(0);
      List<FieldElement> rightResults = results.get(1).get(0);

      List<FieldElement> expected = Arrays.asList(leftInputOne.multiply(rightInput),
          leftInputTwo.multiply(rightInput), leftInputThree.multiply(rightInput));
      List<FieldElement> actual =
          IntStream.range(0, 3).mapToObj(idx -> leftResults.get(idx).add(rightResults.get(idx)))
              .collect(Collectors.toList());
      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

}
