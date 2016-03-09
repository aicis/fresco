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

public class BasicBooleanTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	public static class TestInput extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestBoolApplication app = new TestBoolApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
							BasicLogicBuilder builder = new BasicLogicBuilder(prov);
							SBool inp = builder.input(1, builder.getOBool(true));
							OBool output = builder.output(inp);
							this.outputs = new OBool[] { output };
							return builder.getCircuit();
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(true,
							app.getOutputs()[0].getValue());
				}
			};
		}
	}
	
	public static class TestXOR extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestBoolApplication app = new TestBoolApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
							BasicLogicBuilder builder = new BasicLogicBuilder(prov);
							SBool inp100 = builder.input(1, builder.getOBool(false));
							SBool inp200 = builder.input(1, builder.getOBool(false));
							
							SBool xor00 = builder.xor(inp100, inp200);
							
							SBool inp110 = builder.input(1, builder.getOBool(true));
							SBool inp210 = builder.input(1, builder.getOBool(false));
							
							SBool xor10 = builder.xor(inp110, inp210);
							
							SBool inp101 = builder.input(1, builder.getOBool(false));
							SBool inp201 = builder.input(1, builder.getOBool(true));
							
							SBool xor01 = builder.xor(inp101, inp201);
							
							SBool inp111 = builder.input(1, builder.getOBool(true));
							SBool inp211 = builder.input(1, builder.getOBool(true));
							
							SBool xor11 = builder.xor(inp111, inp211);
							
							OBool[] output = builder.output(xor00, xor10, xor01, xor11);
							this.outputs = output;
							return builder.getCircuit();
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(false,
							app.getOutputs()[0].getValue());
					Assert.assertEquals(true,
							app.getOutputs()[1].getValue());
					Assert.assertEquals(true,
							app.getOutputs()[2].getValue());
					Assert.assertEquals(false,
							app.getOutputs()[3].getValue());
				}
			};
		}
	}
	
	public static class TestAND extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestBoolApplication app = new TestBoolApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
							BasicLogicBuilder builder = new BasicLogicBuilder(prov);
							SBool inp1 = builder.input(1, builder.getOBool(true));
							SBool inp2 = builder.input(1, builder.getOBool(false));
							
							SBool and = builder.and(inp1, inp2);							
							
							OBool output = builder.output(and);
							this.outputs = new OBool[] { output };
							return builder.getCircuit();
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(false,
							app.getOutputs()[0].getValue());
				}
			};
		}
	}
	
	public static class TestNOT extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestBoolApplication app = new TestBoolApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
							BasicLogicBuilder builder = new BasicLogicBuilder(prov);
							SBool inp1 = builder.input(1, builder.getOBool(true));
							
							SBool not = builder.not(inp1);							
							
							OBool output = builder.output(not);
							this.outputs = new OBool[] { output };
							return builder.getCircuit();
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(false,
							app.getOutputs()[0].getValue());
				}
			};
		}
	}
	
	/**
	 * Tests both input, xor, not, and and output.
	 * Computes all variants of: NOT((i1 XOR i2) AND i1)
	 *
	 */
	public static class TestBasicProtocols extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestBoolApplication app = new TestBoolApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
							BasicLogicBuilder builder = new BasicLogicBuilder(prov);
							SBool inp100 = builder.input(1, builder.getOBool(false));
							SBool inp200 = builder.input(1, builder.getOBool(false));
							SBool xor00 = builder.xor(inp100, inp200);
							SBool and00 = builder.and(inp100, xor00);
							SBool not00 = builder.not(and00);	
							
							SBool inp110 = builder.input(1, builder.getOBool(true));
							SBool inp210 = builder.input(1, builder.getOBool(false));
							SBool xor10 = builder.xor(inp110, inp210);
							SBool and10 = builder.and(inp110, xor10);
							SBool not10 = builder.not(and10);
							
							SBool inp101 = builder.input(1, builder.getOBool(false));
							SBool inp201 = builder.input(1, builder.getOBool(true));
							SBool xor01 = builder.xor(inp101, inp201);
							SBool and01 = builder.and(inp101, xor01);
							SBool not01 = builder.not(and01);
							
							SBool inp111 = builder.input(1, builder.getOBool(true));
							SBool inp211 = builder.input(1, builder.getOBool(true));
							SBool xor11 = builder.xor(inp111, inp211);
							SBool and11 = builder.and(inp111, xor11);
							SBool not11 = builder.not(and11);
							
							//maybe remove again - test for having not before and
							SBool ainp111 = builder.input(1, builder.getOBool(true));
							SBool ainp211 = builder.input(1, builder.getOBool(true));
							SBool anot11 = builder.not(ainp211);
							SBool aand11 = builder.and(ainp111, anot11);
							
							OBool[] output = builder.output(not00, not10, not01, not11, aand11);
							this.outputs = output;
							return builder.getCircuit();
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(true,
							app.getOutputs()[0].getValue());
					Assert.assertEquals(false,
							app.getOutputs()[1].getValue());
					Assert.assertEquals(true,
							app.getOutputs()[2].getValue());
					Assert.assertEquals(true,
							app.getOutputs()[3].getValue());
					Assert.assertEquals(false,
							app.getOutputs()[4].getValue());
				}
			};
		}
	}
}
