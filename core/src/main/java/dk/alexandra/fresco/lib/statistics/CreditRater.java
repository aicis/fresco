package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Application for performing credit rating.
 *
 * Given a dataset (a vector of values) and a credit rating function (a set of intervals for each
 * value) will calculate the combined score.
 */
public class CreditRater implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private List<DRes<SInt>> values;
  private List<List<DRes<SInt>>> intervals;
  private List<List<DRes<SInt>>> intervalScores;

  /**
   * @throws IllegalArgumentException if the intervals, values and intervalScores does not have the same length
   */
  public CreditRater(
      List<DRes<SInt>> values, List<List<DRes<SInt>>> intervals,
      List<List<DRes<SInt>>> intervalScores){
    this.values = values;
    this.intervals = intervals;
    this.intervalScores = intervalScores;
    if (!consistencyCheck()) {
      throw new IllegalArgumentException("Inconsistent data");
    }
  }

  /**
   * Verify that the input values are consistent, i.e. the there is an interval for each value
   *
   * @return If the input is consistent.
   */
  private boolean consistencyCheck() {
    return this.values.size() == this.intervals.size()
        && this.intervals.size() == (this.intervalScores.size());
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric sequential) {
    return sequential.par(
        parallel -> {
          List<DRes<SInt>> scores = new ArrayList<>(values.size());
          for (int i = 0; i < values.size(); i++) {
            DRes<SInt> value = values.get(i);
            List<DRes<SInt>> interval = intervals.get(i);
            List<DRes<SInt>> intervalScore = intervalScores.get(i);

            scores.add(
                parallel.seq(new ComputeIntervalScore(interval, value, intervalScore)));
          }
          return () -> scores;
        }
    ).seq((seq, list) -> seq.advancedNumeric().sum(list));
  }

  private static class ComputeIntervalScore implements
      Computation<SInt, ProtocolBuilderNumeric> {

    private final List<DRes<SInt>> interval;
    private final DRes<SInt> value;
    private final List<DRes<SInt>> scores;

    /**
     * Given a value and scores for an interval, will lookup the score for the value.
     *
     * @param value The value to lookup
     * @param interval The interval definition
     * @param scores The scores for each interval
     */
    ComputeIntervalScore(List<DRes<SInt>> interval, DRes<SInt> value,
        List<DRes<SInt>> scores) {
      this.interval = interval;
      this.value = value;
      this.scores = scores;
    }

    @Override
    public DRes<SInt> buildComputation(ProtocolBuilderNumeric rootBuilder) {
      return rootBuilder.par((parallelBuilder) -> {
        List<DRes<SInt>> result = new ArrayList<>();
        Comparison builder = parallelBuilder.comparison();

        // Compare if "x <= the n interval definitions"
        for (DRes<SInt> anInterval : interval) {
          result.add(builder.compareLEQ(value, anInterval));
        }
        return () -> result;
      }).seq((builder, comparisons) -> {
        // Add "x > last interval definition" to comparisons

        Numeric numericBuilder = builder.numeric();
        DRes<SInt> lastComparison = comparisons.get(comparisons.size() - 1);
        comparisons.add(numericBuilder.sub(BigInteger.ONE, lastComparison));
        return () -> comparisons;
      }).par((parallelBuilder, comparisons) -> {
        //Comparisons now contain if x <= each definition and if x>= last definition

        Numeric numericBuilder = parallelBuilder.numeric();
        List<DRes<SInt>> innerScores = new ArrayList<>();
        innerScores.add(numericBuilder.mult(comparisons.get(0), scores.get(0)));
        for (int i = 1; i < scores.size() - 1; i++) {
          DRes<SInt> hit = numericBuilder
              .sub(comparisons.get(i), comparisons.get(i - 1));
          innerScores.add(numericBuilder.mult(hit, scores.get(i)));
        }
        DRes<SInt> a = comparisons.get(scores.size() - 1);
        DRes<SInt> b = scores.get(scores.size() - 1);
        innerScores.add(numericBuilder.mult(a, b));
        return () -> innerScores;

      }).seq((seq, list) -> seq.advancedNumeric().sum(list));
    }
  }
}
