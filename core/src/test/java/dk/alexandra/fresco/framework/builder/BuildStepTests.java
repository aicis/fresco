package dk.alexandra.fresco.framework.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;

public class BuildStepTests extends AbstractDummyArithmeticTest {

  /**
   * 
   * Tests that whileLoop method performs correct number of iterations.
   *
   * @param <ResourcePoolT>
   */
  private class TestWhileLoop<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    protected final int numIterations;
    protected final List<Integer> expected;

    public TestWhileLoop(int numIterations, List<Integer> expected) {
      this.numIterations = numIterations;
      this.expected = expected;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {
        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<List<Integer>, SequentialNumericBuilder> testApplication = root -> {
            List<Integer> rounds = new ArrayList<>();
            return root.seq(seq -> {
              // initiate loop
              return new IterationState(0);
            }).whileLoop(
                // iterate
                (state) -> state.round < numIterations, (state, seq) -> {
                  rounds.add(state.round);
                  return new IterationState(state.round + 1);
                }).seq((state, seq) -> {
                  return () -> rounds;
                });
          };
          List<Integer> actual = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  private static final class IterationState implements Computation<IterationState> {

    private final int round;

    private IterationState(int round) {
      this.round = round;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

  @Test
  public void test_while_no_iteration() throws Exception {
    runTest(new TestWhileLoop<>(0, Arrays.asList()), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, 1);
  }

  @Test
  public void test_while_single_iteration() throws Exception {
    runTest(new TestWhileLoop<>(1, Arrays.asList(0)), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, 1);
  }

  @Test
  public void test_while_multiple_iterations() throws Exception {
    runTest(new TestWhileLoop<>(10, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 1);
  }
}
