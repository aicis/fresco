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
package dk.alexandra.fresco.lib.arithmetic;

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
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.ComparisonProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;

public class ComparisonTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf,
					conf.protocolSuiteConf);
		}

	}

	/**
	 * Compares the two numbers 3 and 5 and checks that 3 < 5. Also checks that 5 is not < 3
	 * @author Kasper Damgaard
	 *
	 */
	public static class TestCompareLT extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;
						
						private BigInteger three = BigInteger.valueOf(3);
						private BigInteger five = BigInteger.valueOf(5);
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
							LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
							NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
							ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
							PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
							SequentialProtocolProducer seq = new SequentialProtocolProducer();

							ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
									80, bnFactory, localInvFactory,
									numericBitFactory, expFromOIntFactory,
									expFactory);
							
							NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
							ComparisonProtocolBuilder compBuilder = new ComparisonProtocolBuilder(compFactory, bnFactory);
							
							SInt x = ioBuilder.input(three, 1);
							SInt y = ioBuilder.input(five, 1);
							seq.append(ioBuilder.getProtocol());
							
							SInt compResult1 = compBuilder.compare(x, y);
							SInt compResult2 = compBuilder.compare(y, x);
							OInt res1 = ioBuilder.output(compResult1);
							OInt res2 = ioBuilder.output(compResult2);
							outputs = new OInt[] {res1, res2};
							
							seq.append(compBuilder.getProtocol());
							seq.append(ioBuilder.getProtocol());
							
							return seq;
						}
					};
					sce.runApplication(app);
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[0].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[1].getValue());
				}
			};
		}
	}
	
	/**
	 * Compares the two numbers 3 and 5 and checks that 3 < 5. Also checks that 5 is not < 3
	 * @author Kasper Damgaard
	 *
	 */
	public static class TestCompareEQ extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;
						
						private BigInteger three = BigInteger.valueOf(3);
						private BigInteger five = BigInteger.valueOf(5);
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
							LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
							NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
							ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
							PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
							SequentialProtocolProducer seq = new SequentialProtocolProducer();

							ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
									80, bnFactory, localInvFactory,
									numericBitFactory, expFromOIntFactory,
									expFactory);
							
							NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
							ComparisonProtocolBuilder compBuilder = new ComparisonProtocolBuilder(compFactory, bnFactory);
							
							SInt x = ioBuilder.input(three, 1);
							SInt y = ioBuilder.input(five, 1);
							seq.append(ioBuilder.getProtocol());
							
							SInt compResult1 = compBuilder.compareEqual(x, x);
							SInt compResult2 = compBuilder.compareEqual(x, y);
							OInt res1 = ioBuilder.output(compResult1);
							OInt res2 = ioBuilder.output(compResult2);
							outputs = new OInt[] {res1, res2};
							
							seq.append(compBuilder.getProtocol());
							seq.append(ioBuilder.getProtocol());
							
							return seq;
						}
					};
					sce.runApplication(app);
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[0].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[1].getValue());
				}
			};
		}
	}
}
