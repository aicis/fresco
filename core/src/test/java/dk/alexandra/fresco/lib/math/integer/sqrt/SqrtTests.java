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
package dk.alexandra.fresco.lib.math.integer.sqrt;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
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
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;
import org.junit.Assert;

public class SqrtTests {

	public static class TestSquareRoot extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				
				private final BigInteger[] x = new BigInteger[] { 
						BigInteger.valueOf(1234), 
						BigInteger.valueOf(12345), 
						BigInteger.valueOf(123456), 
						BigInteger.valueOf(1234567),
						BigInteger.valueOf(12345678), 
						BigInteger.valueOf(123456789) 
						};
				private final int n = x.length;

				private OInt[] precision = new OInt[n];
				
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
							SquareRootFactory squareRootFactory = new SquareRootFactoryImpl(basicNumericFactory, divisionFactory, rightShiftFactory);
							
							SInt[] sqrt = new SInt[n];

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt[] inputs = ioBuilder.inputArray(x, 1);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							for (int i = 0; i < n; i++) {
								sqrt[i] = basicNumericFactory.getSInt();
								precision[i] = basicNumericFactory.getOInt();
								SquareRootProtocol squareRootProtocol = squareRootFactory.getSquareRootProtocol(inputs[i], x[i].bitLength(), sqrt[i], precision[i]);
								sequentialProtocolProducer.append(squareRootProtocol);
							}
							
							OInt[] outputs = ioBuilder.outputArray(sqrt);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							this.outputs = outputs;

							return sequentialProtocolProducer;
						}
					};

					secureComputationEngine
							.runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));

          for (int i = 0; i < n; i++) {
						BigInteger actual = app.getOutputs()[i].getValue();
						BigInteger expected = BigInteger.valueOf((long) Math.sqrt(x[i].intValue()));
						
						BigInteger difference = expected.subtract(actual).abs();
						
						int precision = expected.bitLength() - difference.bitLength();

						boolean shouldBeCorrect = precision >= expected.bitLength();
						expected.equals(actual);
						boolean isCorrect = expected.equals(actual);
						
						Assert.assertFalse(shouldBeCorrect && !isCorrect);
						
						System.out.println("sqrt(" + x[i] + ") = " + actual + ", expected " + expected + ". " + (!isCorrect ? 
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
