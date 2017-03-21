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
package dk.alexandra.fresco.lib.math.integer.binary;

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
public class BinaryOperationsTests {

	/**
	 * Test binary right shift of a shared secret.
	 */
	public static class TestRightShift extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger input = BigInteger.valueOf(12332157);

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

							SInt result = basicNumericFactory.getSInt();
							SInt remainder = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(input, 1);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							RightShiftProtocol rightShiftProtocol = rightShiftFactory.getRightShiftProtocol(input1, result, remainder);
							sequentialProtocolProducer.append(rightShiftProtocol);
							
							OInt output1 = ioBuilder.output(result);
							OInt output2 = ioBuilder.output(remainder);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[] {output1, output2};
							
							return gp;
						}
					};
					sce.runApplication(app);
					BigInteger result = app.getOutputs()[0].getValue();
					BigInteger remainder = app.getOutputs()[1].getValue();
					
					Assert.assertEquals(result, input.shiftRight(1));
					Assert.assertEquals(remainder, input.mod(BigInteger.valueOf(2)));
				}
			};
		}
	}
	

	public static class TestRepeatedRightShift extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger input = BigInteger.valueOf(12332153);
				private final int n = 7;
				
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

							SInt result = basicNumericFactory.getSInt();
							
							SInt[] remainders = new SInt[n];
							for (int i = 0; i < n; i++) {
								remainders[i] = basicNumericFactory.getSInt();
							}
							
							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(input, 1);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							RepeatedRightShiftProtocol rightShiftProtocol = rightShiftFactory.getRepeatedRightShiftProtocol(input1, n, result, remainders);
							sequentialProtocolProducer.append(rightShiftProtocol);
							
							OInt shiftOutput = ioBuilder.output(result);
							OInt[] remainderOutput = ioBuilder.outputArray(remainders);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[n+1];
							outputs[0] = shiftOutput;
							for (int i = 0; i < n; i++) { 
								outputs[i+1] = remainderOutput[i];
							}
							return gp;
						}
					};
					sce.runApplication(app);
					
					BigInteger output = app.getOutputs()[0].getValue();
					Assert.assertEquals(input.shiftRight(n), output);

					BigInteger[] remainders = new BigInteger[n];
					for (int i = 0; i < n; i++) {
						remainders[i] = app.getOutputs()[i+1].getValue();
						Assert.assertEquals(input.testBit(i) ? BigInteger.ONE : BigInteger.ZERO, remainders[i]);
					}
				}
			};
		}
	}
	
	/**
	 * Test binary right shift of a shared secret.
	 */
	public static class TestMostSignificantBit extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger input = BigInteger.valueOf(5);

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
							
							SInt result = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(input, 1);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							BitLengthProtocol bitLengthProtocol = bitLengthFactory.getBitLengthProtocol(input1, result, input.bitLength() * 2);
							sequentialProtocolProducer.append(bitLengthProtocol);
							
							OInt output1 = ioBuilder.output(result);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[] {output1};
							
							return gp;
						}
					};
					sce.runApplication(app);
					BigInteger result = app.getOutputs()[0].getValue();
					
					System.out.println(result);
					
					Assert.assertEquals(BigInteger.valueOf(input.bitLength()), result);
				}
			};
		}
	}

}
