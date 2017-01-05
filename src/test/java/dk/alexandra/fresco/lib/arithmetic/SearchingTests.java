package dk.alexandra.fresco.lib.arithmetic;

import java.io.IOException;
import java.util.Random;

import org.junit.Assert;

import dk.alexandra.fresco.framework.Protocol;
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
import dk.alexandra.fresco.lib.collections.LookUpProtocol;
import dk.alexandra.fresco.lib.collections.LookUpProtocolFactory;
import dk.alexandra.fresco.lib.collections.LookupProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;
import dk.alexandra.fresco.lib.lp.LPFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.PreprocessedNumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;

public class SearchingTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf,
					conf.protocolSuiteConf);
		}

	}
	
	public static class TestIsSorted extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					final int PAIRS = 10;
					final int MAXVALUE = 20000;
					final int NOTFOUND = -1;
					int[] keys = new int[PAIRS];
					int[] values = new int[PAIRS];
					SInt[] sKeys = new SInt[PAIRS];
					SInt[] sValues = new SInt[PAIRS];
					TestApplication app = new TestApplication() {
						private static final long serialVersionUID = 7960372460887688296L;
																	
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							BasicNumericFactory bnf = (BasicNumericFactory) factory;
							SequentialProtocolProducer seq = new SequentialProtocolProducer();
							Random rand = new Random(0);
							for (int i = 0; i < PAIRS; i++) {
								keys[i] = i;
								sKeys[i] = bnf.getSInt();
								sValues[i] = bnf.getSInt();
								seq.append(bnf.getSInt(i, sKeys[i]));
								values[i] = rand.nextInt(MAXVALUE);
								seq.append(bnf.getSInt(values[i], sValues[i]));
							}
							return seq;
						}
					};
					sce.runApplication(app);
					for (int i = 0; i < PAIRS; i++) {
						final int counter = i;
						TestApplication app1 = new TestApplication() {
							
							@Override
							public ProtocolProducer prepareApplication(ProtocolFactory factory) {
								BasicNumericFactory bnf = (BasicNumericFactory) factory;
								LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
								PreprocessedNumericBitFactory numericBitFactory = (PreprocessedNumericBitFactory) factory;
								ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
								PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
								LPFactory lpFactory = new LPFactoryImpl(80, bnf, localInvFactory, numericBitFactory, expFromOIntFactory, expFactory);
								LookUpProtocolFactory<SInt> lpf = new LookupProtocolFactoryImpl(80, lpFactory, bnf);
								SInt sOut = bnf.getSInt(NOTFOUND);																
								LookUpProtocol<SInt> luc = lpf
										.getLookUpProtocol(sKeys[counter], sKeys, sValues,
												sOut);
								OInt out = bnf.getOInt();
								Protocol p = bnf.getOpenProtocol(sOut, out);
								this.outputs = new OInt[] {out};
								return new SequentialProtocolProducer(luc, p);
							}
						};
						
						sce.runApplication(app1);

						Assert.assertEquals(values[i], app1.outputs[0].getValue()
								.intValue());
					}
				}
			};	
		}
	}

	/**
	 * Tests that looking up keys that are present in the key/value pairs works
	 * also when the value is a list of values.
	 * 
	 * @throws Exception
	 */
	/*
	@Test
	public void testCorrectLookUpArray() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						NumericCircuitFactory ncb = new NumericCircuitFactory(
								provider);
						NumericIOFactory niob = new NumericIOFactory(provider);
						final int PAIRS = 50;
						final int MAXVALUE = 20000;
						final int NOTFOUND = -1;
						final int LISTLENGTH = 10;
						int[] notfound = new int[LISTLENGTH];
						Arrays.fill(notfound, NOTFOUND);
						int[] keys = new int[PAIRS];
						int[][] values = new int[PAIRS][LISTLENGTH];
						SInt[] sKeys = new SInt[PAIRS];
						SInt[][] sValues = new SInt[PAIRS][LISTLENGTH];
						for (int i = 0; i < PAIRS; i++) {
							keys[i] = i;
							sKeys[i] = provider.getSInt(i);
							for (int j = 0; j < LISTLENGTH; j++) {
								values[i][j] = rand.nextInt(MAXVALUE);
								sValues[i][j] = provider.getSInt(values[i][j]);
							}
						}
						sKeys = ncb.getSIntArray(keys);
						sValues = ncb.getSIntMatrix(values);
						for (int i = 0; i < PAIRS; i++) {
							SInt[] sOut = ncb.getSIntArray(notfound);
							LookUpCircuit<SInt> luc = provider
									.getLookUpCircuit(sKeys[i], sKeys, sValues,
											sOut);
							niob.beginSeqScope();
							niob.addGateProducer(luc);
							OInt[] outs = niob.outputArray(sOut);
							niob.endCurScope();
							sce.runApplication(niob.getCircuit());
							for (int j = 0; j < outs.length; j++) {
								Assert.assertEquals(values[i][j], outs[j]
										.getValue().intValue());
							}
						}
					}
				};
			}
		}, 2);
	}
*/
	/**
	 * Tests that looking up keys that are not present in the key/value pairs
	 * works also when the value is a list of values.
	 * 
	 * @throws Exception
	 */
	/*
	@Test
	public void testIncorrectLookUpArray() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						NumericCircuitFactory ncb = new NumericCircuitFactory(
								provider);
						NumericIOFactory niob = new NumericIOFactory(provider);
						final int PAIRS = 50;
						final int BADKEY = PAIRS + 1;
						final int MAXVALUE = 20000;
						final int NOTFOUND = -1;
						final int LISTLENGTH = 10;
						int[] notfound = new int[LISTLENGTH];
						Arrays.fill(notfound, NOTFOUND);
						int[] keys = new int[PAIRS];
						int[][] values = new int[PAIRS][LISTLENGTH];
						SInt[] sKeys = new SInt[PAIRS];
						SInt[][] sValues = new SInt[PAIRS][LISTLENGTH];
						for (int i = 0; i < PAIRS; i++) {
							keys[i] = i;
							sKeys[i] = provider.getSInt(i);
							for (int j = 0; j < LISTLENGTH; j++) {
								values[i][j] = rand.nextInt(MAXVALUE);
								sValues[i][j] = provider.getSInt(values[i][j]);
							}
						}
						sKeys = ncb.getSIntArray(keys);
						sValues = ncb.getSIntMatrix(values);
						SInt lookUpKey = provider.getSInt(PAIRS + 1);
						for (int i = 0; i < PAIRS; i++) {
							SInt[] sOut = ncb.getSIntArray(notfound);
							LookUpCircuit<SInt> luc = provider
									.getLookUpCircuit(lookUpKey, sKeys,
											sValues, sOut);
							niob.beginSeqScope();
							niob.addGateProducer(luc);
							OInt[] outs = niob.outputArray(sOut);
							niob.endCurScope();
							sce.runApplication(niob.getCircuit());
							for (int j = 0; j < outs.length; j++) {
								Assert.assertEquals(NOTFOUND, outs[j]
										.getValue().intValue());
							}
						}
					}
				};
			}
		}, 2);
	}
*/
	/**
	 * Tests that looking up keys that are not present in the key/value pairs
	 * works
	 * 
	 * @throws Exception
	 */
	/*
	@Test
	public void testIncorrectLookUp() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						final int PAIRS = 50;
						final int MAXVALUE = 20000;
						final int NOTFOUND = -1;
						int[] keys = new int[PAIRS];
						int[] values = new int[PAIRS];
						SInt[] sKeys = new SInt[PAIRS];
						SInt[] sValues = new SInt[PAIRS];
						for (int i = 0; i < PAIRS; i++) {
							keys[i] = i;
							sKeys[i] = provider.getSInt(i);
							values[i] = rand.nextInt(MAXVALUE);
							sValues[i] = provider.getSInt(values[i]);
						}
						SInt lookUpKey = provider.getSInt(PAIRS + 1);
						for (int i = 0; i < 1; i++) {
							SInt sOut = provider.getSInt(NOTFOUND);
							OInt out = provider.getOInt();

							AppendableGateProducer agp = new SequentialProtocolProducer();
							LookUpCircuit<SInt> luc = provider
									.getLookUpCircuit(lookUpKey, sKeys,
											sValues, sOut);
							agp.append(luc);
							agp.append(provider.getOpenIntCircuit(sOut, out));
							sce.runApplication(agp);

							Assert.assertEquals(NOTFOUND, out.getValue()
									.intValue());
						}
					}
				};
			}
		}, 2);
	}
*/
}
