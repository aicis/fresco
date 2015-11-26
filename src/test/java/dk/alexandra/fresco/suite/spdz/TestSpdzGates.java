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


public class TestSpdzGates {

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
			sce.setSCEConfiguration(new TestSCEConfiguration("spdz", new SequentialEvaluator(), 1, conf));
			provider = sce.getProvider();
			supplier = new SpdzByteDataRetriever(conf.getMyId(), conf.noOfParties(), "triples/spdz2-byte/");
		}

	}
	
	//@Ignore
	@Test
	public void testMultLots() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						BigInteger value1 = BigInteger.valueOf(10);
						BigInteger value2 = BigInteger.valueOf(50);
						SpdzSInt input1 = new SpdzSInt();
						SpdzSInt input2 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value1, input1, 1);
						Circuit c2 = new SpdzInputGate(value2, input2, 2);

						SInt out = provider.getSInt();
						
						ParallelGateProducer par = new ParallelGateProducer();
						SpdzMultGate mult = new SpdzMultGate(input1, input2,
								out);
						par.append(mult);
						Multiplier multiplier = new Multiplier();
						multiplier.left = input1;
						multiplier.right = input1;
						multiplier.p = provider;
						multiplier.limit = 10000000;
						par.append(multiplier);
						SpdzOInt outputValue = new SpdzOInt();
						Circuit output = new SpdzOutputToAllGate(out,
								outputValue);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new ProtocolProducer[] { c1, c2, par, output });
						sce.runApplication(prod);

						assertEquals(value1.multiply(value2), outputValue.getValue());
					}
				};
			}
		}, 2);
	}
	
	private class Multiplier extends AbstractRepeatCircuit {

		public int limit;
		public SInt left;
		public SInt right;
		public BasicNumericFactory p;
		private int i = -1;
		
		@Override
		protected ProtocolProducer getNextGateProducer() {
			i++;
			if (i == limit) {
				return null;
			}
			return p.getMultCircuit(left, right, p.getSInt());
		}
		
	}

	@Test
	public void testNegateBitCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						SInt bit = supplier.retrieveBit();

						SpdzOInt output1 = new SpdzOInt();
						SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(bit,
								output1);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								o1);

						sce.runApplication(prod);
						assertTrue(output1.getValue().equals(BigInteger.ONE)
								|| output1.getValue().equals(BigInteger.ZERO));
						BigInteger bitVal = output1.getValue();

						SInt out = provider.getSInt();
						Circuit negateCircuit = provider.getNegatedBitCircuit(
								bit, out);
						SpdzOInt output2 = new SpdzOInt();
						SpdzOutputToAllGate o2 = new SpdzOutputToAllGate(out,
								output2);
						SequentialProtocolProducer ouputProducer = new SequentialProtocolProducer(
								negateCircuit, o2);
						sce.runApplication(ouputProducer);
						assertEquals(BigInteger.ONE.subtract(bitVal),
								output2.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testCommitmentGates() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger s = BigInteger.valueOf(5);
						SpdzCommitment commitment = new SpdzCommitment(new Util().getHashFunction(), s, rand);
						Map<Integer, BigInteger> comms = new HashMap<Integer, BigInteger>();
						SpdzCommitGate comm = new SpdzCommitGate(commitment,
								comms);
						Map<Integer, BigInteger> ss = new HashMap<Integer, BigInteger>();
						SpdzOpenCommitGate open = new SpdzOpenCommitGate(
								commitment, comms, ss);

						ProtocolProducer gp = new SequentialProtocolProducer(
								new Circuit[] { comm, open });
						sce.runApplication(gp);

						if (conf.getMyId() == 1) {
							assertEquals(ss.get(1), s);
						} else if (conf.getMyId() == 2) {
							assertEquals(ss.get(0), s);
						}
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testOneInputOutputGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger value = BigInteger.valueOf(-10);
						SpdzSInt out1 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value, out1, 2);

						SpdzOInt out = new SpdzOInt();
						SpdzOutputToAllGate c3 = new SpdzOutputToAllGate(out1,
								out);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { c1, c3 });
						sce.runApplication(prod);

						assertEquals(value, out.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testNegativeInputOutputGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger value = BigInteger.valueOf(-1);
						SpdzSInt out1 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value, out1, 2);

						SpdzOInt out = new SpdzOInt();
						SpdzOutputToAllGate c3 = new SpdzOutputToAllGate(out1,
								out);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { c1, c3 });
						sce.runApplication(prod);

						assertEquals(value, out.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testInputGates() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger value = BigInteger.valueOf(10);
						SpdzSInt out1 = new SpdzSInt();
						SpdzSInt out2 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value, out1, 1);
						Circuit c2 = new SpdzInputGate(value.add(value), out2, 2);

						SpdzOInt out = new SpdzOInt();
						SpdzOInt out_2 = new SpdzOInt();
						SpdzOutputToAllGate c3 = new SpdzOutputToAllGate(out1,
								out);
						SpdzOutputToAllGate c4 = new SpdzOutputToAllGate(out2,
								out_2);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] {c1, c3});
						sce.runApplication(prod);
						if (out2.value == null) {
							System.out.println("id:" + conf.getMyId());
						}
						//assertEquals(value, out.value);
						//assertEquals(value.add(value), out_2.value);
					}
				};
			}
		}, 2);
	}
*/
	/**
	 * Tests mixed input and multgates to point out in error in receiving
	 * broadcasts
	 */
	/*
	@Ignore
	@Test
	public void testInputGates2() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger value1 = BigInteger.valueOf(10);
						BigInteger value2 = BigInteger.valueOf(80);
						SpdzSInt out1 = new SpdzSInt();
						SpdzSInt out2 = new SpdzSInt();
						SpdzSInt out3 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value1, out1, 1);
						Circuit c2 = new SpdzInputGate(value1.add(value1),
								out2, 2);
						ProtocolProducer firstInput = new ParallelGateProducer(c1,
								c2);

						Circuit c3 = new SpdzInputGate(value2, out3, 2);
						Circuit m1 = new SpdzMultGate(out1, out2, out2);
						ProtocolProducer inputAndMult = new ParallelGateProducer(
								m1, c3);

						SpdzOInt mult_out = new SpdzOInt();
						SpdzOInt input3_out = new SpdzOInt();
						SpdzOutputToAllGate c4 = new SpdzOutputToAllGate(out2,
								mult_out);
						SpdzOutputToAllGate c5 = new SpdzOutputToAllGate(out3,
								input3_out);
						ProtocolProducer output = new ParallelGateProducer(c4, c5);
						sce.runApplication(new SequentialProtocolProducer(firstInput,
								inputAndMult, output));

						assertEquals(value1.add(value1).multiply(value1),
								mult_out.getValue());
						assertEquals(value2, input3_out.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testAddGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger value = BigInteger.valueOf(10);
						SpdzSInt out1 = new SpdzSInt();
						SpdzSInt out2 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value, out1, 1);
						Circuit c2 = new SpdzInputGate(value.add(value), out2,
								2);

						SInt out = provider.getSInt();
						Circuit add = provider.getAddCircuit(out1, out2, out);

						SInt out_ = provider.getSInt();
						Circuit add2 = provider.getAddCircuit(
								provider.getSInt(2), provider.getSInt(2), out_);

						SpdzOInt output = new SpdzOInt();
						SpdzOInt output_ = new SpdzOInt();
						SpdzOutputToAllGate c3 = new SpdzOutputToAllGate(out,
								output);
						SpdzOutputToAllGate c4 = new SpdzOutputToAllGate(out_,
								output_);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								c1, c2, add, add2, c3, c4);

						sce.runApplication(prod);
						BigInteger outputValue = output.getValue();
						assertEquals(value.add(value.add(value)), outputValue);
						assertEquals(BigInteger.valueOf(4), output_.getValue());

						SInt out__ = provider.getSInt();
						Circuit add3 = provider
								.getAddCircuit(provider.getSInt(4),
										provider.getOInt(5), out__);
						SpdzOInt output__ = new SpdzOInt();
						Circuit output3 = provider.getOpenIntCircuit(out__,
								output__);
						prod = new SequentialProtocolProducer(add3, output3);
						sce.runApplication(prod);
						assertEquals(BigInteger.valueOf(9), output__.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testIncrementGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						

						SInt out = provider.getSInt();
						SInt in = provider.getSInt(10);
						ProtocolProducer inc = provider
								.getIncrementByOneCircuit(in, out);
						SpdzOInt output = new SpdzOInt();
						Circuit output3 = provider.getOpenIntCircuit(out,
								output);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								inc, output3);
						sce.runApplication(prod);
						assertEquals(BigInteger.valueOf(11), output.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testMultAndOutputGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger value1 = BigInteger.valueOf(10);
						BigInteger value2 = BigInteger.valueOf(54);
						SpdzSInt input1 = new SpdzSInt();
						SpdzSInt input2 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value1, input1, 1);
						Circuit c2 = new SpdzInputGate(value2, input2, 2);

						SInt out = provider.getSInt();
						SpdzMultGate mult = new SpdzMultGate(input1, input2,
								out);

						SpdzOInt outputValue = new SpdzOInt();
						Circuit output = new SpdzOutputToAllGate(out,
								outputValue);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { c1, c2, mult, output });
						sce.runApplication(prod);

						assertEquals(value1.multiply(value2), outputValue.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Ignore
	@Test
	public void testSquareAndOutputGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						BigInteger value1 = BigInteger.valueOf(53);
						SpdzSInt input1 = new SpdzSInt();
						Circuit c1 = new SpdzInputGate(value1, input1, 1);

						SInt out = provider.getSInt();
						SpdzSquareGate mult = new SpdzSquareGate(input1, out);

						SpdzOInt outputValue = new SpdzOInt();
						Circuit output = new SpdzOutputToAllGate(out,
								outputValue);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { c1, mult, output });
						sce.runApplication(prod);

						assertEquals(value1.multiply(value1), outputValue.getValue());
						
					}
				};
			}
		}, 2);
	}

	// Test for running 3 multgates and an output gate doing the following:
	// (10*10)^2 --output--> party 1
	@Test
	public void testMultipleMultAndOutputGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						

						BigInteger value = new BigInteger("10");
						SpdzSInt inOut = new SpdzSInt();
						int towardsPlayer = 1;
						Circuit c1 = new SpdzInputGate(value, inOut,
								towardsPlayer);

						SpdzSInt outM1, outM2, outM3;
						outM1 = new SpdzSInt();
						outM2 = new SpdzSInt();
						outM3 = new SpdzSInt();

						SpdzMultGate c2 = new SpdzMultGate(inOut, inOut, outM1);

						SpdzMultGate c3 = new SpdzMultGate(outM1, outM1, outM2);

						SpdzOInt openInput = new SpdzOInt();
						openInput.setValue(BigInteger.valueOf(13));
						SpdzMultGate c4 = new SpdzMultGate(openInput, inOut,
								outM3);

						// Done with mult gate - now doing a output gate to open
						// the value towards party 1 only.
						SpdzOInt out1 = new SpdzOInt();
						SpdzOInt out2 = new SpdzOInt();
						SpdzOutputGate c5 = new SpdzOutputGate(outM2, out1, 1);
						SpdzOutputGate c6 = new SpdzOutputGate(outM3, out2, 1);
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { c1, c2, c3, c4, c5, c6 });
						sce.runApplication(prod);
						if (conf.getMyId() == 1) {
							assertEquals(BigInteger.valueOf(10000).mod(Util.getModulus()),
									out1.getValue());
							assertEquals(BigInteger.valueOf(13 * 10),
									out2.getValue());
						}
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testOutputGate1() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						SInt in = provider.getSInt(200);
						SpdzOInt out1 = new SpdzOInt(), out2 = new SpdzOInt();

						ProtocolProducer o1 = provider.getOpenIntCircuit(in, out1);
						ProtocolProducer o2 = provider.getOpenIntCircuit(in, out2);

						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								o1, o2);
						sce.runApplication(prod);

						if (conf.getMyId() == 1)
							assertEquals(new BigInteger("200"), out1.getValue());
						if (conf.getMyId() == 2)
							assertEquals(new BigInteger("200"), out2.getValue());
						
					}
				};
			}
		}, 2);
	}

	@Test
	public void testOutputGate() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {	
						
						SInt in = provider.getSInt(100);
						System.out.println(in);
						SpdzOInt out1 = new SpdzOInt(), out2 = new SpdzOInt();
						SpdzOutputGate o2 = new SpdzOutputGate(in, out2, 2);						
						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { o2 });
						
						sce.runApplication(prod);
						//if (conf.getMyId() == 1)
						//	assertEquals(randomNumber, out1.value);
						if (conf.getMyId() == 2)
							assertEquals(BigInteger.valueOf(100), out2.getValue());
						
					}
				};
			}
		}, 2);
	}
*/
	/**
	 * Testing all gates combined into a "big" circuit consisting of 3 input
	 * gates, 2 mult gates, 1 add gate and 3 output gates: (x1*x2)+(x3*x1),
	 * where xi is the input from Pi. When x1=x3=5 and x2=3, then the result
	 * should be (5*3)+(5*5) = 40
	 */
	/*
	@Test
	public void testCompleteCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						

						SpdzSInt in1 = new SpdzSInt();
						SpdzSInt in2 = new SpdzSInt();
						SpdzSInt in3 = new SpdzSInt();

						// 3 input gates
						SpdzInputGate i1 = new SpdzInputGate(
								new BigInteger("5"), in1, 1);
						SpdzInputGate i2 = new SpdzInputGate(
								new BigInteger("3"), in2, 2);
						SpdzInputGate i3 = new SpdzInputGate(
								new BigInteger("5"), in3, 2);

						// 2 mult gates
						SpdzSInt outputM1 = new SpdzSInt();
						SpdzSInt outputM2 = new SpdzSInt();
						SpdzMultGate m1 = new SpdzMultGate(in1, in2, outputM1);
						SpdzMultGate m2 = new SpdzMultGate(in3, in1, outputM2);

						// 1 add gate
						SpdzSInt outputA1 = new SpdzSInt();
						SpdzAddGate a = new SpdzAddGate(outputM1, outputM2,
								outputA1);

						// 1 output gate
						SpdzOInt out1 = new SpdzOInt();

						SpdzOutputToAllGate o1 = new SpdzOutputToAllGate(
								outputA1, out1);

						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { i1, i2, i3, m1, m2, a, o1 });
						sce.runApplication(prod);

						assertEquals(BigInteger.valueOf(40), out1.getValue());

						
					}
				};
			}
		}, 2);
	}
*/
	/**
	 * Testing all gates combined into a "big" circuit consisting of 3 input
	 * gates, 2 mult gates, 1 add gate and 3 output gates: (x1*x2)+(x3*x1),
	 * where xi is the input from Pi. When x1=x3=5 and x2=3, then the result
	 * should be (5*3)+(5*5) = 40. Then we multiply this open result with the
	 * output of multiplying input1 and input2.
	 */
	/*
	@Test
	public void testCompleteCircuitWithContinueAfterOutput() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						

						SpdzSInt in1 = new SpdzSInt();
						SpdzSInt in2 = new SpdzSInt();
						SpdzSInt in3 = new SpdzSInt();

						// 3 input gates
						SpdzInputGate i1 = new SpdzInputGate(
								new BigInteger("5"), in1, 1);
						SpdzInputGate i2 = new SpdzInputGate(
								new BigInteger("3"), in2, 2);
						SpdzInputGate i3 = new SpdzInputGate(
								new BigInteger("5"), in3, 2);

						// 2 mult gates
						SpdzSInt outputM1 = new SpdzSInt();
						SpdzSInt outputM2 = new SpdzSInt();
						SpdzMultGate m1 = new SpdzMultGate(in1, in2, outputM1);
						SpdzMultGate m2 = new SpdzMultGate(in3, in1, outputM2);

						// 1 add gate
						SpdzSInt outputA1 = new SpdzSInt();
						SpdzAddGate a = new SpdzAddGate(outputM1, outputM2,
								outputA1);

						// 3 output gates
						SpdzOInt out1 = new SpdzOInt();

						OpenCircuit<SInt, OInt> o1 = provider
								.getOpenIntCircuit(outputA1, out1);

						SequentialProtocolProducer prod = new SequentialProtocolProducer(
								new Circuit[] { i1, i2, i3, m1, m2, a, o1 });
						sce.runApplication(prod);

						assertEquals(BigInteger.valueOf(40), out1.getValue());

						SpdzSInt outputM3 = new SpdzSInt();
						SpdzMultGate m3 = new SpdzMultGate(out1, outputM1,
								outputM3); // 40*5*3 = 600

						SpdzOInt finalOut = new SpdzOInt();
						SpdzOutputGate o4 = new SpdzOutputGate(outputM3,
								finalOut, 1);

						prod = new SequentialProtocolProducer(new Circuit[] { m3,
								o4 });
						sce.runApplication(prod);

						if (conf.getMyId() == 1) {
							assertEquals(BigInteger.valueOf(600),
									finalOut.getValue());
						}

						
					}
				};
			}
		}, 2);
	}

	@Ignore
	// takes too long time in a run. Enable if need be.
	@Test
	public void testTiming() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(NetworkConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						

						long avgTime = 0;
						int rounds = 15;
						int warmupRounds = 6;
						int noOfMultGates = 500000;
						int batchSize = 50;
						boolean shouldBeSequential = false;
						// extract the first triple for testing - ensures that
						// the mac check is correct at the end.
						SpdzTriple triple = supplier
								.retrieveTriple();

						for (int i = 0; i < rounds + warmupRounds; i++) {							
							Circuit[] c = new Circuit[noOfMultGates + 2];
							SpdzSInt output1 = new SpdzSInt(triple.getA());
							SpdzSInt output2 = new SpdzSInt(triple.getB());
							// SpdzSInt output3 = new SpdzSInt(triple.getC());

							SpdzSInt multOut = new SpdzSInt();
							for (int j = 0; j < noOfMultGates; j++) {
								c[j] = new SpdzMultGate(output1, output2,
										multOut);
							}

							SpdzOInt out = new SpdzOInt();
							SpdzOutputGate out1 = new SpdzOutputGate(multOut,
									out, 1);
							SpdzOutputGate out2 = new SpdzOutputGate(multOut,
									out, 2);
							// SpdzOutputGate out3 = new SpdzOutputGate(multOut,
							// out, 3);

							c[noOfMultGates] = out1;
							c[noOfMultGates + 1] = out2;
							// c[noOfMultGates+2]= out3;

							ProtocolProducer prod;
							if (shouldBeSequential)
								prod = new SequentialProtocolProducer(c);
							else
								prod = new ParallelGateProducer(c);

							long time = System.nanoTime();
							sce.runApplication(prod);
							long totalTime = System.nanoTime() - time;
							if (i == warmupRounds)
								System.out
										.println("Done preparing WM - now starting data timing collection...");
							if (i > warmupRounds - 1)
								avgTime += totalTime;
							System.out.println("Total time for doing "
									+ noOfMultGates + " multiplication gates: "
									+ totalTime / 1000000.0 + " ms");

							// if(out.value.equals(new BigInteger("100"))){
							// System.out.println("Circuit successfully evaluated..");
							// }
						}
						avgTime = avgTime / rounds;
						System.out.println("The average running time over "
								+ rounds + " evaluations of the circuit was: "
								+ avgTime / 1000000.0 + " ms");
						System.out
								.println("Which can be translated to a benchmark of "
										+ (avgTime / 1000000.0)
										/ noOfMultGates
										+ " ms per multiplication gate. (Including output phase MAC checking)");
						System.out.println("Or in other words: "
								+ noOfMultGates
								* Math.pow(avgTime / 1000000000.0, -1)
								+ " multiplications per second");

						
					}
				};
			}
		}, 2);
	}
	*/
}
