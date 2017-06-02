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
package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluatorFactory;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluatorFactoryImpl;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluatorProtocol;
import java.math.BigInteger;
import org.springframework.util.Assert;

public class PolynomialTests {

	public static class TestPolynomialEvaluator extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {

			return new TestThread() {
				private final int[] coefficients = { 1, 0, 1, 2 };
				private final int x = 3;

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory provider) {

							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);

							PolynomialFactory polynomialFactory = new PolynomialFactoryImpl();
							Polynomial p = polynomialFactory.createPolynomial(ioBuilder.inputArray(
									coefficients, 1));
							
							SInt input = ioBuilder.input(x, 2);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							SInt result = basicNumericFactory.getSInt();
							PolynomialEvaluatorFactory polynomialEvaluatorFactory = new PolynomialEvaluatorFactoryImpl(
									basicNumericFactory);
							PolynomialEvaluatorProtocol polynomialEvaluatorProtocol = polynomialEvaluatorFactory
									.createPolynomialEvaluator(input, p, result);
							sequentialProtocolProducer.append(polynomialEvaluatorProtocol);

							OInt output = ioBuilder.output(result);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							ProtocolProducer gp = sequentialProtocolProducer;
							outputs = new OInt[] { output };
							return gp;
						}
					};
          secureComputationEngine.runApplication(app);

					int f = 0;
					int power = 1;
					for (int i = 0; i < coefficients.length; i++) {
						f += coefficients[i] * power;
						power *= x;
					}
					BigInteger result = app.getOutputs()[0].getValue();
					Assert.isTrue(result.intValue() == f);
				}
			};
		}
	}
}
