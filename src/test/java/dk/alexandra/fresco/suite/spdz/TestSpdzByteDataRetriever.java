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


public class TestSpdzByteDataRetriever {

	/*
	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;
		protected Random rand;
		protected SpdzProvider provider;
		protected DataRetriever supplier;
		
		@Override
		public void setUp() throws IOException {
			rand = new Random(0);
			sce = SCE.getInstance(conf.getMyId());
			sce.setSCEConfiguration(new TestSCEConfiguration("spdz", new SequentialEvaluator(), 1, conf.netConf));
			provider = sce.getProvider();
			supplier = new SpdzByteDataRetriever(conf.getMyId(), conf.netConf.noOfParties(), "triples/spdz2-byte0/");
		}
				
	}

	@Test
	public void testTriple() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						int noTriples = 100000;
						OInt[] outputs1 = new OInt[noTriples];
						OInt[] outputs2 = new OInt[noTriples];
						OInt[] outputs3 = new OInt[noTriples];

						ParallelGateProducer par = new ParallelGateProducer();
						for (int i = 0; i < noTriples; i++) {
							SpdzTriple triple = supplier.retrieveTriple();
							outputs1[i] = new SpdzOInt();
							outputs2[i] = new SpdzOInt();
							outputs3[i] = new SpdzOInt();
							SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
									new SpdzSInt(triple.getA()), outputs1[i]);
							SpdzOutputToAllGate o2 = new SpdzOutputToAllGate(
									new SpdzSInt(triple.getB()), outputs2[i]);
							SpdzOutputToAllGate o3 = new SpdzOutputToAllGate(
									new SpdzSInt(triple.getC()), outputs3[i]);
							par.append(o1);
							par.append(o2);
							par.append(o3);
						}

						sce.runApplication(par);
						for (int i = 0; i < noTriples; i++) {
							BigInteger ab = outputs1[i].getValue()
									.multiply(outputs2[i].getValue())
									.mod(Util.getModulus());
							Assert.assertEquals(Util.convertRepresentation(ab),
									outputs3[i].getValue());
						}
					}
				};
			}
		}, 2);
	}

	@Test
	public void testInput() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						for (int i = 0; i < 100; i++) {
							SpdzInputMask mask = supplier.retrieveInputMask(1);
							SpdzOInt output1 = new SpdzOInt();
							try {
								SpdzSInt number = new SpdzSInt(mask.getMask());
								SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
										number, output1);
								SequentialProtocolProducer prod = new SequentialProtocolProducer(
										o1);
								sce.runApplication(prod);
							} catch (MPCException e) {
								System.out.println("Failed on input: " + i);
							}
							if (conf.getMyId() == 1) {
								Assert.assertEquals(Util
										.convertRepresentation(mask
												.getRealValue()), output1
										.getValue());
							}
						}
					}
				};
			}
		}, 2);
	}

	@Test
	public void testInputs() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						final int TESTS = 100000;
						for (int playerid = 1; playerid < 3; playerid++) {
							BigInteger[] values = new BigInteger[TESTS];
							OInt[] outputs = new OInt[TESTS];
							ParallelGateProducer par = new ParallelGateProducer();
							for (int i = 0; i < TESTS; i++) {
								SpdzInputMask mask = supplier
										.retrieveInputMask(playerid);
								if (conf.getMyId() == playerid) {
									values[i] = mask.getRealValue();
								}
								outputs[i] = new SpdzOInt();
								SpdzSInt number = new SpdzSInt(mask.getMask());
								SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
										number, outputs[i]);
								par.append(o1);
							}
							sce.runApplication(par);
							for (int i = 0; i < TESTS; i++) {
								if (conf.getMyId() == playerid) {
									Assert.assertEquals(Util
											.convertRepresentation(values[i]),
											outputs[i].getValue());
								}
							}
						}
					}
				};
			}
		}, 2);
	}

	@Test
	public void testBit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						for (int i = 0; i < 1000; i++) {
							SpdzSInt bit = supplier.retrieveBit();

							SpdzOInt output1 = new SpdzOInt();
							SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
									bit, output1);
							SequentialProtocolProducer prod = new SequentialProtocolProducer(
									o1);

							sce.runApplication(prod);

							assertTrue(output1.getValue().equals(BigInteger.ONE)
									|| output1.getValue().equals(BigInteger.ZERO));
						}
					}
				};
			}
		}, 2);
	}

	@Test
	public void testExpPipe() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						for (int i = 0; i < 10; i++) {
							SpdzSInt[] expPipe = supplier.retrieveExpPipe();
							checkExpCircuit(sce, expPipe);
						}
					}
				};
			}
		}, 2);
	}

	private boolean checkExpCircuit(SCE sce, SInt[] outputs) {
		BigInteger[] openedVals = new BigInteger[outputs.length];
		for (int i = 0; i < outputs.length; i++) {
			SpdzOInt output1 = new SpdzOInt();
			SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(outputs[i],
					output1);
			SequentialProtocolProducer prod = new SequentialProtocolProducer(o1);

			sce.runApplication(prod);
			openedVals[i] = output1.getValue();
		}
		BigInteger R = openedVals[1];
		BigInteger nextR = R;
		BigInteger R_inv = openedVals[0];
		assertEquals(BigInteger.ONE, R.multiply(R_inv).mod(Util.getModulus()));
		for (int i = 2; i < outputs.length; i++) {
			nextR = R.multiply(nextR).mod(Util.getModulus());
			assertEquals(Util.convertRepresentation(nextR), openedVals[i]);
		}
		BigInteger[] Ms = Util.getClearExpPipe(R);
		for (int i = 0; i < Ms.length; i++) {
			assertEquals(Util.convertRepresentation(Ms[i]), openedVals[i + 1]);
		}
		return true;
	}
*/
}
