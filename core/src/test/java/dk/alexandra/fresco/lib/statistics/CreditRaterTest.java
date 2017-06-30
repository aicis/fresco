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
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderHelper;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import java.math.BigInteger;
import java.util.List;
import org.hamcrest.core.Is;
import org.junit.Assert;

/**
 * Test class for the DEASolver.
 * Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem
 * as inputs (i.e. the number of input and output variables, the number
 * of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.
 */
public class CreditRaterTest {


  public static class TestCreditRater<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric.SequentialProtocolBuilder> {

    final int[] values;
    final int[][] intervals;
    final int[][] scores;

    public TestCreditRater(int[] values, int[][] intervals, int[][] scores) {
      this.values = values;
      this.intervals = intervals;
      this.scores = scores;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric.SequentialProtocolBuilder> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric.SequentialProtocolBuilder> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric.SequentialProtocolBuilder>() {
        SInt[] secretValues;

        @Override
        public void test() throws Exception {
          ResourcePoolT resourcePool = SecureComputationEngineImpl.createResourcePool(conf.sceConf,
              conf.sceConf.getSuite());

          BigInteger result = null;

          SInt[][] secretIntervals = new SInt[intervals.length][];
          SInt[][] secretScores = new SInt[scores.length][];

          Application<List<BigInteger>, ProtocolBuilderNumeric.SequentialProtocolBuilder> input =
              producer -> {
                BuilderFactory factoryNumeric = ProtocolBuilderHelper.getFactoryNumeric(producer);

                ProtocolFactory provider = factoryNumeric.getProtocolFactory();
                BasicNumericFactory bnFactory = (BasicNumericFactory) provider;
                NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);

                SequentialProtocolProducer sseq = new SequentialProtocolProducer();

                secretValues = ioBuilder.inputArray(values, 1);
                for (int i = 0; i < intervals.length; i++) {
                  secretIntervals[i] = ioBuilder.inputArray(intervals[i], 1);
                }
                for (int i = 0; i < scores.length; i++) {
                  secretScores[i] = ioBuilder.inputArray(scores[i], 1);
                }
                sseq.append(ioBuilder.getProtocol());
                producer.append(sseq);
                return () -> null;
              };
          secureComputationEngine.runApplication(input, resourcePool);

          CreditRater rater = new CreditRater(AlgebraUtil.arrayToList(secretValues),
              AlgebraUtil.arrayToList(secretIntervals),
              AlgebraUtil.arrayToList(secretScores));
          SInt creditRatingOutput = secureComputationEngine.runApplication(rater, resourcePool);

          Application<BigInteger, SequentialProtocolBuilder> outputApp =
              seq -> seq.numeric()
                  .open(creditRatingOutput);
          BigInteger resultCreditOut = secureComputationEngine
              .runApplication(outputApp, resourcePool);
          Assert.assertThat(resultCreditOut, Is.is(
              BigInteger.valueOf(PlaintextCreditRater.calculateScore(values, intervals, scores))));
        }
      };
    }
  }

}
