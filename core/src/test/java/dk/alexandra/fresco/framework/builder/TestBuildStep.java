package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestBuildStep extends AbstractDummyArithmeticTest {

  /**
   * 
   * Tests that whileLoop method performs correct number of iterations.
   *
   * @param <ResourcePoolT>
   */
  private class TestWhileLoop<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    protected final int numIterations;
    protected final List<Integer> expected;

    public TestWhileLoop(int numIterations, List<Integer> expected) {
      this.numIterations = numIterations;
      this.expected = expected;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<List<Integer>, ProtocolBuilderNumeric> testApplication = root -> root
              .seq(seq -> {
                // initiate loop
                return new IterationState(0, new ArrayList<>());
              }).whileLoop(
                  // iterate
                  (state) -> state.round < numIterations,
                  (seq, state) -> {
                    List<Integer> roundsSoFar = state.rounds;
                    roundsSoFar.add(state.round);
                    return new IterationState(state.round + 1, roundsSoFar);
                  }).seq((seq, state) -> () -> state.rounds);
          List<Integer> actual = runApplication(testApplication);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  private static final class IterationState implements DRes<IterationState> {

    private final int round;
    private final List<Integer> rounds;

    private IterationState(int round, List<Integer> rounds) {
      this.round = round;
      this.rounds = rounds;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

  @Test
  public void test_while_no_iteration() throws Exception {
    System.out.println("Testing no iteration");
    runTest(new TestWhileLoop<>(0, Collections.emptyList()), EvaluationStrategy.SEQUENTIAL,
        1);
  }

  @Test
  public void test_while_single_iteration() throws Exception {
    runTest(new TestWhileLoop<>(1, Collections.singletonList(0)), EvaluationStrategy.SEQUENTIAL,
        1);
  }

  @Test
  public void test_while_multiple_iterations() throws Exception {
    runTest(new TestWhileLoop<>(10, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
        EvaluationStrategy.SEQUENTIAL, 1);
  }
}
