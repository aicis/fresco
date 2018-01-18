package dk.alexandra.fresco.tools.mascot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.tools.mascot.arithm.ArithmeticCollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

public class TestMascot extends NetworkedTest {

  private final FieldElement macKeyShareOne = new FieldElement(11231, modulus);
  private final FieldElement macKeyShareTwo = new FieldElement(7719, modulus);

  private List<MultTriple> runTripleGen(MascotTestContext ctx, FieldElement macKeyShare,
      int numTriples) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.getTriples(numTriples);
  }

  private List<AuthenticatedElement> runRandomElementGeneration(MascotTestContext ctx,
      FieldElement macKeyShare, int numElements) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.getRandomElements(numElements);
  }

  private List<AuthenticatedElement> runRandomBitGeneration(MascotTestContext ctx,
      FieldElement macKeyShare, int numBits) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.getRandomBits(numBits);
  }

  private List<AuthenticatedElement> runInputter(MascotTestContext ctx, FieldElement macKeyShare,
      List<FieldElement> inputs) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.input(inputs);
  }

  private List<AuthenticatedElement> runNonInputter(MascotTestContext ctx, FieldElement macKeyShare,
      Integer inputterId, int numInputs) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.input(inputterId, numInputs);
  }

  private List<InputMask> runInputMask(MascotTestContext ctx, Integer inputterId, int numMasks,
      FieldElement macKeyShare) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.getInputMasks(inputterId, numMasks);
  }

  @Test
  public void testTriple() {
    // set up runtime environment and get contexts
    initContexts(Arrays.asList(1, 2));

    // define per party task with params
    List<Callable<List<MultTriple>>> tasks = new ArrayList<>();
    tasks.add(() -> runTripleGen(contexts.get(1), macKeyShareOne, 1));
    tasks.add(() -> runTripleGen(contexts.get(2), macKeyShareTwo, 1));

    List<List<MultTriple>> results = testRuntime.runPerPartyTasks(tasks);
    assertEquals(results.get(0).size(), 1);
    assertEquals(results.get(1).size(), 1);
    List<MultTriple> combined = new ArithmeticCollectionUtils<MultTriple>().sumRows(results);
    for (MultTriple triple : combined) {
      CustomAsserts.assertTripleIsValid(triple, macKeyShareOne.add(macKeyShareTwo));
    }
  }

  @Test
  public void testRandomGen() {
    // set up runtime environment and get contexts
    initContexts(Arrays.asList(1, 2));

    // define per party task with params
    List<Callable<List<AuthenticatedElement>>> tasks = new ArrayList<>();
    tasks.add(() -> runRandomElementGeneration(contexts.get(1), macKeyShareOne, 1));
    tasks.add(() -> runRandomElementGeneration(contexts.get(2), macKeyShareTwo, 1));

    List<List<AuthenticatedElement>> results = testRuntime.runPerPartyTasks(tasks);
    assertEquals(results.get(0).size(), 1);
    assertEquals(results.get(1).size(), 1);
    AuthenticatedElement recombined = new ArithmeticCollectionUtils<AuthenticatedElement>()
        .sumRows(results).get(0);
    // sanity check
    assertFalse(recombined.getShare().isZero());
  }

  @Test
  public void testRandomBitGen() {
    // set up runtime environment and get contexts
    initContexts(Arrays.asList(1, 2));

    // define per party task with params
    List<Callable<List<AuthenticatedElement>>> tasks = new ArrayList<>();
    tasks.add(() -> runRandomBitGeneration(contexts.get(1), macKeyShareOne, 1));
    tasks.add(() -> runRandomBitGeneration(contexts.get(2), macKeyShareTwo, 1));

    List<List<AuthenticatedElement>> results = testRuntime.runPerPartyTasks(tasks);
    assertEquals(results.get(0).size(), 1);
    assertEquals(results.get(1).size(), 1);

    AuthenticatedElement bit = results.get(0).get(0).add(results.get(1).get(0));
    FieldElement actualBit = bit.getShare();
    CustomAsserts.assertFieldElementIsBit(actualBit);
  }

  @Test
  public void testInputMask() {
    // set up runtime environment and get contexts
    initContexts(Arrays.asList(1, 2));
    int numMasks = 16;

    // define per party task with params
    List<Callable<List<InputMask>>> tasks = new ArrayList<>();
    tasks.add(() -> runInputMask(contexts.get(1), 1, numMasks, macKeyShareOne));
    tasks.add(() -> runInputMask(contexts.get(2), 1, numMasks, macKeyShareTwo));

    List<List<InputMask>> results = testRuntime.runPerPartyTasks(tasks);
    List<InputMask> leftMasks = results.get(0);
    List<InputMask> rightMasks = results.get(1);
    assertEquals(results.get(0).size(), numMasks);
    assertEquals(results.get(1).size(), numMasks);

    FieldElement macKey = macKeyShareOne.add(macKeyShareTwo);
    for (int i = 0; i < leftMasks.size(); i++) {
      InputMask left = leftMasks.get(i);
      InputMask right = rightMasks.get(i);
      assertTrue(right.getOpenValue() == null);
      AuthenticatedElement recombined = left.getMaskShare().add(right.getMaskShare());
      AuthenticatedElement expected = new AuthenticatedElement(left.getOpenValue(),
          left.getOpenValue().multiply(macKey), modulus);
      CustomAsserts.assertEquals(expected, recombined);
    }

  }

  @Test
  public void testInput() {
    // set up runtime environment and get contexts
    initContexts(Arrays.asList(1, 2));

    FieldElement input = new FieldElement(12345, modulus);

    // define per party task with params
    List<Callable<List<AuthenticatedElement>>> tasks = new ArrayList<>();
    tasks.add(() -> runInputter(contexts.get(1), macKeyShareOne, Collections.singletonList(input)));
    tasks.add(() -> runNonInputter(contexts.get(2), macKeyShareTwo, 1, 1));

    List<List<AuthenticatedElement>> results = testRuntime.runPerPartyTasks(tasks);
    assertEquals(results.get(0).size(), 1);
    assertEquals(results.get(1).size(), 1);
    List<AuthenticatedElement> combined =
        new ArithmeticCollectionUtils<AuthenticatedElement>().sumRows(results);
    FieldElement actualRecombinedValue = combined.get(0).getShare();
    FieldElement actualRecombinedMac = combined.get(0).getMac();
    CustomAsserts.assertEquals(input, actualRecombinedValue);
    FieldElement expectedMac = input.multiply(macKeyShareOne.add(macKeyShareTwo));
    CustomAsserts.assertEquals(expectedMac, actualRecombinedMac);
  }

}
