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
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
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


  public static class TestCreditRater<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory {

    final int[] values;
    final int[][] intervals;
    final int[][] scores;

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
          ResourcePoolT resourcePool = ResourcePoolCreator.createResourcePool(conf.sceConf);

          Application<CreditRaterInput, ProtocolBuilderNumeric> input =
              producer -> {
                NumericBuilder numeric = producer.numeric();
                int[] values = TestCreditRater.this.values;
                List<Computation<SInt>> closedValues = knownArray(numeric, values);

                List<List<Computation<SInt>>> closedIntervals = Arrays.stream(intervals)
                    .map(array -> knownArray(numeric, array))
                    .collect(Collectors.toList());

                List<List<Computation<SInt>>> closedScores = Arrays.stream(scores)
                    .map(array -> knownArray(numeric, array))
                    .collect(Collectors.toList());
                return () -> new CreditRaterInput(closedValues, closedIntervals, closedScores);
              };
          CreditRaterInput creditRaterInput = secureComputationEngine
              .runApplication(input, resourcePool);

          CreditRater rater = new CreditRater(
              creditRaterInput.values,
              creditRaterInput.intervals,
              creditRaterInput.intervalScores);
          SInt creditRatingOutput = secureComputationEngine.runApplication(rater, resourcePool);

          Application<BigInteger, ProtocolBuilderNumeric> outputApp =
              seq -> seq.numeric().open(creditRatingOutput);

          BigInteger resultCreditOut = secureComputationEngine
              .runApplication(outputApp, resourcePool);

          Assert.assertThat(resultCreditOut, Is.is(
              BigInteger.valueOf(PlaintextCreditRater.calculateScore(values, intervals, scores))));
        }
      };
    }
  }

  private static List<Computation<SInt>> knownArray(NumericBuilder numeric, int[] values) {
    return Arrays.stream(values)
        .mapToObj(BigInteger::valueOf)
        .map(numeric::known)
        .collect(Collectors.toList());
  }

  private static class CreditRaterInput {

    private final List<Computation<SInt>> values;
    private final List<List<Computation<SInt>>> intervals;
    private final List<List<Computation<SInt>>> intervalScores;

    private CreditRaterInput(
        List<Computation<SInt>> values,
        List<List<Computation<SInt>>> intervals,
        List<List<Computation<SInt>>> intervalScores) {
      this.values = values;
      this.intervals = intervals;
      this.intervalScores = intervalScores;
    }
  }

}
