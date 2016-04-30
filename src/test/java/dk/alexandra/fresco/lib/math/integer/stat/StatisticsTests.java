/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.math.integer.stat;

import java.io.IOException;
import java.math.BigInteger;

import org.springframework.util.Assert;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.PreprocessedNumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactoryImpl;


/**
 * Generic test cases for basic finite field operations.
 * 
 * Can be reused by a test case for any protocol suite that implements the basic
 * field protocol factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in
 * mpc.lib or something.
 *
 */
public class StatisticsTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	public static class TestStatistics extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new ThreadWithFixture() {
				private final int[] data1 = {543,520,532,497,450,432};
				private final int[] data2 = {432,620,232,337,250,433};
				private final int[] data3 = {80,90,123,432,145,606};
				
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
							PreprocessedNumericBitFactory preprocessedNumericBitFactory = (PreprocessedNumericBitFactory) factory;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(80, basicNumericFactory, randomAdditiveMaskFactory);
							DivisionFactory euclidianDivisionFactory = new DivisionFactoryImpl(basicNumericFactory, rightShiftFactory);
							StatisticsFactory statisticsFactory = new StatisticsFactoryImpl(basicNumericFactory, euclidianDivisionFactory);
							
							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt mean1 = basicNumericFactory.getSInt();
							SInt mean2 = basicNumericFactory.getSInt();
							SInt variance = basicNumericFactory.getSInt();
							SInt covariance = basicNumericFactory.getSInt();
							
							SInt[][] covarianceMatrix = new SInt[3][3];
							for (int i = 0; i < 3; i++) {
								for (int j = 0; j < 3; j++) {
									covarianceMatrix[i][j] = basicNumericFactory.getSInt();
								}
							}
							
							SInt[][] input = ioBuilder.inputMatrix(new int[][] {data1, data2, data3}, 1);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							MeanProtocol arithmeticMeanProtocol = statisticsFactory.getMeanProtocol(input[0], 10, mean1);
							sequentialProtocolProducer.append(arithmeticMeanProtocol);

							MeanProtocol arithmeticMeanProtocol2 = statisticsFactory.getMeanProtocol(input[1], 10, mean2);
							sequentialProtocolProducer.append(arithmeticMeanProtocol2);

							VarianceProtocol varianceProtocol = statisticsFactory.getVarianceProtocol(input[0], 10, mean1, variance);
							sequentialProtocolProducer.append(varianceProtocol);
							
							CovarianceProtocol covarianceProtocol = statisticsFactory.getCovarianceProtocol(input[0], input[1], 10, mean1, mean2, covariance);
							sequentialProtocolProducer.append(covarianceProtocol);
							
							CovarianceMatrixProtocol covarianceMatrixProtocol = statisticsFactory.getCovarianceMatrixProtocol(input, 10, covarianceMatrix);
							sequentialProtocolProducer.append(covarianceMatrixProtocol);
							
							OInt output1 = ioBuilder.output(mean1);
							OInt output2 = ioBuilder.output(mean2);
							OInt output3 = ioBuilder.output(variance);
							OInt output4 = ioBuilder.output(covariance);
							OInt[][] output5 = ioBuilder.outputMatrix(covarianceMatrix);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[] {output1, output2, output3, output4, output5[0][0], output5[1][0]};
							
							return gp;
						}
					};
					sce.runApplication(app);
					BigInteger mean1 = app.getOutputs()[0].getValue();
					BigInteger mean2 = app.getOutputs()[1].getValue();
					BigInteger variance = app.getOutputs()[2].getValue();
					BigInteger covariance = app.getOutputs()[3].getValue();
					
					double sum = 0.0;
					for (int entry : data1) {
						sum += entry;
					}
					double mean1Exact = sum / data1.length;
							
					sum = 0.0;
					for (int entry : data2) {
						sum += entry;
					}
					double mean2Exact = sum / data2.length;
					
					
					double ssd = 0.0;
					for (int entry : data1) {
						ssd += (entry - mean1Exact) * (entry - mean1Exact);
					}
					double varianceExact = ssd / (data1.length - 1);
					
					double covarianceExact = 0.0;
					for (int i = 0; i < data1.length; i++) {
						covarianceExact += (data1[i] - mean1Exact) * (data2[i] - mean2Exact);
					}
					covarianceExact /= (data1.length - 1);
					
					double tolerance = 1.0;
					Assert.isTrue(isInInterval(mean1, mean1Exact, tolerance));
					Assert.isTrue(isInInterval(mean2, mean2Exact, tolerance));
					Assert.isTrue(isInInterval(variance, varianceExact, tolerance));
					Assert.isTrue(isInInterval(covariance, covarianceExact, tolerance));
					Assert.isTrue(isInInterval(app.getOutputs()[4].getValue(), varianceExact, tolerance));
					Assert.isTrue(isInInterval(app.getOutputs()[5].getValue(), covarianceExact, tolerance));

				}
			};
		}
		
		private static boolean isInInterval(BigInteger value, double center, double tolerance) {
			return value.intValue() >= center - tolerance && value.intValue() <= center + tolerance;
		}
	}
}
