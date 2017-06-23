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

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.junit.Assert;

/**
 * Test class for the DEASolver.
 * Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem
 * as inputs (i.e. the number of input and output variables, the number
 * of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.    
 *
 */
public class CreditRaterTest {

	public static class TestCreditRater extends TestThreadFactory {

	  final int[] values;
	  final int[][] intervals;
	  final int[][] scores;
		
		public TestCreditRater(int[] values, int[][] intervals, int[][] scores) {
		  this.values = values;
		  this.intervals = intervals;
		  this.scores = scores;
		}

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new TestThread() {
				@Override
				public void test() throws Exception {
					
					OInt[] result = new OInt[1];
					
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
							
							SequentialProtocolProducer sseq = new SequentialProtocolProducer();
							
							SInt[] secretValues = ioBuilder.inputArray(values, 1);
							SInt[][] secretIntervals = new SInt[intervals.length][];
							SInt[][] secretScores = new SInt[scores.length][];
							for(int i =0; i< intervals.length; i++){
							  secretIntervals[i] = ioBuilder.inputArray(intervals[i], 1);
							}
              for(int i = 0; i< scores.length; i++){
                secretScores[i] = ioBuilder.inputArray(scores[i], 1);
              }
							sseq.append(ioBuilder.getProtocol());
		
							CreditRater rater = new CreditRater(AlgebraUtil.arrayToList(secretValues), 
							    AlgebraUtil.arrayToList(secretIntervals),
							    AlgebraUtil.arrayToList(secretScores));
							
							sseq.append(rater.prepareApplication(factory));
							result[0] = ioBuilder.output(rater.getResult());
							sseq.append(ioBuilder.getProtocol());
							
							return sseq;
						}
					};
					secureComputationEngine
							.runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
					Assert.assertThat(result[0].getValue(), Is.is(BigInteger.valueOf(PlaintextCreditRater.calculateScore(values, intervals, scores))));
				}
			};
		}
	}

}
