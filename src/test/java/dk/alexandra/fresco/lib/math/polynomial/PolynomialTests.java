package dk.alexandra.fresco.lib.math.polynomial;

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
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluatorFactory;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluatorFactoryImpl;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluatorProtocol;

public class PolynomialTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}
	}

	public static class TestPolynomialEvaluator extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {

			return new ThreadWithFixture() {
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
							sequentialProtocolProducer.append(ioBuilder.getCircuit());

							SInt result = basicNumericFactory.getSInt();
							PolynomialEvaluatorFactory polynomialEvaluatorFactory = new PolynomialEvaluatorFactoryImpl(
									basicNumericFactory);
							PolynomialEvaluatorProtocol polynomialEvaluatorProtocol = polynomialEvaluatorFactory
									.createPolynomialEvaluator(input, p, result);
							sequentialProtocolProducer.append(polynomialEvaluatorProtocol);

							OInt output = ioBuilder.output(result);
							sequentialProtocolProducer.append(ioBuilder.getCircuit());

							ProtocolProducer gp = sequentialProtocolProducer;
							outputs = new OInt[] { output };
							return gp;
						}
					};
					sce.runApplication(app);

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
