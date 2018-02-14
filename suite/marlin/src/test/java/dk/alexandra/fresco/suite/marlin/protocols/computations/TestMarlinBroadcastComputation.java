package dk.alexandra.fresco.suite.marlin.protocols.computations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.marlin.AbstractMarlinTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestMarlinBroadcastComputation extends AbstractMarlinTest {

  @Test
  public void testBroadcast() {
    runTest(new TestTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  private static class TestTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication = root -> {
            byte[] inputOne = new byte[]{0x01, 0x42};
            byte[] inputTwo = new byte[]{0x02, 0x07};
            if (root.getBasicNumericContext().getMyId() == 1) {
              return new MarlinBroadcastComputation<>(inputOne).buildComputation(root);
            } else {
              return new MarlinBroadcastComputation<>(inputTwo).buildComputation(root);
            }
          };
          List<byte[]> actual = runApplication(testApplication);
          List<byte[]> expected = Arrays.asList(
              new byte[]{0x01, 0x42},
              new byte[]{0x02, 0x07}
          );
          assertEquals(expected.size(), actual.size());
          for (int i = 0; i < actual.size(); i++) {
            assertArrayEquals(expected.get(i), actual.get(i));
          }
        }
      };
    }
  }

}
