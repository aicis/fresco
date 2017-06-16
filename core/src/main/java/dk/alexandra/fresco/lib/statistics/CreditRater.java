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
 *******************************************************************************/
package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.AddSIntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
  public ProtocolProducer prepareApplication(ProtocolFactory provider) {
    return ProtocolBuilder.createRoot(provider, (sequential) -> {
      List<Computation<? extends SInt>> individualScores = new ArrayList<>(this.values.size());

      sequential.createParallelSubFactory((parallel) -> {
        for (int i = 0; i < this.values.size(); i++) {
          SInt value = values.get(i);
          List<SInt> interval = intervals.get(i);
          List<SInt> intervalScore = intervalScores.get(i);

          individualScores.add(
              parallel.createSequentialSubFactory(
                  new ComputeIntervalScore(interval, value, intervalScore)));
        }
      });

      delegateResult = sequential.createSequentialSubFactory(new AddSIntList(individualScores));
    }).build();
  }

  public SInt closeApplication() {
    if (delegateResult != null) {
      return this.delegateResult.out();
    } else {
      return null;
    }
  }

  private static class ComputeIntervalScore implements Consumer<SequentialProtocolBuilder<SInt>>,
      Computation<SInt> {

    private final List<Computation<SInt>> interval;
    private final Computation<SInt> value;
    private final List<Computation<SInt>> scores;
    private Computation<? extends SInt> delegageComputation;


    /**
     * Given a value and scores for an interval, will lookup the score for
     * the value.
     *
     * @param value The value to lookup
     * @param interval The interval definition
     * @param scores The scores for each interval
     */
    ComputeIntervalScore(List<SInt> interval, SInt value,
        List<SInt> scores) {
      this.interval = new ArrayList<>(interval);
      this.value = value;
      this.scores = new ArrayList<>(scores);
    }

    @Override
    public void accept(SequentialProtocolBuilder<SInt> rootBuilder) {
      List<Computation<SInt>> comparisons = new ArrayList<>();
      rootBuilder.createParallelSubFactory((parallelBuilder) -> {
        ComparisonProtocolFactory factory =
            parallelBuilder.createAppendingComparisonProtocolFactory();

        // Compare if "x <= the n interval definitions"
        for (Computation<SInt> anInterval : interval) {
          comparisons.add(factory.compare(value.out(), anInterval.out()));
        }
      });
      // Add "x > last interval definition" to comparisons
      rootBuilder.createSequentialSubFactory((builder) -> {
        BasicNumericFactory<SInt> factory = builder.createAppendingBasicNumericFactory();
        SInt one = factory.getSInt(1);
        comparisons.add(factory.sub(one, comparisons.get(comparisons.size() - 1)).out());
      });
      //Comparisons now contain if x <= each definition and if x>= last definition

      List<Computation<SInt>> intermediateScores = new ArrayList<>();
      rootBuilder.createParallelSubFactory((parallelBuilder) -> {
        BasicNumericFactory<SInt> factory =
            parallelBuilder.createAppendingBasicNumericFactory();
        intermediateScores.add(factory.mult(comparisons.get(0), scores.get(0)));
        for (int i = 1; i < scores.size() - 1; i++) {
          Computation<SInt> hit = factory.sub(comparisons.get(i), comparisons.get(i - 1));
          intermediateScores.add(factory.mult(hit, scores.get(i)));
        }
        Computation<SInt> a = comparisons.get(scores.size() - 1);
        Computation<SInt> b = scores.get(scores.size() - 1);
        intermediateScores.add(factory.mult(a, b));
      });
      AddSIntList<SInt> consumer = new AddSIntList<>(intermediateScores);
      this.delegageComputation = rootBuilder.createSequentialSubFactory(consumer);
    }

    @Override
    public SInt out() {
      return delegageComputation.out();
    }
  }
}
