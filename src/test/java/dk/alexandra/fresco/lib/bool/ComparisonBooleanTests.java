package dk.alexandra.fresco.lib.bool;

import java.io.IOException;

import org.junit.Assert;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class ComparisonBooleanTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	/**
	 * Tests if the number 01010 > 01110 - then it reverses that.
	 * @author Kasper Damgaard
	 *
	 */
	public static class TestGreaterThan extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					boolean[] comp1 = new boolean[] {false, true, false, true, false};
					boolean[] comp2 = new boolean[] {false, true, true, true, false};
					
					TestBoolApplication app = new TestBoolApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
							BasicLogicBuilder builder = new BasicLogicBuilder(prov);
							
							SBool[] in1 = builder.input(1, comp1);
							SBool[] in2 = builder.input(1, comp2);
							
							SBool compRes1 = builder.greaterThan(in1, in2);
							SBool compRes2 = builder.greaterThan(in2, in1);
							
							OBool[] output = builder.output(compRes1, compRes2);
							this.outputs = output;
							return builder.getCircuit();
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(false,
							app.getOutputs()[0].getValue());
					Assert.assertEquals(true,
							app.getOutputs()[1].getValue());
				}
			};
		}
	}
}
