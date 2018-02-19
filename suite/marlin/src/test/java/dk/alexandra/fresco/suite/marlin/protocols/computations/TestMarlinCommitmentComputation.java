package dk.alexandra.fresco.suite.marlin.protocols.computations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.marlin.AbstractMarlinTest;
import dk.alexandra.fresco.suite.marlin.datatypes.CompositeUInt128;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestMarlinCommitmentComputation extends AbstractMarlinTest {

  @Test
  public void testCommitmentTwo() {
    runTest(new TestTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testCommitmentThree() {
    runTest(new TestTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  private static class TestTest<ResourcePoolT extends MarlinResourcePool<CompositeUInt128>>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int noParties = conf.getResourcePool().getNoOfParties();
          List<byte[]> inputs = new ArrayList<>();
          Random random = new Random(42);
          for (int i = 1; i <= noParties; i++) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            inputs.add(bytes);
          }
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication =
              root -> new MarlinCommitmentComputation<>(
                  conf.getResourcePool(),
                  inputs.get(root.getBasicNumericContext().getMyId() - 1))
                  .buildComputation(root);
          List<byte[]> actual = runApplication(testApplication);
          assertEquals(inputs.size(), actual.size());
          for (int i = 0; i < actual.size(); i++) {
            assertArrayEquals(inputs.get(i), actual.get(i));
          }
        }
      };
    }
  }

}
