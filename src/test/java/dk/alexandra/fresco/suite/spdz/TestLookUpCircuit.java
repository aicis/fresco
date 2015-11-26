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
package dk.alexandra.fresco.suite.spdz;


public class TestLookUpCircuit {
	/*
	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;
		protected Random rand;
		protected SpdzProvider provider;

		@Override
		public void setUp() throws IOException {
			rand = new Random(0);
			sce = SCE.getInstance(conf.getMyId());
			sce.setSCEConfiguration(new TestSCEConfiguration("spdz", new SequentialEvaluator(), 1, conf.netConf));
			provider = sce.getProvider();
		}
	}

	/**
	 * Tests that looking up keys that are present in the key/value pairs works
	 * 
	 * @throws Exception
	 */
	/*
	@Test
	public void testCorrectLookUp() throws Exception {
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
						for (int i = 0; i < PAIRS; i++) {
							SInt sOut = provider.getSInt(NOTFOUND);
							LookUpCircuit<SInt> luc = provider
									.getLookUpCircuit(sKeys[i], sKeys, sValues,
											sOut);
							NumericIOFactory niob = new NumericIOFactory(
									provider);
							niob.addGateProducer(luc);
							OInt out = niob.output(sOut);
							sce.runApplication(niob.getCircuit());

							Assert.assertEquals(values[i], out.getValue()
									.intValue());
						}
					}
				};
			}
		}, 2);
	}
*/
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
