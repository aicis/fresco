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
package dk.alexandra.fresco.lib.math.integer.division;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.Assert;

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

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	/**
	 * Test Euclidian division
	 */
	public static class TestEuclidianDivision extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new ThreadWithFixture() {
				private final BigInteger x = new BigInteger("123978634193227335452345761");
				private final BigInteger d = new BigInteger("6543212341214412");

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
							PreprocessedNumericBitFactory preprocessedNumericBitFactory = (PreprocessedNumericBitFactory) provider;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(80, basicNumericFactory, randomAdditiveMaskFactory);
							DivisionFactory euclidianDivisionFactory = new DivisionFactoryImpl(basicNumericFactory, rightShiftFactory);
							
							SInt quotient = basicNumericFactory.getSInt();
							SInt remainder = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(x, 1);
							OInt input2 = basicNumericFactory.getOInt(d);
							sequentialProtocolProducer.append(ioBuilder.getCircuit());
							
							DivisionProtocol euclidianDivisionProtocol = euclidianDivisionFactory.getDivisionProtocol(input1, x.bitLength() + 1, input2, quotient, remainder);
							sequentialProtocolProducer.append(euclidianDivisionProtocol);
							
							OInt output1 = ioBuilder.output(quotient);
							OInt output2 = ioBuilder.output(remainder);
							
							sequentialProtocolProducer.append(ioBuilder.getCircuit());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[] {output1, output2};
							
							return gp;
						}
					};
					sce.runApplication(app);
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
			
			return new ThreadWithFixture() {
				private final BigInteger x = new BigInteger("8000");
				private final BigInteger d = new BigInteger("3");
				private final int precision = 4; // How many bits of precision do we get? Should be 2^p / l...

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
							PreprocessedNumericBitFactory preprocessedNumericBitFactory = (PreprocessedNumericBitFactory) provider;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(80, basicNumericFactory, randomAdditiveMaskFactory);
							DivisionFactory divisionFactory = new DivisionFactoryImpl(basicNumericFactory, rightShiftFactory);
							
							SInt quotient = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(x, 1);
							SInt input2 = ioBuilder.input(d, 2);
							sequentialProtocolProducer.append(ioBuilder.getCircuit());
							
							DivisionProtocol divisionProtocol = divisionFactory.getDivisionProtocol(input1, input2, d.bitLength() + 1, precision, quotient);
							sequentialProtocolProducer.append(divisionProtocol);
							
							OInt output1 = ioBuilder.output(quotient);
							
							sequentialProtocolProducer.append(ioBuilder.getCircuit());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[] {output1};
							
							return gp;
						}
					};
					sce.runApplication(app);
					BigInteger quotient = app.getOutputs()[0].getValue();
					Assert.assertTrue(isInInterval(quotient, x.divide(d).intValue(), 1.0));
				}
			};
		}
		
		private static boolean isInInterval(BigInteger value, double center, double tolerance) {
			return value.intValue() >= center - tolerance && value.intValue() <= center + tolerance;
		}

	}
}
