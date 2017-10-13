package dk.alexandra.fresco.lib.collections.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test class for the CloseList protocol.
 */
public class CloseListTests {

  /**
   * Performs a CloseList computation on an empty list of BigIntegers. Checks that result is empty.
   */
  public static class TestCloseEmptyList<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define input and output
          List<BigInteger> input = new ArrayList<>();
          // define functionality to be tested
          Application<List<SInt>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<List<DRes<SInt>>> closed = root.collections().closeList(input, 1);
            return () -> closed.out().stream().map(DRes::out).collect(Collectors.toList());
          };
          List<SInt> output = runApplication(testApplication);
          assertTrue(output.isEmpty());
        }
      };
    }
  }

  /**
   * Opens and closes an input list of BigIntegers. Checks that opened result is same as original
   * input.
   */
  public static class TestCloseAndOpenList<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define input
          List<BigInteger> input = new ArrayList<>();
          input.add(BigInteger.valueOf(1));
          input.add(BigInteger.valueOf(2));
          input.add(BigInteger.valueOf(3));

          // define functionality to be tested
          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = root -> {
            Collections collections = root.collections();
            DRes<List<DRes<SInt>>> closed = null;
            if (root.getBasicNumericContext().getMyId() == 1) {
              // party 1 provides input
              closed = collections.closeList(input, 1);
            } else {
              // other parties receive it
              closed = collections.closeList(3, 1);
            }
            DRes<List<DRes<BigInteger>>> opened = collections.openList(closed);
            return () -> opened.out().stream().map(DRes::out).collect(Collectors.toList());
          };
          // run test application
          final List<BigInteger> output = runApplication(testApplication);
          
          // define expected result and assert
          List<BigInteger> expected = new ArrayList<>();
          expected.add(BigInteger.valueOf(1));
          expected.add(BigInteger.valueOf(2));
          expected.add(BigInteger.valueOf(3));
          assertThat(output, is(expected));
        }
      };
    }
  }
}
