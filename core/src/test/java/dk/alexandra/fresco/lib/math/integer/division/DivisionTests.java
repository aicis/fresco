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
package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;
import org.junit.Assert;


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
public class DivisionTests {

	/**
	 * Test Euclidian division
	 */
	public static class TestEuclidianDivision extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger x = new BigInteger("123978634193227335452345761");
				private final BigInteger d = new BigInteger("6543212341214412");

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
							NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
							ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)factory;
							PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)factory;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
							IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory, rightShiftFactory);
							BitLengthFactory bitLengthFactory = new BitLengthFactoryImpl(basicNumericFactory, integerToBitsFactory);
							ExponentiationFactory exponentiationFactory = new ExponentiationFactoryImpl(basicNumericFactory, integerToBitsFactory);
							ComparisonProtocolFactory comparisonFactory = new ComparisonProtocolFactoryImpl(80, basicNumericFactory, localInversionFactory, preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory);
							DivisionFactory divisionFactory = new DivisionFactoryImpl(basicNumericFactory, rightShiftFactory, bitLengthFactory, exponentiationFactory, comparisonFactory);
							
							SInt quotient = basicNumericFactory.getSInt();
							SInt remainder = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(x, 1);
							OInt input2 = basicNumericFactory.getOInt(d);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							DivisionProtocol euclidianDivisionProtocol = divisionFactory.getDivisionProtocol(input1, input2, quotient, remainder);
							sequentialProtocolProducer.append(euclidianDivisionProtocol);
							
							OInt output1 = ioBuilder.output(quotient);
							OInt output2 = ioBuilder.output(remainder);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							outputs = new OInt[] {output1, output2};

							return sequentialProtocolProducer;
						}
					};
					secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          BigInteger quotient = app.getOutputs()[0].getValue();
          BigInteger remainder = app.getOutputs()[1].getValue();
          Assert.assertEquals(quotient, x.divide(d));
          Assert.assertEquals(remainder, x.mod(d));
				}
			};
		}
	}
	
	/**
	 * Test division with secret shared divisor
	 */
	public static class TestSecretSharedDivision extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger[] x = new BigInteger[] {new BigInteger("1234567"), BigInteger.valueOf(1230121230), BigInteger.valueOf(313222110), BigInteger.valueOf(5111215), BigInteger.valueOf(6537) };
				private final BigInteger d = BigInteger.valueOf(1110);
				private final int n = x.length;
				
				OInt[] precision = new OInt[n];
				
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {

							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
							NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
							ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)factory;
							PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)factory;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
							IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory, rightShiftFactory);
							BitLengthFactory bitLengthFactory = new BitLengthFactoryImpl(basicNumericFactory, integerToBitsFactory);
							ExponentiationFactory exponentiationFactory = new ExponentiationFactoryImpl(basicNumericFactory, integerToBitsFactory);
							ComparisonProtocolFactory comparisonFactory = new ComparisonProtocolFactoryImpl(80, basicNumericFactory, localInversionFactory, preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory);
							DivisionFactory divisionFactory = new DivisionFactoryImpl(basicNumericFactory, rightShiftFactory, bitLengthFactory, exponentiationFactory, comparisonFactory);
							
							SInt[] quotient = new SInt[n];
							
							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt[] inputs = ioBuilder.inputArray(x, 1);
							SInt input2 = ioBuilder.input(d, 2);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							for (int i = 0; i < n; i++) {
								precision[i] = basicNumericFactory.getOInt();
								quotient[i] = basicNumericFactory.getSInt();
								DivisionProtocol divisionProtocol = divisionFactory.getDivisionProtocol(inputs[i], input2, quotient[i], precision[i]);
								sequentialProtocolProducer.append(divisionProtocol);
							}
							
							this.outputs = ioBuilder.outputArray(quotient);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							return sequentialProtocolProducer;
						}
					};
					secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          for (int i = 0; i < n; i++) {
            BigInteger actual = app.getOutputs()[i].getValue();

            BigInteger expected = x[i].divide(d);
            BigInteger difference = expected.subtract(actual).abs();
						
						int precision = expected.bitLength() - difference.bitLength();
						
						boolean isCorrect = expected.equals(actual);
						
						System.out.println(x[i] + "/" + d + " = " + actual + ", expected " + expected + ". " + (!isCorrect ? 
								"Got precision " + precision + "/" + expected.bitLength() + ", expected at least " + this.precision[i].getValue().intValue() : ""));
						if (!isCorrect) {
							Assert.assertTrue(precision >= this.precision[i].getValue().intValue());
						}
					}
				}
			};
		}
	}
}
