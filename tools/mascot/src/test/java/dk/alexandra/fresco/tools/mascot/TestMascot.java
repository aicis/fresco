package dk.alexandra.fresco.tools.mascot;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

public class TestMascot extends NetworkedTest {

  private final FieldElement macKeyShareOne = new FieldElement(11231, modulus, modBitLength);
  private final FieldElement macKeyShareTwo = new FieldElement(7719, modulus, modBitLength);

  private List<MultTriple> runTripleGen(MascotTestContext ctx, FieldElement macKeyShare,
      int numTriples) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.getTriples(numTriples);
  }

  private List<AuthenticatedElement> runRandomElementGen(MascotTestContext ctx,
      FieldElement macKeyShare, int numElements) {
    Mascot mascot = new Mascot(ctx.getResourcePool(), ctx.getNetwork(), macKeyShare);
    return mascot.getRandomElements(numElements);
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
    List<MultTriple> combined = CollectionUtils.pairwiseSum(results);
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
    tasks.add(() -> runRandomElementGen(contexts.get(1), macKeyShareOne, 1));
    tasks.add(() -> runRandomElementGen(contexts.get(2), macKeyShareTwo, 1));

    List<List<AuthenticatedElement>> results = testRuntime.runPerPartyTasks(tasks);
    assertEquals(results.get(0).size(), 1);
    assertEquals(results.get(1).size(), 1);

    // TODO assert that elements are different?
  }

  @Test
  public void testInput() {
    // set up runtime environment and get contexts
    initContexts(Arrays.asList(1, 2));

    FieldElement input = new FieldElement(12345, modulus, modBitLength);

    // define per party task with params
    List<Callable<List<AuthenticatedElement>>> tasks = new ArrayList<>();
    tasks.add(() -> runInputter(contexts.get(1), macKeyShareOne, Collections.singletonList(input)));
    tasks.add(() -> runNonInputter(contexts.get(2), macKeyShareTwo, 1, 1));

    List<List<AuthenticatedElement>> results = testRuntime.runPerPartyTasks(tasks);
    assertEquals(results.get(0).size(), 1);
    assertEquals(results.get(1).size(), 1);
    List<AuthenticatedElement> combined = CollectionUtils.pairwiseSum(results);
    FieldElement actualRecombinedValue = combined.get(0).getShare();
    FieldElement actualRecombinedMac = combined.get(0).getMac();
    CustomAsserts.assertEquals(input, actualRecombinedValue);
    FieldElement expectedMac = input.multiply(macKeyShareOne.add(macKeyShareTwo));
    CustomAsserts.assertEquals(expectedMac, actualRecombinedMac);
  }

}
