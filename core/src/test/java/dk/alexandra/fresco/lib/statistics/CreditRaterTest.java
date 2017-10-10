package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Assert;

/**
 * Test class for the DEASolver. Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem as inputs (i.e. the number of
 * input and output variables, the number of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.
 */
public class CreditRaterTest {


  public static class TestCreditRater<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final int[] values;
    final int[][] intervals;
    final int[][] scores;

    /**
     * Creates an instance of the Credit rater tester.
     * 
     * @param values The values to score.
     * @param intervals The intervals at which to separate scores (we assume the same number of
     *        intervals as values and scores.)
     * @param scores The DEA scores The score you get per interval.
     */
    public TestCreditRater(int[] values, int[][] intervals, int[][] scores) {
      this.values = values;
      this.intervals = intervals;
      this.scores = scores;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<CreditRaterInput, ProtocolBuilderNumeric> input = producer -> {
            Numeric numeric = producer.numeric();
            int[] values = TestCreditRater.this.values;
            List<DRes<SInt>> closedValues = knownArray(numeric, values);

            List<List<DRes<SInt>>> closedIntervals = Arrays.stream(intervals)
                .map(array -> knownArray(numeric, array)).collect(Collectors.toList());

            List<List<DRes<SInt>>> closedScores = Arrays.stream(scores)
                .map(array -> knownArray(numeric, array)).collect(Collectors.toList());
            return () -> new CreditRaterInput(closedValues, closedIntervals, closedScores);
          };
          CreditRaterInput creditRaterInput = runApplication(input);

          CreditRater rater = new CreditRater(creditRaterInput.values, creditRaterInput.intervals,
              creditRaterInput.intervalScores);
          SInt creditRatingOutput = runApplication((builder) -> builder.seq(rater));

          Application<BigInteger, ProtocolBuilderNumeric> outputApp =
              seq -> seq.numeric().open(creditRatingOutput);

          BigInteger resultCreditOut = runApplication(outputApp);

          Assert.assertThat(resultCreditOut, Is.is(
              BigInteger.valueOf(PlaintextCreditRater.calculateScore(values, intervals, scores))));
        }
      };
    }
  }

  private static List<DRes<SInt>> knownArray(Numeric numeric, int[] values) {
    return Arrays.stream(values).mapToObj(BigInteger::valueOf).map(numeric::known)
        .collect(Collectors.toList());
  }

  private static class CreditRaterInput {

    private final List<DRes<SInt>> values;
    private final List<List<DRes<SInt>>> intervals;
    private final List<List<DRes<SInt>>> intervalScores;

    private CreditRaterInput(List<DRes<SInt>> values, List<List<DRes<SInt>>> intervals,
        List<List<DRes<SInt>>> intervalScores) {
      this.values = values;
      this.intervals = intervals;
      this.intervalScores = intervalScores;
    }
  }

}
