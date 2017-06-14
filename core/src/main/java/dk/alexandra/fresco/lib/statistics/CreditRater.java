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
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.AddSIntList;
import java.util.ArrayList;
import java.util.List;

/**
 * NativeProtocol for performing credit rating.
 *
 * Given a dataset (a vector of values)
 * and a credit rating function (a set of intervals for each value)
 * will calculate the combined score.
 */
public class CreditRater implements Application, Computation<SInt> {

  private static final long serialVersionUID = 7679664125131997196L;
  private List<SInt> values;
  private List<List<SInt>> intervals;
  private List<List<SInt>> intervalScores;

  private SInt score;

  /**
   * @throws MPCException
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
    this.score = ((BasicNumericFactory<SInt>) provider).getSInt();
    return ProtocolBuilder.createRoot(provider, (sequential) -> {
      List<Computation<? extends SInt>> individualScores = new ArrayList<>(this.values.size());

      for (int i = 0; i < this.values.size(); i++) {
        SInt value = values.get(i);
        List<SInt> interval = intervals.get(i);
        List<SInt> intervalScore = intervalScores.get(i);

        individualScores.add(computeIntervalScore(value,
            interval,
            intervalScore,
            sequential));
      }

      Computation<SInt> computation = sequential
          .createSequentialSubFactory(new AddSIntList(individualScores));
      sequential.createSequentialSubFactory((protocolBuilder) -> {
        this.score.setSerializableContent(computation.out().getSerializableContent());
      });
    }).build();
  }

  /**
   * Given a value and scores for an interval, will lookup the score for
   * the value.
   *
   * @param value The value to lookup
   * @param interval The interval definition
   * @param scores The scores for each interval
   * @return The score for the lookup
   */
  private Computation<SInt> computeIntervalScore(SInt value, List<SInt> interval, List<SInt> scores,
      ProtocolBuilder<SInt> rootBuilder) {

    List<SInt> comparisons = new ArrayList<>();
    rootBuilder.createParallelSubFactory((parallelSubFactory) -> {
      ComparisonProtocolFactory initialComparisons =
          parallelSubFactory.createAppendingComparisonProtocolFactory();

      // Compare if "x <= the n interval definitions"
      for (SInt anInterval : interval) {
        comparisons.add(initialComparisons.compare(value, anInterval).out());
      }
    });
    // Add "x > last interval definition" to comparisons
    rootBuilder.createSequentialSubFactory((protocolBuilder) -> {
      BasicNumericFactory<SInt> appendingFactory =
          protocolBuilder.createAppendingBasicNumericFactory();
      SInt one = appendingFactory.getSInt(1);
      comparisons.add(appendingFactory.sub(one, comparisons.get(comparisons.size() - 1)).out());
    });
    //Comparisons now contain if x <= each definition and if x>= last definition

    List<Computation<? extends SInt>> intermediateScores = new ArrayList<>();
    rootBuilder.createParallelSubFactory((parallelSubFactory) -> {
      BasicNumericFactory<SInt> factory =
          parallelSubFactory.createAppendingBasicNumericFactory();
      intermediateScores.add(factory.mult(comparisons.get(0), scores.get(0)));
      for (int i = 1; i < scores.size() - 1; i++) {
        SInt hit = factory.sub(comparisons.get(i), comparisons.get(i - 1)).out();
        intermediateScores.add(factory.mult(hit, scores.get(i)));
      }
      SInt a = comparisons.get(scores.size() - 1);
      SInt b = scores.get(scores.size() - 1);
      intermediateScores.add(factory.mult(a, b));
    });
    return rootBuilder.createSequentialSubFactory(new AddSIntList(intermediateScores));
  }

  @Override
  public SInt out() {
    return this.score;
  }
}
