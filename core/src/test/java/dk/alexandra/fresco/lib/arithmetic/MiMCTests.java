/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
import dk.alexandra.fresco.framework.value.KnownSIntProtocol;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.SymmetricEncryptionBuilder;

public class MiMCTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	public static class TestMiMCEncSameEnc extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							OmniBuilder builder = new OmniBuilder(factory);
							SInt k = builder.getNumericIOBuilder().input(BigInteger.valueOf(527618), 2);							
							SInt x = builder.getNumericIOBuilder().input(BigInteger.valueOf(10), 1);
							
//							SInt k = builder.getNumericProtocolBuilder().known(527618);							
//							SInt x = builder.getNumericProtocolBuilder().known(10);
							
							SInt encX1 = builder.getSymmetricEncryptionBuilder().mimcEncrypt(x,k);
							SInt encX2 = builder.getSymmetricEncryptionBuilder().mimcEncrypt(x,k);
							
							OInt out1 = builder.getNumericIOBuilder().output(encX1);
							OInt out2 = builder.getNumericIOBuilder().output(encX2);							
							
							this.outputs = new OInt[] { out1, out2 };
							return builder.getProtocol();
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(app.getOutputs()[0].getValue(), app.getOutputs()[1].getValue());
				}
			};
		}
	}
	
	public static class TestMiMCEncDec extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					BigInteger x_big = BigInteger.valueOf(10);
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							BasicNumericFactory fac = (BasicNumericFactory) factory;
							SymmetricEncryptionBuilder symBuilder = new SymmetricEncryptionBuilder(fac);
							SInt k = fac.getSInt();
							KnownSIntProtocol knownKProtocol = fac.getSInt(20, k);
							
							SInt x = fac.getSInt();
							KnownSIntProtocol knownXProtocol = fac.getSInt(x_big, x);
							symBuilder.addProtocolProducer(knownKProtocol);
							symBuilder.addProtocolProducer(knownXProtocol);
							SInt encX = symBuilder.mimcEncrypt(x,k);
							SInt decX = symBuilder.mimcDecrypt(encX, k);							
							
							OInt outEnc = fac.getOInt();
							OInt out1 = fac.getOInt();
							symBuilder.addProtocolProducer(fac.getOpenProtocol(encX, outEnc));
							symBuilder.addProtocolProducer(fac.getOpenProtocol(decX, out1));
							
							this.outputs = new OInt[] { outEnc, out1 };
							return symBuilder.getProtocol();
						}
					};

					sce.runApplication(app);
					Assert.assertEquals(x_big, app.getOutputs()[1].getValue());
				}
			};
		}
	}
}
