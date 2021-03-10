package dk.alexandra.fresco.tools.bitTriples.triple;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTriplesTestContext;
import dk.alexandra.fresco.tools.bitTriples.NetworkedTest;
import dk.alexandra.fresco.tools.bitTriples.elements.AuthenticatedElement;
import dk.alexandra.fresco.tools.bitTriples.elements.MultiplicationTriple;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrgImpl;
import dk.alexandra.fresco.tools.bitTriples.triple.TripleGeneration.AuthenticatedCandidate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Test;

public class TripleGenerationTest extends NetworkedTest {

  private List<MultiplicationTriple> runTripleGeneration(BitTriplesTestContext ctx) {
    BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(ctx.getPrgSeedLength()));
    TripleGeneration tripleGeneration =
        new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(), 16, jointSampler);
    return tripleGeneration.triple(1024);
  }

  @Test
  public void tripleTwoParties() {
    initContexts(2);
    List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
    tasks.add(() -> runTripleGeneration(contexts.get(1)));
    tasks.add(() -> runTripleGeneration(contexts.get(2)));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void tripleThreeParties() {
    initContexts(3);
    List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
    tasks.add(() -> runTripleGeneration(contexts.get(1)));
    tasks.add(() -> runTripleGeneration(contexts.get(2)));
    tasks.add(() -> runTripleGeneration(contexts.get(3)));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void tripleFourParties() {
    initContexts(4);
    List<Callable<List<MultiplicationTriple>>> tasks = new ArrayList<>();
    tasks.add(() -> runTripleGeneration(contexts.get(1)));
    tasks.add(() -> runTripleGeneration(contexts.get(2)));
    tasks.add(() -> runTripleGeneration(contexts.get(3)));
    tasks.add(() -> runTripleGeneration(contexts.get(4)));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void getBucketSize() {
    Assert.assertEquals(5, TripleGeneration.getBucketSize(20, 20));
    Assert.assertEquals(4, TripleGeneration.getBucketSize(2000, 20));
    Assert.assertEquals(3, TripleGeneration.getBucketSize(20000, 20));

    Assert.assertEquals(8, TripleGeneration.getBucketSize(20, 41));
    Assert.assertEquals(6, TripleGeneration.getBucketSize(2000, 41));
    Assert.assertEquals(5, TripleGeneration.getBucketSize(20000, 41));
  }

  @Test(expected = IllegalArgumentException.class)
  public void toAuthenticatedElement() {
    TripleGeneration.toAuthenticatedElement(new ArrayList<>(), new StrictBitVector(8));
  }

  @Test(expected = IllegalArgumentException.class)
  public void toAuthenticatedCandidate() {
    List<AuthenticatedElement> notEmpty = new ArrayList<>();
    notEmpty.add(mock(AuthenticatedElement.class));
    TripleGeneration.toAuthenticatedCandidate(new ArrayList<>(), new ArrayList<>(), notEmpty);
  }

  private Integer runCheckMultiplicationPredicate(
      BitTriplesTestContext ctx, List<AuthenticatedCandidate> candidates) {
    BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(ctx.getPrgSeedLength()));
    TripleGeneration tripleGeneration =
        new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(), 16, jointSampler);
    tripleGeneration.checkMultiplicationPredicate(candidates, new ArrayList<>());
    return 1;
  }

  @Test
  public void testCheckMultiplicationPredicateFails() {
    initContexts(2);
    AuthenticatedCandidate cand1 =
        new AuthenticatedCandidate(
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)));
    AuthenticatedCandidate cand2 =
        new AuthenticatedCandidate(
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)));

    List<AuthenticatedCandidate> arg1 = new ArrayList<>();
    List<AuthenticatedCandidate> arg2 = new ArrayList<>();
    arg1.add(cand1);
    arg1.add(cand2);

    List<Callable<Integer>> tasks = new ArrayList<>();
    tasks.add(() -> runCheckMultiplicationPredicate(contexts.get(1), arg1));
    tasks.add(() -> runCheckMultiplicationPredicate(contexts.get(2), arg2));

    try {
      testRuntime.runPerPartyTasks(tasks);
    } catch (Exception e) {
      Assert.assertTrue(e.getCause().getCause() instanceof MaliciousException);
    }
  }

  private Integer runCheckR(
      BitTriplesTestContext ctx, AuthenticatedCandidate head, AuthenticatedCandidate candidate) {
    BytePrgImpl jointSampler = new BytePrgImpl(new StrictBitVector(ctx.getPrgSeedLength()));
    TripleGeneration tripleGeneration =
        new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(), 16, jointSampler);
    tripleGeneration.checkR(head, candidate, new ArrayList<>());
    return 1;
  }

  @Test
  public void checkR() {
    initContexts(2);
    AuthenticatedCandidate headA =
        new AuthenticatedCandidate(
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)));
    AuthenticatedCandidate candidateA =
        new AuthenticatedCandidate(
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)));

    AuthenticatedCandidate headB =
        new AuthenticatedCandidate(
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)));
    AuthenticatedCandidate candidateB =
        new AuthenticatedCandidate(
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)));

    List<Callable<Integer>> tasks = new ArrayList<>();
    tasks.add(() -> runCheckR(contexts.get(1), headA, candidateA));
    tasks.add(() -> runCheckR(contexts.get(2), headB, candidateB));

    testRuntime.runPerPartyTasks(tasks);
  }

  @Test
  public void checkRThrowsCorrectException() {
    initContexts(2);
    AuthenticatedCandidate headA =
        new AuthenticatedCandidate(
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)));
    AuthenticatedCandidate candidateA =
        new AuthenticatedCandidate(
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)));

    AuthenticatedCandidate headB =
        new AuthenticatedCandidate(
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)),
            new AuthenticatedElement(false, new StrictBitVector(8)));
    AuthenticatedCandidate candidateB =
        new AuthenticatedCandidate(
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)),
            new AuthenticatedElement(true, new StrictBitVector(8)));

    List<Callable<Integer>> tasks = new ArrayList<>();
    tasks.add(() -> runCheckR(contexts.get(1), headA, candidateA));
    tasks.add(() -> runCheckR(contexts.get(2), headB, candidateB));

    try {
      testRuntime.runPerPartyTasks(tasks);
    } catch (Exception e) {
      Assert.assertTrue(e.getCause().getCause() instanceof MaliciousException);
    }
  }
}
