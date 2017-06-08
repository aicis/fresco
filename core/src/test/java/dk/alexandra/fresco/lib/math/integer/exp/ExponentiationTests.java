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
package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
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
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;
import org.junit.Assert;

public class ExponentiationTests {

	/**
	 * Test binary right shift of a shared secret.
	 */
	public static class TestExponentiation extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger input = BigInteger.valueOf(12332157);
				private final int exp = 12;

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
							NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) provider;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							LocalInversionFactory localInversionFactory = (LocalInversionFactory) provider;
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
							IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory, rightShiftFactory);
							ExponentiationFactory exponentiationFactory = new ExponentiationFactoryImpl(basicNumericFactory, integerToBitsFactory);

							SInt result = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(input, 1);
							SInt input2 = ioBuilder.input(exp, 2);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							ExponentiationProtocol exponentiationProtocol = exponentiationFactory.getExponentiationCircuit(input1, input2, 5, result);
							sequentialProtocolProducer.append(exponentiationProtocol);
							
							OInt output1 = ioBuilder.output(result);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[] {output1};
							
							return gp;
						}
					};
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf));
          BigInteger result = app.getOutputs()[0].getValue();
					
					Assert.assertEquals(input.pow(exp), result);
				}
			};
		}
	}
	
}
