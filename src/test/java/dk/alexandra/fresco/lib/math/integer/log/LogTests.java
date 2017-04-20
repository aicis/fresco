/*******************************************************************************
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
package dk.alexandra.fresco.lib.math.integer.log;

import java.math.BigInteger;

import org.junit.Assert;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
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
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;


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
public class LogTests {

	public static class TestLogarithm extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger[] x = { BigInteger.valueOf(201235), BigInteger.valueOf(1234), BigInteger.valueOf(405068), BigInteger.valueOf(123456), BigInteger.valueOf(110) };

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
							NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
							IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory, rightShiftFactory);
							BitLengthFactory bitLengthFactory = new BitLengthFactoryImpl(basicNumericFactory, integerToBitsFactory);
							LogarithmFactory logarithmFactory = new LogarithmFactoryImpl(basicNumericFactory, rightShiftFactory, bitLengthFactory);
							
							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt[] inputs = ioBuilder.inputArray(x, 1);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							SInt[] logs = new SInt[x.length];
							
							for (int i = 0; i < inputs.length; i++) {
								logs[i] = basicNumericFactory.getSInt();
								LogarithmProtocol logarithmProtocol = logarithmFactory.getLogarithmProtocol(inputs[i], x[i].bitLength(), logs[i]);
								sequentialProtocolProducer.append(logarithmProtocol);
							}
							
							OInt[] outputs = ioBuilder.outputArray(logs);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							this.outputs = outputs;
							
							return gp;
						}
					};
					sce.runApplication(app);
					
					for (int i = 0; i < x.length; i++) {
						int actual = app.getOutputs()[i].getValue().intValue();
						int expected = (int) Math.log(x[i].doubleValue());
						int difference = Math.abs(actual - expected);						
						Assert.assertTrue(difference <= 1); // Difference should be less than a bit
					}					
				}
			};
		}
	}
}
