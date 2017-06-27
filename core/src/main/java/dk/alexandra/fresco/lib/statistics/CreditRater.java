/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 */
package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Application for performing credit rating.
 *
 * Given a dataset (a vector of values)
 * and a credit rating function (a set of intervals for each value)
 * will calculate the combined score.
 */
public class CreditRater implements Application<SInt> {

  private List<SInt> values;
  private List<List<SInt>> intervals;
  private List<List<SInt>> intervalScores;
  private Computation<SInt> delegateResult;

  /**
   * @throws MPCException if the intervals, values and intervalScores does not have the same length
   */
  public CreditRater(
      List<SInt> values, List<List<SInt>> intervals, List<List<SInt>> intervalScores)
      throws MPCException {
    this.values = values;
    this.intervals = intervals;
    this.intervalScores = intervalScores;
    if (!consistencyCheck()) {
      throw new MPCException("Inconsistent data");
    }
  }

  /**
   * Verify that the input values are consistent, i.e.
   * the there is an interval for each value
   *
   * @return If the input is consistent.
   */
  private boolean consistencyCheck() {
    if (this.values.size() != this.intervals.size()) {
      return false;
    }
    if (this.intervals.size() != (this.intervalScores.size())) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public ProtocolProducer prepareApplication(BuilderFactory provider) {
    return ProtocolBuilder.createApplicationRoot((BuilderFactoryNumeric) provider, (sequential) ->
        delegateResult = sequential.par(
            parallel -> {
              List<Computation<SInt>> scores = new ArrayList<>(values.size());
              for (int i = 0; i < values.size(); i++) {
                SInt value = values.get(i);
                List<SInt> interval = intervals.get(i);
                List<SInt> intervalScore = intervalScores.get(i);

                scores.add(
                    parallel.createSequentialSub(
                        new ComputeIntervalScore(interval, value, intervalScore)));
              }
              return () -> scores;
            }
        ).seq((list, seq) ->
            new SumSIntList(list).build(seq)
        )).build();
  }

  public SInt closeApplication() {
    if (delegateResult != null) {
      return this.delegateResult.out();
    } else {
      return null;
    }
  }

  private static class ComputeIntervalScore implements
      ComputationBuilder<SInt> {

    private final List<Computation<SInt>> interval;
    private final Computation<SInt> value;
    private final List<Computation<SInt>> scores;


    /**
     * Given a value and scores for an interval, will lookup the score for
     * the value.
     *
     * @param value The value to lookup
     * @param interval The interval definition
     * @param scores The scores for each interval
     */
    ComputeIntervalScore(List<SInt> interval, SInt value, List<SInt> scores) {
      this.interval = new ArrayList<>(interval);
      this.value = value;
      this.scores = new ArrayList<>(scores);
    }

    @Override
    public Computation<SInt> build(SequentialProtocolBuilder rootBuilder) {
      return rootBuilder.par(
          (parallelBuilder) -> {
            List<Computation<SInt>> result = new ArrayList<>();
            ComparisonBuilder builder = parallelBuilder.comparison();

            // Compare if "x <= the n interval definitions"
            for (Computation<SInt> anInterval : interval) {
              result.add(builder.compare(value, anInterval));
            }
            return () -> result;
          })
          // Add "x > last interval definition" to comparisons
          .seq((comparisons, builder) -> {
            NumericBuilder numericBuilder = builder.numeric();
            Computation<SInt> one = builder.createInputBuilder().known(BigInteger.valueOf(1));
            Computation<SInt> lastComparison = comparisons.get(comparisons.size() - 1);
            comparisons.add(numericBuilder.sub(one, lastComparison));
            return () -> comparisons;
          })
          //Comparisons now contain if x <= each definition and if x>= last definition
          .par((comparisons, parallelBuilder) -> {
            NumericBuilder numericBuilder = parallelBuilder.numeric();
            List<Computation<SInt>> innerScores = new ArrayList<>();
            innerScores.add(numericBuilder.mult(comparisons.get(0), scores.get(0)));
            for (int i = 1; i < scores.size() - 1; i++) {
              Computation<SInt> hit = numericBuilder
                  .sub(comparisons.get(i), comparisons.get(i - 1));
              innerScores.add(numericBuilder.mult(hit, scores.get(i)));
            }
            Computation<SInt> a = comparisons.get(scores.size() - 1);
            Computation<SInt> b = scores.get(scores.size() - 1);
            innerScores.add(numericBuilder.mult(a, b));
            return () -> innerScores;
          })
          .seq((list, seq) ->
              new SumSIntList(list).build(seq)
          );
    }
  }
}
