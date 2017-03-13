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
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;

/**
 * Tests which ensures that the SCE's parallel and sequential evaluations of
 * application works.
 * 
 * @author Kasper Damgaard
 *
 */
public class ParallelAndSequenceTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	public static class TestSequentialEvaluation extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplicationSum sumApp = new ParallelAndSequenceTests().new TestApplicationSum();
					TestApplicationMult multApp = new ParallelAndSequenceTests().new TestApplicationMult();

					sce.runApplicationsInSequence(sumApp, multApp);

					OInt sum = sumApp.getOutputs()[0];
					OInt mult = multApp.getOutputs()[0];
					Assert.assertEquals(BigInteger.valueOf(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9), sum.getValue());
					Assert.assertEquals(BigInteger.valueOf(1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9), mult.getValue());
				}
			};
		}
	}
	
	public static class TestParallelEvaluation extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplicationSum sumApp = new ParallelAndSequenceTests().new TestApplicationSum();
					TestApplicationMult multApp = new ParallelAndSequenceTests().new TestApplicationMult();

					sce.runApplicationsInParallel(sumApp, multApp);

					OInt sum = sumApp.getOutputs()[0];
					OInt mult = multApp.getOutputs()[0];
					Assert.assertEquals(BigInteger.valueOf(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9), sum.getValue());
					Assert.assertEquals(BigInteger.valueOf(1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9), mult.getValue());
				}
			};
		}
	}

	private class TestApplicationSum extends TestApplication {
		private static final long serialVersionUID = 1L;

		@Override
		public ProtocolProducer prepareApplication(ProtocolFactory factory) {
			OmniBuilder builder = new OmniBuilder(factory);
			SInt[] terms = builder.getNumericIOBuilder().inputArray(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 1);
			SInt sum = builder.getNumericProtocolBuilder().sum(terms);
			this.outputs = new OInt[] { builder.getNumericIOBuilder().output(sum) };
			return builder.getProtocol();
		}

	}

	private class TestApplicationMult extends TestApplication {
		private static final long serialVersionUID = 1L;

		@Override
		public ProtocolProducer prepareApplication(ProtocolFactory factory) {
			OmniBuilder builder = new OmniBuilder(factory);
			SInt[] terms = builder.getNumericIOBuilder().inputArray(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 1);
			SInt mult = builder.getNumericProtocolBuilder().mult(terms);
			this.outputs = new OInt[] { builder.getNumericIOBuilder().output(mult) };
			return builder.getProtocol();
		}

	}
}
