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
package dk.alexandra.fresco.suite.spdz;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.lib.lp.LPBuildingBlockTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

public class TestSpdzLPBuildingBlocks {	
	
	private static final int noOfParties = 2;
	private Map<Integer, TestThreadConfiguration> conf; 
	
	private void configure(EvaluationStrategy evalStrategy,
			StorageStrategy storageStrategy) {
		Level logLevel = Level.FINE;
		Reporter.init(logLevel);
		// Since SCAPI currently does not work with ports > 9999 we use fixed
		// ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(noOfParties);
		for (int i = 1; i <= noOfParties; i++) {
			ports.add(9000 + i);
		}

		Map<Integer, NetworkConfiguration> netConf = TestConfiguration
				.getNetworkConfigurations(noOfParties, ports, logLevel);
		conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);
			
			SpdzConfiguration spdzConf = new SpdzConfiguration() {
				
				@Override
				public PreprocessingStrategy getPreprocessingStrategy() {
					return PreprocessingStrategy.DUMMY;
				}

				@Override
				public String fuelStationBaseUrl() {
					return null;
				}
				
				@Override
				public int getMaxBitLength() {
					return 150;
				}
			};
			ttc.protocolSuiteConf = spdzConf;
			boolean useSecureConnection = false; // No tests of secure
													// connection
													// here.
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			ProtocolSuite suite = SpdzProtocolSuite.getInstance(playerId);
			ProtocolEvaluator evaluator = EvaluationStrategy
					.fromEnum(evalStrategy);
			dk.alexandra.fresco.framework.sce.resources.storage.Storage storage = inMemStore;			
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator,
					noOfThreads, noOfVMThreads, ttc.netConf, storage,
					useSecureConnection);
			conf.put(playerId, ttc);
		}
	}

	private void runTest(TestThreadFactory f) throws Exception {
		TestThreadRunner.run(f, conf);
	}

	private static InMemoryStorage inMemStore = new InMemoryStorage();

	@Test
	public void test_Exiting_Variable_Sequential() throws Exception {
		configure(EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
		runTest(new LPBuildingBlockTests.TestDummy());
		BigInteger mod = SpdzProtocolSuite.getInstance(1).getModulus();
		runTest(new LPBuildingBlockTests.TestDanzigEnteringVariable(mod));
	}
	
	@Test
	public void test_Exiting_Variable_Parallel() throws Exception {
		configure(EvaluationStrategy.PARALLEL, StorageStrategy.IN_MEMORY);
		runTest(new LPBuildingBlockTests.TestDummy());
		BigInteger mod = SpdzProtocolSuite.getInstance(1).getModulus();
		runTest(new LPBuildingBlockTests.TestDanzigEnteringVariable(mod));
	}
	
	@Test
	public void test_Exiting_Variable_Sequential_Batched() throws Exception {
		configure(EvaluationStrategy.PARALLEL_BATCHED, StorageStrategy.IN_MEMORY);
		runTest(new LPBuildingBlockTests.TestDummy());
		BigInteger mod = SpdzProtocolSuite.getInstance(1).getModulus();
		runTest(new LPBuildingBlockTests.TestDanzigEnteringVariable(mod));
	}
	
	@Test
	public void test_Exiting_Variable_Parallel_Batched() throws Exception {
		configure(EvaluationStrategy.PARALLEL_BATCHED, StorageStrategy.IN_MEMORY);
		runTest(new LPBuildingBlockTests.TestDummy());
		BigInteger mod = SpdzProtocolSuite.getInstance(1).getModulus();
		runTest(new LPBuildingBlockTests.TestDanzigEnteringVariable(mod));
	}
	
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
	
	@Ignore
	@Test
	public void tesMaxSqrt() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						BigInteger half = Util.getModulus().subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
						int bitlength = half.bitLength();
						BigInteger approxMaxSqrt = BigInteger.valueOf(2).pow(bitlength / 2);
						//System.out.println("P: " + Util.p);
						//System.out.println("Half: " + half);
						//System.out.println("Approxsqrt: " + approxMaxSqrt);
						//System.out.println("Approxsqrt^2: " + approxMaxSqrt.multiply(approxMaxSqrt));
						Assert.assertTrue(half.compareTo(approxMaxSqrt.max(approxMaxSqrt)) == 1);
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testRank() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						int numbers = 100;
						int actualRank = rand.nextInt(100);
						BigInteger[] numerators = new BigInteger[numbers];
						BigInteger[] denominators = new BigInteger[numbers];
						for (int i = 0; i < numbers; i++) {
							numerators[(numbers - 1) - i] = BigInteger.ONE;
							denominators[(numbers - 1) - i] = BigInteger.valueOf(i + 1);
						}
						BigInteger numerator = BigInteger.valueOf(9900  + actualRank);
						BigInteger denominator = BigInteger.valueOf((100 - actualRank)*10000);
						
						SInt[] nums = sIntFill(new SInt[numbers], provider);
						SInt[] dens = sIntFill(new SInt[numbers], provider);
						SInt num = provider.getSInt();
						SInt den = provider.getSInt();
						ProtocolProducer inp1 = new ParallelGateProducer(makeInputGates(numerators, nums, provider));
						ProtocolProducer inp2 = new ParallelGateProducer(makeInputGates(denominators, dens, provider));
						ProtocolProducer inp3 = new ParallelGateProducer(makeInputGates(new BigInteger[]{numerator, denominator}, 
								new SInt[]{num, den}, provider));
						ProtocolProducer input = new ParallelGateProducer(inp1, inp2, inp3);
						SInt rank = provider.getSInt();
						ProtocolProducer rankCircuit = provider.getRankCircuit(nums, dens, num, den, rank);
						OInt out = provider.getOInt();
						ProtocolProducer output = provider.getOpenIntCircuit(rank, out);
						
						double d = numerator.doubleValue() / denominator.doubleValue();
						double over = numerators[actualRank].doubleValue() / denominators[actualRank].doubleValue();
						double under = numerators[actualRank - 1].doubleValue() / denominators[actualRank - 1].doubleValue();
						
						
						System.out.println(under + " < " + d + " < " + over);
						
						ProtocolProducer gp = new SequentialProtocolProducer(input, rankCircuit, output);
						
						sce.runApplication(gp);
						
						Assert.assertEquals(actualRank, out.getValue().intValue());
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testRankRandom() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						for (int j = 0; j < 20; j++) {
							int numbers = 20;
							BigInteger[] numerators = new BigInteger[numbers];
							BigInteger[] denominators = new BigInteger[numbers];
							for (int i = 0; i < numbers; i++) {
								numerators[(numbers - 1) - i] = new BigInteger(20, rand);
								denominators[(numbers - 1) - i] = new BigInteger(20, rand);
							}
							BigInteger numerator = new BigInteger(20, rand);
							BigInteger denominator = new BigInteger(20, rand);
							
							int count = 0;
							for (int i = 0; i < numbers; i++) {
								BigInteger product1 = numerators[i].multiply(denominator); 
								BigInteger product2 = denominators[i].multiply(numerator);
								if (product1.compareTo(product2) <= 0) {
									count++;
								}
							}							
							
							SInt[] nums = sIntFill(new SInt[numbers], provider);
							SInt[] dens = sIntFill(new SInt[numbers], provider);
							SInt num = provider.getSInt();
							SInt den = provider.getSInt();
							ProtocolProducer inp1 = new ParallelGateProducer(makeInputGates(numerators, nums, provider));
							ProtocolProducer inp2 = new ParallelGateProducer(makeInputGates(denominators, dens, provider));
							ProtocolProducer inp3 = new ParallelGateProducer(makeInputGates(new BigInteger[]{numerator, denominator}, 
									new SInt[]{num, den}, provider));
							ProtocolProducer input = new ParallelGateProducer(inp1, inp2, inp3);
							SInt rank = provider.getSInt();
							ProtocolProducer rankCircuit = provider.getRankCircuit(nums, dens, num, den, rank);
							OInt out = provider.getOInt();
							ProtocolProducer output = provider.getOpenIntCircuit(rank, out);
							
							ProtocolProducer gp = new SequentialProtocolProducer(input, rankCircuit, output);

							sce.runApplication(gp);
							

							Assert.assertEquals(count, out.getValue().intValue());
						}
						
					}
				};
			}
		}, 2);
	}
	
	@Test 
	public void testComparisonCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						 
						{
							BigInteger valueA = BigInteger.valueOf(10);
							BigInteger valueB = BigInteger.valueOf(20);						
						
							SInt a = provider.getSInt();
							SInt b = provider.getSInt();
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();
	
							SpdzInputGate inputGateA = (SpdzInputGate)provider.getInputCircuit(valueA, a, 1);
							SpdzInputGate inputGateB = (SpdzInputGate)provider.getInputCircuit(valueB, b, 1);
							ComparisonCircuit cs = provider.getComparisonCircuit(a, b, result, false);
							OpenCircuit<SInt, OInt> outputGate =  provider.getOpenIntCircuit(result, output);
	
							sce.runApplication(new SequentialProtocolProducer(inputGateA, inputGateB, cs, outputGate));
	
							Assert.assertEquals(BigInteger.ONE, ((SpdzOInt)output).getValue());
						}
						System.out.println("Test one DONE");
						{
							BigInteger valueA = BigInteger.valueOf(10);
							BigInteger valueB = BigInteger.valueOf(20);						
						
							SInt a = provider.getSInt();
							SInt b = provider.getSInt();
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();

							SpdzInputGate inputGateA = (SpdzInputGate)provider.getInputCircuit(valueA, a, 1);
							SpdzInputGate inputGateB = (SpdzInputGate)provider.getInputCircuit(valueB, b, 1);
							ComparisonCircuit cs = provider.getComparisonCircuit(b, a, result, false); //compare b to a
							OpenCircuit<SInt, OInt> outputGate =  provider.getOpenIntCircuit(result, output);

							sce.runApplication(new SequentialProtocolProducer(inputGateA, inputGateB, cs, outputGate));

							Assert.assertEquals(BigInteger.ZERO, ((SpdzOInt)output).getValue());
						}
						System.out.println("Test two DONE");
						{
							BigInteger valueA = BigInteger.valueOf(10);
							BigInteger valueB = BigInteger.valueOf(-20).mod(Util.getModulus());						
						
							SInt a = provider.getSInt();
							SInt b = provider.getSInt();
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();

							SpdzInputGate inputGateA = (SpdzInputGate)provider.getInputCircuit(valueA, a, 1);
							SpdzInputGate inputGateB = (SpdzInputGate)provider.getInputCircuit(valueB, b, 1);
							ComparisonCircuit cs = provider.getComparisonCircuit(a, b, result, false); //compare a to b
							OpenCircuit<SInt, OInt> outputGate =  provider.getOpenIntCircuit(result, output);

							sce.runApplication(new SequentialProtocolProducer(inputGateA, inputGateB, cs, outputGate));
							System.out.println(valueA);
							System.out.println(valueB);
							Assert.assertEquals(BigInteger.ZERO, ((SpdzOInt)output).getValue());
						}
						System.out.println("Test three DONE");
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testDotProductCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						 
						int noOfGates = 32000;
						BigInteger[] inputValues = new BigInteger[noOfGates];
						SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
						SInt[] as = new SInt[noOfGates];
						SInt[] results = new SInt[noOfGates];
						OInt[] outputs = new OInt[noOfGates];
						OpenCircuit<SInt, OInt>[] outputGates = new SpdzOutputToAllGate[noOfGates];
						for(int i = 0; i < noOfGates; i++) {
							inputValues[i] = BigInteger.valueOf(i+1);
							as[i] = provider.getSInt();
							results[i] = provider.getSInt();
							outputs[i] = provider.getOInt();
							inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
							outputGates[i] = provider.getOpenIntCircuit(results[i], outputs[i]);
						}						
						DotProductCircuit dpc = provider.getDotProductCircuit(as, as, results);
						ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
						ProtocolProducer dotProductGP = new ParallelGateProducer(dpc);
						ProtocolProducer outputGP = new ParallelGateProducer(outputGates);
						sce.runApplication(new SequentialProtocolProducer(inputGP, dotProductGP, outputGP));
						
						for(int i = 0; i < noOfGates; i++){
							Assert.assertEquals(inputValues[i].multiply(inputValues[i]), ((SpdzOInt)outputs[i]).getValue());
						}
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testDotProductCircuit2() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						 
						int noOfGates = 50000;
						BigInteger[] inputValues = new BigInteger[noOfGates];
						SInt[] as = new SInt[noOfGates];
						SInt[] results = new SInt[noOfGates];
						OInt[] outputs = new OInt[noOfGates];
						OpenCircuit<SInt, OInt>[] outputGates = new SpdzOutputToAllGate[noOfGates];
						for(int i = 0; i < noOfGates; i++){
							inputValues[i] = BigInteger.valueOf(i+1);
							as[i] = provider.getSInt(inputValues[i]);
							results[i] = provider.getSInt();
							outputs[i] = provider.getOInt();
							outputGates[i] = provider.getOpenIntCircuit(results[i], outputs[i]);
						}						
						DotProductCircuit dpc = provider.getDotProductCircuit(as, as, results);
						ProtocolProducer dotProductGP = new ParallelGateProducer(dpc);
						ProtocolProducer outputGP = new ParallelGateProducer(outputGates);
						sce.runApplication(new SequentialProtocolProducer(dotProductGP, outputGP));
						
						for(int i = 0; i < noOfGates; i++){
							Assert.assertEquals(inputValues[i].multiply(inputValues[i]), ((SpdzOInt)outputs[i]).getValue());
						}
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testInnerProductCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						 
						{
		
							final int noOfGates = 10000;
							final BigInteger[] inputValues = new BigInteger[noOfGates];
							final SInt[] as = new SInt[noOfGates];

							SInt result = provider.getSInt();
							OInt output = provider.getOInt();
							OpenCircuit<SInt, OInt> outputGates = provider.getOpenIntCircuit(result, output); 
							ProtocolProducer parallelInput = new AbstractRepeatCircuit() {
								
								int i = 0;							
								
								@Override
								protected ProtocolProducer getNextGateProducer() {
									if (i < noOfGates) {
										inputValues[i] = BigInteger.valueOf(i+1);
										as[i] = provider.getSInt();
										ProtocolProducer input = provider.getInputCircuit(inputValues[i], as[i], 1);
										i++;
										return input;
									} else {
										return null;
									}
								}
							};
							InnerProductCircuit ipc = provider.getInnerProductCircuit(as, as, result);
							ProtocolProducer innerProductGP = new SequentialProtocolProducer(ipc);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);

							sce.runApplication(new SequentialProtocolProducer(parallelInput, innerProductGP, outputGP));

							BigInteger innerproduct = inputValues[0].multiply(inputValues[0]);
							for(int i = 1; i < noOfGates; i++){
								innerproduct = innerproduct.add(inputValues[i].multiply(inputValues[i]));
							}
							Assert.assertEquals(innerproduct, output.getValue());
						}					
						
						for(int count=0; count < 15; count++){
							int dimension= 10;
							BigInteger[] aInputValues = new BigInteger[dimension];
							BigInteger[] bInputValues = new BigInteger[dimension];
							aInputValues = randomFill(aInputValues, rand);
							bInputValues = randomFill(bInputValues, rand);							
							
							SInt[] as = new SInt[dimension];
							SInt[] bs = new SInt[dimension];
							as = sIntFill(as, provider);
							bs = sIntFill(bs,provider);
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();
							
							ProtocolProducer aIn = new ParallelGateProducer(makeInputGates(aInputValues, as, provider));
							ProtocolProducer bIn = new ParallelGateProducer(makeInputGates(bInputValues, bs, provider));
							OpenCircuit<SInt, OInt> outputGate = provider.getOpenIntCircuit(result, output); 
							
							
							InnerProductCircuit ipc = provider.getInnerProductCircuit(as, bs, result);
							ProtocolProducer inputGP = new ParallelGateProducer(aIn, bIn);
							ProtocolProducer innerProductGP = new SequentialProtocolProducer(ipc);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGate);


							sce.runApplication(new SequentialProtocolProducer(inputGP, innerProductGP, outputGP));

							BigInteger innerproduct = aInputValues[0].multiply(bInputValues[0]);
							for(int i = 1; i < dimension; i++){
								innerproduct = innerproduct.add(aInputValues[i].multiply(bInputValues[i])).mod(Util.getModulus());
							}
							Assert.assertEquals(Util.convertRepresentation(innerproduct), output.getValue());
						}
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testAltInnerProductCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						 
						{
							int noOfGates = 10;
							BigInteger[] inputValues = new BigInteger[noOfGates];
							SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
							SInt[] as = new SInt[noOfGates];
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();
							OpenCircuit<SInt, OInt> outputGates = provider.getOpenIntCircuit(result, output); 
							for(int i = 0; i < noOfGates; i++){
								inputValues[i] = BigInteger.valueOf(i+1);
								as[i] = provider.getSInt();
								inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
							}						
							InnerProductCircuit ipc = new AltInnerProductCircuitImpl(as, as, result, provider, provider);
							ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
							ProtocolProducer innerProductGP = new SequentialProtocolProducer(ipc);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);


							sce.runApplication(new SequentialProtocolProducer(inputGP, innerProductGP, outputGP));

							BigInteger innerproduct = inputValues[0].multiply(inputValues[0]);
							for(int i = 1; i < noOfGates; i++){
								innerproduct = innerproduct.add(inputValues[i].multiply(inputValues[i]));
							}
							Assert.assertEquals(innerproduct, output.getValue());
						}						
						
						for(int count=0; count < 5; count++){
							int dimension= 10;
							BigInteger[] aInputValues = new BigInteger[dimension];
							BigInteger[] bInputValues = new BigInteger[dimension];
							aInputValues = randomFill(aInputValues, rand);
							bInputValues = randomFill(bInputValues, rand);
							
							
							
							SInt[] as = new SInt[dimension];
							SInt[] bs = new SInt[dimension];
							as = sIntFill(as, provider);
							bs = sIntFill(bs,provider);
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();
							
							ProtocolProducer aIn = new ParallelGateProducer(makeInputGates(aInputValues, as, provider));
							ProtocolProducer bIn = new ParallelGateProducer(makeInputGates(bInputValues, bs, provider));
							OpenCircuit<SInt, OInt> outputGate = provider.getOpenIntCircuit(result, output); 
							
							
							InnerProductCircuit ipc = new AltInnerProductCircuitImpl(as, bs, result, provider, provider);
							ProtocolProducer inputGP = new ParallelGateProducer(aIn, bIn);
							ProtocolProducer innerProductGP = new SequentialProtocolProducer(ipc);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGate);


							sce.runApplication(new SequentialProtocolProducer(inputGP, innerProductGP, outputGP));

							BigInteger innerproduct = aInputValues[0].multiply(bInputValues[0]);
							for(int i = 1; i < dimension; i++){
								innerproduct = innerproduct.add(aInputValues[i].multiply(bInputValues[i])).mod(Util.getModulus());
							}
							Assert.assertEquals(Util.convertRepresentation(innerproduct), output.getValue());
						}
						
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testInnerProductHalfPublicCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						 
						{
							int noOfGates = 10;
							BigInteger[] inputValues = new BigInteger[noOfGates];
							SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
							SInt[] as = new SInt[noOfGates];
							OInt[] bs = new OInt[noOfGates];
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();
							OpenCircuit<SInt, OInt> outputGates = provider.getOpenIntCircuit(result, output); 
							for(int i = 0; i < noOfGates; i++){
								inputValues[i] = BigInteger.valueOf(i+1);
								as[i] = provider.getSInt();
								bs[i] = provider.getOInt(i+1);
								inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
							}						
							InnerProductCircuit ipc = provider.getInnerProductCircuit(as, bs, result);
							ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
							ProtocolProducer innerProductGP = new SequentialProtocolProducer(ipc);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);


							sce.runApplication(new SequentialProtocolProducer(inputGP, innerProductGP, outputGP));

							BigInteger innerproduct = inputValues[0].multiply(inputValues[0]);
							for(int i = 1; i < noOfGates; i++){
								innerproduct = innerproduct.add(inputValues[i].multiply(inputValues[i]));
							}
							Assert.assertEquals(Util.convertRepresentation(innerproduct), output.getValue());
						}
//						for(int count=0; count < 15; count++){
//							int dimension= 10;
//							BigInteger[] aInputValues = new BigInteger[dimension];
//							BigInteger[] bInputValues = new BigInteger[dimension];
//							aInputValues = randomFill(aInputValues, rand);
//							bInputValues = randomFill(bInputValues, rand);
//							
//							
//							
//							SInt[] as = new SInt[dimension];
//							SInt[] bs = new SInt[dimension];
//							as = sIntFill(as, provider);
//							bs = sIntFill(bs,provider);
//							SInt result = provider.getSInt();
//							OInt output = provider.getOInt();
//							
//							GateProducer aIn = new ParallelGateProducer(makeInputGates(aInputValues, as, provider));
//							GateProducer bIn = new ParallelGateProducer(makeInputGates(bInputValues, bs, provider));
//							OpenCircuit<SInt, OInt> outputGate = provider.getOpenIntCircuit(result, output); 
//							
//							
//							InnerProductCircuit ipc = provider.getInnerProductCircuit(as, bs, result);
//							GateProducer inputGP = new ParallelGateProducer(aIn, bIn);
//							GateProducer innerProductGP = new SequentialGateProducer(ipc);
//							GateProducer outputGP = new ParallelGateProducer(outputGate);
//
//
//							sce.runApplication(new SequentialGateProducer(inputGP, innerProductGP, outputGP));
//
//							BigInteger innerproduct = aInputValues[0].multiply(bInputValues[0]);
//							for(int i = 1; i < dimension; i++){
//								innerproduct = innerproduct.add(aInputValues[i].multiply(bInputValues[i])).mod(Util.p);
//							}
//							Assert.assertEquals(innerproduct, output.getValue());
//						}
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testMinimumFractionCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						{
							int noOfFractions = 2;
							int bitLength = 20;
							BigInteger[] nValues = new BigInteger[noOfFractions];
							BigInteger[] dValues = new BigInteger[noOfFractions];
							SInt[] ns = new SInt[noOfFractions];
							SInt[] ds = new SInt[noOfFractions];
							SInt[] cs = new SInt[noOfFractions];
							OInt[] outputs = new OInt[noOfFractions+2];							
														
							ns = sIntFill(ns, provider);
							ds = sIntFill(ds, provider);
							cs = sIntFill(cs, provider);
							SInt nm = provider.getSInt();
							SInt dm = provider.getSInt();
							outputs = oIntFill(outputs, provider);
							for(int i = 0; i < noOfFractions; i++){
								nValues[i] = (new BigInteger(bitLength, rand)).subtract(BigInteger.valueOf((long)Math.pow(2, bitLength - 1)));
								dValues[i] = (new BigInteger(bitLength-1, rand));
							}
							int minPos = rand.nextInt(noOfFractions);
							nValues[minPos] = BigInteger.valueOf((long)(1 - Math.pow(2, bitLength - 1)));
							dValues[minPos] = BigInteger.valueOf(1);
							ProtocolProducer nInput = new ParallelGateProducer(makeInputGates(nValues, ns, provider));
							ProtocolProducer dInput = new ParallelGateProducer(makeInputGates(dValues, ds, provider));							
							
							MinimumFractionCircuit min = provider.getMinimumFractionCircuit(ns, ds, nm, dm, cs);
							ProtocolProducer inputGP = new ParallelGateProducer(nInput, dInput);
							ProtocolProducer minGP = new ParallelGateProducer(min);							
							SInt[] allResults = (SInt[]) ArrayUtils.addAll(new SInt[]{nm, dm}, cs);
							ProtocolProducer outputGP = makeOpenCircuit(allResults, outputs, provider);
							
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));
							if (conf.getMyId() == 1) {
								int index = 0;
								int count = 0;
								for (int i = 0; i < noOfFractions; i++) {
									 count += outputs[i+2].getValue().intValue();
									 if (count < 1) {
										 index++;
									 }
								}
								
								Assert.assertEquals(1, count);
								Assert.assertEquals(Util.convertRepresentation(nValues[index]), outputs[0].getValue());
								Assert.assertEquals(Util.convertRepresentation(dValues[index]), outputs[1].getValue());
								Assert.assertEquals(minPos, index);
							}
						}
						{
							int noOfFractions = 3;
							int bitLength = 20;
							BigInteger[] nValues = new BigInteger[noOfFractions];
							BigInteger[] dValues = new BigInteger[noOfFractions];
							SInt[] ns = new SInt[noOfFractions];
							SInt[] ds = new SInt[noOfFractions];
							SInt[] cs = new SInt[noOfFractions];
							OInt[] outputs = new OInt[noOfFractions+2];
																					
							ns = sIntFill(ns, provider);
							ds = sIntFill(ds, provider);
							cs = sIntFill(cs, provider);
							SInt nm = provider.getSInt();
							SInt dm = provider.getSInt();
							outputs = oIntFill(outputs, provider);
							for(int i = 0; i < noOfFractions; i++){
								nValues[i] = (new BigInteger(bitLength, rand)).subtract(BigInteger.valueOf((long)Math.pow(2, bitLength - 1)));
								dValues[i] = (new BigInteger(bitLength - 1, rand));
							}
							int minPos = rand.nextInt(noOfFractions);
							nValues[minPos] = BigInteger.valueOf((long)(1 - Math.pow(2, bitLength)));
							dValues[minPos] = BigInteger.valueOf(1);
							ProtocolProducer nInput = new ParallelGateProducer(makeInputGates(nValues, ns, provider));
							ProtocolProducer dInput = new ParallelGateProducer(makeInputGates(dValues, ds, provider));
							
							MinimumFractionCircuit min = provider.getMinimumFractionCircuit(ns, ds, nm, dm, cs);
							ProtocolProducer inputGP = new ParallelGateProducer(nInput, dInput);
							ProtocolProducer minGP = new ParallelGateProducer(min);							
							SInt[] allResults = (SInt[]) ArrayUtils.addAll(new SInt[]{nm, dm}, cs);
							ProtocolProducer outputGP = makeOpenCircuit(allResults, outputs, provider);
							
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));

							if (conf.getMyId() == 1) {
								int index = 0;
								int count = 0;
								for (int i = 0; i < noOfFractions; i++) {
									 count += outputs[i+2].getValue().intValue();
									 if (count < 1) {
										 index++;
									 }
								}
								
								Assert.assertEquals(1, count);
								Assert.assertEquals(Util.convertRepresentation(nValues[index]), outputs[0].getValue());
								Assert.assertEquals(Util.convertRepresentation(dValues[index]), outputs[1].getValue());
								Assert.assertEquals(minPos, index);
							}
						}
						{
							int noOfFractions = 50;
							int bitLength = 20;
							BigInteger[] nValues = new BigInteger[noOfFractions];
							BigInteger[] dValues = new BigInteger[noOfFractions];
							SInt[] ns = new SInt[noOfFractions];
							SInt[] ds = new SInt[noOfFractions];
							SInt[] cs = new SInt[noOfFractions];
							OInt[] outputs = new OInt[noOfFractions+2];
							
							ns = sIntFill(ns, provider);
							ds = sIntFill(ds, provider);
							cs = sIntFill(cs, provider);
							SInt nm = provider.getSInt();
							SInt dm = provider.getSInt();
							outputs = oIntFill(outputs, provider);
							for(int i = 0; i < noOfFractions; i++){
								nValues[i] = (new BigInteger(bitLength, rand)).subtract(BigInteger.valueOf((long)Math.pow(2, bitLength - 1)));
								dValues[i] = (new BigInteger(bitLength - 1, rand));
							}
							int minPos = rand.nextInt(noOfFractions);
							nValues[minPos] = BigInteger.valueOf((long)(1 - Math.pow(2, bitLength - 1)));
							dValues[minPos] = BigInteger.valueOf(1);
							ProtocolProducer nInput = new ParallelGateProducer(makeInputGates(nValues, ns, provider));
							ProtocolProducer dInput = new ParallelGateProducer(makeInputGates(dValues, ds, provider));
							
							MinimumFractionCircuit min = provider.getMinimumFractionCircuit(ns, ds, nm, dm, cs);
							ProtocolProducer inputGP = new ParallelGateProducer(nInput, dInput);
							ProtocolProducer minGP = new ParallelGateProducer(min);							
							SInt[] allResults = (SInt[]) ArrayUtils.addAll(new SInt[]{nm, dm}, cs);
							ProtocolProducer outputGP = makeOpenCircuit(allResults, outputs, provider);
							
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));

							if (conf.getMyId() == 1) {
								int index = 0;
								int count = 0;
								for (int i = 0; i < noOfFractions; i++) {
									 count += outputs[i+2].getValue().intValue();
									 if (count < 1) {
										 index++;
									 }
								}
								
								Assert.assertEquals(1, count);
								Assert.assertEquals(Util.convertRepresentation(nValues[index]), outputs[0].getValue());
								Assert.assertEquals(Util.convertRepresentation(dValues[index]), outputs[1].getValue());
								Assert.assertEquals(minPos, index);
							}
						}
						{
							int noOfFractions = 100;
							int bitLength = 20;
							BigInteger[] nValues = new BigInteger[noOfFractions];
							BigInteger[] dValues = new BigInteger[noOfFractions];
							SInt[] ns = new SInt[noOfFractions];
							SInt[] ds = new SInt[noOfFractions];
							SInt[] cs = new SInt[noOfFractions];
							OInt[] outputs = new OInt[noOfFractions+2];
							
							ns = sIntFill(ns, provider);
							ds = sIntFill(ds, provider);
							cs = sIntFill(cs, provider);
							SInt nm = provider.getSInt();
							SInt dm = provider.getSInt();
							outputs = oIntFill(outputs, provider);
							for(int i = 0; i < noOfFractions; i++){
								nValues[i] = (new BigInteger(bitLength, rand)).subtract(BigInteger.valueOf((long)Math.pow(2, bitLength -1)));
								dValues[i] = (new BigInteger(bitLength - 1, rand));
							}
							int minPos = rand.nextInt(noOfFractions);
							nValues[minPos] = BigInteger.valueOf((long)(1 - Math.pow(2, bitLength - 1)));
							dValues[minPos] = BigInteger.valueOf(1);
							ProtocolProducer nInput = new ParallelGateProducer(makeInputGates(nValues, ns, provider));
							ProtocolProducer dInput = new ParallelGateProducer(makeInputGates(dValues, ds, provider));
							
							MinimumFractionCircuit min = provider.getMinimumFractionCircuit(ns, ds, nm, dm, cs);
							ProtocolProducer inputGP = new ParallelGateProducer(nInput, dInput);
							ProtocolProducer minGP = new ParallelGateProducer(min);							
							SInt[] allResults = (SInt[]) ArrayUtils.addAll(new SInt[]{nm, dm}, cs);
							ProtocolProducer outputGP = makeOpenCircuit(allResults, outputs, provider);
							
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));

							if (conf.getMyId() == 1) {
								int index = 0;
								int count = 0;
								for (int i = 0; i < noOfFractions; i++) {
									 count += outputs[i+2].getValue().intValue();
									 if (count < 1) {
										 index++;
									 }
								}
								
								Assert.assertEquals(1, count);
								Assert.assertEquals(Util.convertRepresentation(nValues[index]), outputs[0].getValue());
								Assert.assertEquals(Util.convertRepresentation(dValues[index]), outputs[1].getValue());
								Assert.assertEquals(minPos, index);
							}
						}
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testMinimumCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						{
							int noOfGates = 2;
							BigInteger[] inputValues = new BigInteger[noOfGates];
							SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
							SInt[] as = new SInt[noOfGates];
							SInt[] results = new SInt[noOfGates];
							OInt[] outputs = new OInt[noOfGates];
							OInt mOutput = provider.getOInt();
							BigInteger minVal = BigInteger.valueOf(8);
							OpenCircuit<SInt, OInt>[] outputGates = new SpdzOutputToAllGate[noOfGates+1];
							for(int i = 0; i < noOfGates; i++){
								inputValues[i] = BigInteger.valueOf(10);
								if(i == 1){
									inputValues[i] = minVal; //this should be output
								}
								as[i] = provider.getSInt();
								results[i] = provider.getSInt();
								outputs[i] = provider.getOInt();
								inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
								outputGates[i] = provider.getOpenIntCircuit(results[i], outputs[i]);
							}						
							SInt m = provider.getSInt();
							outputGates[noOfGates] = provider.getOpenIntCircuit(m, mOutput);
							MinimumCircuit min = provider.getMinimumCircuit(as, m, results);
							ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
							ProtocolProducer minGP = new ParallelGateProducer(min);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));
	
							assertEquals(Util.convertRepresentation(minVal), mOutput.getValue());
							System.out.println("Passed minimim with 2 inputs");
						}
						{
							int noOfGates = 2;
							BigInteger[] inputValues = new BigInteger[noOfGates];
							SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
							SInt[] as = new SInt[noOfGates];
							SInt[] results = new SInt[noOfGates];
							OInt[] outputs = new OInt[noOfGates];
							OInt mOutput = provider.getOInt();
							BigInteger minVal = BigInteger.valueOf(-8);
							OpenCircuit<SInt, OInt>[] outputGates = new SpdzOutputToAllGate[noOfGates+1];
							for(int i = 0; i < noOfGates; i++){
								inputValues[i] = BigInteger.valueOf(10);
								if(i == 1){
									inputValues[i] = minVal; //this should be output
								}
								as[i] = provider.getSInt();
								results[i] = provider.getSInt();
								outputs[i] = provider.getOInt();
								inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
								outputGates[i] = provider.getOpenIntCircuit(results[i], outputs[i]);
							}						
							SInt m = provider.getSInt();
							outputGates[noOfGates] = provider.getOpenIntCircuit(m, mOutput);
							MinimumCircuit min = provider.getMinimumCircuit(as, m, results);
							ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
							ProtocolProducer minGP = new ParallelGateProducer(min);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));
	
							assertEquals(Util.convertRepresentation(minVal), mOutput.getValue());
							System.out.println("Passed minimim with 2 inputs and negative mininum value");
						}
						{
							int noOfGates = 3;
							BigInteger[] inputValues = new BigInteger[noOfGates];
							SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
							SInt[] as = new SInt[noOfGates];
							SInt[] results = new SInt[noOfGates];
							OInt[] outputs = new OInt[noOfGates];
							OInt mOutput = provider.getOInt();
							BigInteger minVal = BigInteger.valueOf(8);
							OpenCircuit<SInt, OInt>[] outputGates = new SpdzOutputToAllGate[noOfGates+1];
							for(int i = 0; i < noOfGates; i++){
								inputValues[i] = BigInteger.valueOf(10);
								if(i == 2){
									inputValues[i] = minVal; //this should be output
								}
								as[i] = provider.getSInt();
								results[i] = provider.getSInt();
								outputs[i] = provider.getOInt();
								inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
								outputGates[i] = provider.getOpenIntCircuit(results[i], outputs[i]);
							}						
							SInt m = provider.getSInt();
							outputGates[noOfGates] = provider.getOpenIntCircuit(m, mOutput);
							MinimumCircuit min = provider.getMinimumCircuit(as, m, results);
							ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
							ProtocolProducer minGP = new ParallelGateProducer(min);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));
	
							assertEquals(mOutput.getValue(), Util.convertRepresentation(minVal));
							System.out.println("Passed minimim with 3 inputs");
						}
						{

							int noOfGates = 100;
							BigInteger[] inputValues = new BigInteger[noOfGates];
							SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
							SInt[] as = new SInt[noOfGates];
							SInt[] results = new SInt[noOfGates];
							OInt[] outputs = new OInt[noOfGates];
							OInt mOutput = provider.getOInt();
							OpenCircuit<SInt, OInt>[] outputGates = new SpdzOutputToAllGate[noOfGates+1];
							for(int i = 0; i < noOfGates; i++){
								inputValues[i] = new BigInteger(32, rand);								
								as[i] = provider.getSInt();
								results[i] = provider.getSInt();
								outputs[i] = provider.getOInt();
								inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
								outputGates[i] = provider.getOpenIntCircuit(results[i], outputs[i]);
							}						
							SInt m = provider.getSInt();
							outputGates[noOfGates] = provider.getOpenIntCircuit(m, mOutput);
							MinimumCircuit min = provider.getMinimumCircuit(as, m, results);
							ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
							ProtocolProducer minGP = new ParallelGateProducer(min);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));

							if (conf.getMyId() == 1) {
								assertEquals(mOutput.getValue(), Util.convertRepresentation(min(inputValues)));
								System.out.println("Passed minimim with " + noOfGates + " inputs in interval 0 - 2^32");
							}
						}
						{
							int noOfGates = 100;
							BigInteger[] inputValues = new BigInteger[noOfGates];
							SpdzInputGate[] inputGates = new SpdzInputGate[noOfGates];
							SInt[] as = new SInt[noOfGates];
							SInt[] results = new SInt[noOfGates];
							OInt[] outputs = new OInt[noOfGates];
							OInt mOutput = provider.getOInt();
							OpenCircuit<SInt, OInt>[] outputGates = new SpdzOutputToAllGate[noOfGates+1];
							for(int i = 0; i < noOfGates; i++){
								inputValues[i] = (new BigInteger(32, rand)).subtract(BigInteger.valueOf((long)Math.pow(2, 31)));								
								as[i] = provider.getSInt();
								results[i] = provider.getSInt();
								outputs[i] = provider.getOInt();
								inputGates[i] = (SpdzInputGate)provider.getInputCircuit(inputValues[i], as[i], 1);
								outputGates[i] = provider.getOpenIntCircuit(results[i], outputs[i]);
							}						
							SInt m = provider.getSInt();
							outputGates[noOfGates] = provider.getOpenIntCircuit(m, mOutput);
							MinimumCircuit min = provider.getMinimumCircuit(as, m, results);
							ProtocolProducer inputGP = new ParallelGateProducer(inputGates);
							ProtocolProducer minGP = new ParallelGateProducer(min);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGates);
							sce.runApplication(new SequentialProtocolProducer(inputGP, minGP, outputGP));

							if (conf.getMyId() == 1) {
								assertEquals(mOutput.getValue(), Util.convertRepresentation(min(inputValues)));
								System.out.println("Passed minimim with " + noOfGates + " inputs in interval -2^31 - 2^31");
							}
							

						}
						
						
					}
				};
			}
		}, 2);
	}
	
	private BigInteger min(BigInteger[] values){
		BigInteger min = values[0];
		for(BigInteger b: values){
			if(compareModP(b,min) < 0){
				min = b;
			}
		}
		return min;
	}
	
	@Test
	public void testConditionalSelectCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
 					@Override
					public void test() throws Exception {
						 
						{
							BigInteger valueA = BigInteger.valueOf(10);
							BigInteger valueB = BigInteger.valueOf(20);
							BigInteger valueSelector = BigInteger.valueOf(0); // select b						
						
							SInt a = provider.getSInt();
							SInt b = provider.getSInt();
							SInt selector = provider.getSInt();
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();

							SpdzInputGate inputGateA = (SpdzInputGate)provider.getInputCircuit(valueA, a, 1);
							SpdzInputGate inputGateB = (SpdzInputGate)provider.getInputCircuit(valueB, b, 1);
							SpdzInputGate inputGateSelector = (SpdzInputGate)provider.getInputCircuit(valueSelector, selector, 1);
							ConditionalSelectCircuit cs = provider.getConditionalSelectCircuit(selector, a, b, result);
							OpenCircuit<SInt, OInt> outputGate =  provider.getOpenIntCircuit(result, output);

							ProtocolProducer inputGP = new ParallelGateProducer(inputGateA, inputGateB, inputGateSelector);
							ProtocolProducer conditionalSelectGP = new ParallelGateProducer(cs);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGate);

							sce.runApplication(new SequentialProtocolProducer(inputGP, conditionalSelectGP, outputGP));


							Assert.assertEquals(valueB, ((SpdzOInt)output).getValue());
						}
						
						{
							BigInteger valueA = BigInteger.valueOf(10);
							BigInteger valueB = BigInteger.valueOf(20);
							BigInteger valueSelector = BigInteger.valueOf(1); // select a
							
							SInt a = provider.getSInt();
							SInt b = provider.getSInt();
							SInt selector = provider.getSInt();
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();

							SpdzInputGate inputGateA = (SpdzInputGate)provider.getInputCircuit(valueA, a, 1);
							SpdzInputGate inputGateB = (SpdzInputGate)provider.getInputCircuit(valueB, b, 1);
							SpdzInputGate inputGateSelector = (SpdzInputGate)provider.getInputCircuit(valueSelector, selector, 1);
							ConditionalSelectCircuit cs = provider.getConditionalSelectCircuit(selector, a, b, result);
							OpenCircuit<SInt, OInt> outputGate =  provider.getOpenIntCircuit(result, output);

							ProtocolProducer inputGP = new ParallelGateProducer(inputGateA, inputGateB, inputGateSelector);
							ProtocolProducer conditionalSelectGP = new ParallelGateProducer(cs);
							ProtocolProducer outputGP = new ParallelGateProducer(outputGate);

							sce.runApplication(new SequentialProtocolProducer(inputGP, conditionalSelectGP, outputGP));


							Assert.assertEquals(valueA, ((SpdzOInt)output).getValue());
						}
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testInversionCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
 					@Override
					public void test() throws Exception {
						 
						{
							BigInteger valueX = new BigInteger("3823033886");
							
							SInt x = provider.getSInt();
							SInt result = provider.getSInt();
							OInt output = provider.getOInt();

							SpdzInputGate inputGate = (SpdzInputGate)provider.getInputCircuit(valueX, x, 1);
							InversionCircuit ic = provider.getInversionCircuit(x, result);
							OpenCircuit<SInt, OInt> outputGate =  provider.getOpenIntCircuit(result, output);

							sce.runApplication(new SequentialProtocolProducer(inputGate, ic, outputGate));
							
							Assert.assertEquals(valueX.modInverse(Util.getModulus()), ((SpdzOInt)output).getValue());
						}	
						{
							for(int i = 0; i < 100; i++){
								BigInteger valueX = new BigInteger(32, rand);
								System.out.println("Inverting: " + valueX);
								SInt x = provider.getSInt();
								SInt result = provider.getSInt();
								OInt output = provider.getOInt();

								SpdzInputGate inputGate = (SpdzInputGate)provider.getInputCircuit(valueX, x, 1);
								InversionCircuit ic = provider.getInversionCircuit(x, result);
								OpenCircuit<SInt, OInt> outputGate =  provider.getOpenIntCircuit(result, output);

								sce.runApplication(new SequentialProtocolProducer(inputGate, ic, outputGate));
								
								Assert.assertEquals(Util.convertRepresentation(valueX.modInverse(Util.getModulus())), ((SpdzOInt)output).getValue());
							}
						}
						
						
						
 					}
				};
			}
		}, 2);
	}	
	
	@Test
	public void testLPEnteringVariableCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						{
							int m = 3;
							int n = 4;
							BigInteger[][] umValues = new BigInteger[m+1][m+1];
							umValues = zeroFill(umValues);
							BigInteger[][] cValues = new BigInteger[m][n+m];
							cValues = zeroFill(cValues);
							OInt[] outputs = new OInt[n+m];
							BigInteger[] bValues = new BigInteger[m];
							bValues = zeroFill(bValues);
							BigInteger[] fValues = new BigInteger[n+m];
							fValues = zeroFill(fValues);

							Matrix<BigInteger> updateMatrixValues = new Matrix<BigInteger>(umValues);
							Matrix<BigInteger> constraintValues = new Matrix<BigInteger>(cValues);
							int index = enteringVariableIndex(constraintValues, updateMatrixValues, bValues, fValues);

							ProtocolProducer gp = setUpEnteringVariableCircuit(umValues, cValues, bValues, fValues, outputs, provider);

							sce.runApplication(gp);

							int enteringIndex = -1;
							int sum = 0;
							for (int i = 0; i < outputs.length; i++) {
								int value = outputs[i].getValue().intValue();
								if (value != 0) {
									Assert.assertEquals(1, value);
									enteringIndex = i;
								}
								sum += value;
							}
							Assert.assertEquals(1, sum);
							Assert.assertEquals(index, enteringIndex);
						}
						for(int count = 0; count < 3; count++) {
							int m = 9;
							int n = 10;
							BigInteger[][] umValues = new BigInteger[m+1][m+1];
							umValues = randomFill(umValues, rand);
							BigInteger[][] cValues = new BigInteger[m][n+m];
							cValues = randomFill(cValues, rand);
							OInt[] outputs = new OInt[n+m];
							BigInteger[] bValues = new BigInteger[m];
							bValues = randomFill(bValues, rand);
							BigInteger[] fValues = new BigInteger[n+m];
							fValues = randomFill(fValues, rand);
							Matrix<BigInteger> updateMatrixValues = new Matrix<BigInteger>(umValues);
							Matrix<BigInteger> constraintValues = new Matrix<BigInteger>(cValues);
						
							int index = enteringVariableIndex(constraintValues, updateMatrixValues, bValues, fValues);
							ProtocolProducer gp = setUpEnteringVariableCircuit(umValues, cValues, bValues, fValues, outputs, provider);

							sce.runApplication(gp);

							int enteringIndex = -1;
							int sum = 0;
							for (int i = 0; i < outputs.length; i++) {
								int value = outputs[i].getValue().intValue();
								if (value != 0) {
									Assert.assertEquals(1, value);
									enteringIndex = i;
								}
								sum += value;
							}
							Assert.assertEquals(1, sum);
							if (conf.getMyId() == 1) {
								Assert.assertEquals(index, enteringIndex);
							}	
						}
						
						
					}
				};
			}
		}, 2);
	}
	
	@Test
	public void testLPBlandEnteringVariableCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						{
							int m = 3;
							int n = 4;
							BigInteger[][] umValues = new BigInteger[m+1][m+1];
							umValues = indentityMatrix(m + 1);
							BigInteger[][] cValues = new BigInteger[m][n+m];
							cValues = zeroFill(cValues);
							OInt[] outputs = new OInt[n+m];
							BigInteger[] bValues = new BigInteger[m];
							bValues = zeroFill(bValues);
							BigInteger[] fValues = new BigInteger[n+m];
							fValues = zeroFill(fValues);
							fValues[3] = BigInteger.ONE.negate();

							Matrix<BigInteger> updateMatrixValues = new Matrix<BigInteger>(umValues);
							Matrix<BigInteger> constraintValues = new Matrix<BigInteger>(cValues);
							int index = blandEnteringVariableIndex(constraintValues, updateMatrixValues, bValues, fValues);

							ProtocolProducer gp = setUpEnteringVariableCircuit(umValues, cValues, bValues, fValues, outputs, provider);

							sce.runApplication(gp);

							int enteringIndex = -1;
							int sum = 0;
							for (int i = 0; i < outputs.length; i++) {
								int value = outputs[i].getValue().intValue();
								if (value != 0) {
									Assert.assertEquals(1, value);
									enteringIndex = i;
								}
								sum += value;
							}
							Assert.assertEquals(1, sum);
							Assert.assertEquals(index, enteringIndex);
						}
						for(int count = 0; count < 3; count++) {
							int m = 9;
							int n = 10;
							BigInteger[][] umValues = indentityMatrix(m+1);
							BigInteger[][] cValues = new BigInteger[m][n+m];
							cValues = Util.zeroFill(cValues);
							OInt[] outputs = new OInt[n+m];
							BigInteger[] bValues = new BigInteger[m];
							bValues = zeroFill(bValues);
							BigInteger[] fValues = new BigInteger[n+m];
							int index = rand.nextInt(fValues.length);
							for (int i = 0; i < index; i ++) {
								fValues[i] = BigInteger.ZERO;
							}
							fValues[index] =  new BigInteger(10, rand).negate();
							for (int i = index + 1; i < fValues.length; i++) {
								BigInteger random = new BigInteger(11, rand);
								fValues[i] = random.subtract(BigInteger.valueOf(2).pow(10));
							}
							
							ProtocolProducer gp = setUpBlandEnteringVariableCircuit(umValues, cValues, bValues, fValues, outputs, provider);

							sce.runApplication(gp);

							int enteringIndex = -1;
							int sum = 0;
							for (int i = 0; i < outputs.length; i++) {
								int value = outputs[i].getValue().intValue();
								if (value != 0) {
									Assert.assertEquals(1, value);
									enteringIndex = i;
								}
								sum += value;
							}
							Assert.assertEquals(1, sum);
							Assert.assertEquals(index, enteringIndex);
						}
						
						
					}
				};
			}
		}, 2);
	}

	private ProtocolProducer setUpBlandEnteringVariableCircuit(BigInteger[][] umValues, 
			BigInteger[][] cValues, BigInteger[] bValues, BigInteger[] fValues, 
			OInt[] outputs, SpdzProvider provider) {
		int m = cValues.length;
		int nPlusM = cValues[0].length;
		SInt[][] c = new SInt[m][nPlusM];
		c = sIntFill(c, provider);
		SInt[][] um = new SInt[m+1][m+1];
		um = sIntFill(um, provider);
		SInt[] B = new SInt[m];
		B = sIntFill(B, provider);
		SInt[] F = new SInt[nPlusM];
		F = sIntFill(F, provider);
		SInt[] enteringVariable = new SInt[nPlusM];
		enteringVariable = sIntFill(enteringVariable, provider);

		Matrix<SInt> updateMatrix = new Matrix<SInt>(um);
		Matrix<SInt> C = new Matrix<SInt>(c);
		LPTableau tableau = new LPTableau(C, B, F, provider.getSInt());

		ProtocolProducer updateInputProducer = new ParallelGateProducer(makeInputGates(umValues, um, provider));
		ProtocolProducer cInputProducer = new ParallelGateProducer(makeInputGates(cValues, c, provider));
		ProtocolProducer bInputProducer = new ParallelGateProducer(makeInputGates(bValues, B, provider));
		ProtocolProducer fInputProducer = new ParallelGateProducer(makeInputGates(fValues, F, provider));
		ProtocolProducer inputProducer = new ParallelGateProducer(updateInputProducer, cInputProducer, bInputProducer, fInputProducer);

		SInt first = provider.getSInt();
		BlandEnteringVariableCircuit bvc = new BlandEnteringVariableCircuit(
				tableau, updateMatrix, enteringVariable, first, provider, provider);
		
		outputs = oIntFill(outputs, provider);
		ProtocolProducer output = makeOpenCircuit(enteringVariable, outputs, provider);

		return new SequentialProtocolProducer(inputProducer, bvc, output);
	}
	
	
		
	@Test
	public void testExitingVariableCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					public void test() throws Exception {
						
						for (int counter = 0; counter < 0; counter++) {
							int m = 9;
							int n = 20;
							BigInteger[][] cValues = new BigInteger[m][n+m];
							cValues = randomFill(cValues, rand);							
							BigInteger[] bValues = new BigInteger[m];
							bValues = randomFill(bValues, rand);
							BigInteger[] fValues = new BigInteger[n+m];
							fValues = randomFill(fValues, rand);
							int enteringIndex = rand.nextInt(m+n);
							Matrix<BigInteger> updateMatrixValues = new Matrix<BigInteger>(indentityMatrix(m+1));
							Matrix<BigInteger> constraintValues = new Matrix<BigInteger>(cValues);
							
							int exitIndex = rand.nextInt(m);
							constraintValues.getIthRow(exitIndex)[enteringIndex] = BigInteger.ONE;
							bValues[exitIndex] = getInfinity().negate();
													
							BigInteger[] updatedColumn = computeUpdatedColumn(constraintValues, fValues, updateMatrixValues, enteringIndex); 
							int index = exitingIndex(updatedColumn, bValues, updateMatrixValues);
														
							Assert.assertEquals(exitIndex, index);
						}
						if (false){
							int m = 4;
							int n = 5;
							
							BigInteger[][] umValues = new BigInteger[m+1][m+1];
							BigInteger[][] cValues = new BigInteger[m][n+m];
							BigInteger[] bValues = new BigInteger[m];
							BigInteger[] fValues = new BigInteger[n+m];
														
							umValues = indentityMatrix(m+1);
							cValues = zeroFill(cValues);							
							bValues = zeroFill(bValues);
							fValues = zeroFill(fValues);
							int enteringIndexValue = 1;
							int trueExit = 3;
							cValues[trueExit][enteringIndexValue] = BigInteger.ONE;
							bValues[trueExit] = BigInteger.ONE.negate().mod(Util.getModulus());
							
							
							Matrix<BigInteger> updateMatrixValues = new Matrix<BigInteger>(umValues);
							Matrix<BigInteger> constraintValues = new Matrix<BigInteger>(cValues);
																				
							BigInteger[] updatedColumn = computeUpdatedColumn(constraintValues, fValues, updateMatrixValues, enteringIndexValue); 							
							BigInteger[] updateColumn = computeUpdateColumn(trueExit, updatedColumn);
							BigInteger pivot = computePivot(updatedColumn, trueExit);
							
							OInt[] exitingIndexOut = new OInt[m];
							OInt[] updateColumnOut = new OInt[m + 1];
							OInt pivotOut = provider.getOInt();
							
							ProtocolProducer circuit = setUpExitingCircuit(umValues, cValues, bValues, fValues, 
									enteringIndexValue, exitingIndexOut, updateColumnOut, pivotOut, provider);
							
							sce.runApplication(circuit);
							
							int exitIndex = 0;
							int count = 0;
							for (OInt e: exitingIndexOut) {
								count += e.getValue().intValue();
								if (count == 0) {
									exitIndex++;
								}
							}
							
							Assert.assertEquals(1, count);
							Assert.assertEquals(trueExit, exitIndex);
							Assert.assertEquals(pivot, pivotOut.getValue());
							for (int i = 0; i < updateColumn.length; i++) {
								Assert.assertEquals(updateColumn[i], updateColumnOut[i].getValue());
							}
						}
						if (false){
							int m = 4;
							int n = 5;
							
							BigInteger[][] umValues = new BigInteger[m+1][m+1];
							BigInteger[][] cValues = new BigInteger[m][n+m];
							BigInteger[] bValues = new BigInteger[m];
							BigInteger[] fValues = new BigInteger[n+m];
														
							umValues = indentityMatrix(m+1);
							cValues = randomFill(cValues, rand);							
							bValues = randomFill(bValues, rand);
							fValues = randomFill(fValues, rand);
							int enteringIndexValue = 1;
							int trueExit = 3;
							cValues[trueExit][enteringIndexValue] = BigInteger.ONE;
							bValues[trueExit] = BigInteger.valueOf(2).pow(10).negate().mod(Util.getModulus());
							
							
							Matrix<BigInteger> updateMatrixValues = new Matrix<BigInteger>(umValues);
							Matrix<BigInteger> constraintValues = new Matrix<BigInteger>(cValues);
																				
							BigInteger[] updatedColumn = computeUpdatedColumn(constraintValues, fValues, updateMatrixValues, enteringIndexValue); 							
							BigInteger[] updateColumn = computeUpdateColumn(trueExit, updatedColumn);
							BigInteger pivot = computePivot(updatedColumn, trueExit);
							
							OInt[] exitingIndexOut = new OInt[m];
							OInt[] updateColumnOut = new OInt[m + 1];
							OInt pivotOut = provider.getOInt();
							
							ProtocolProducer circuit = setUpExitingCircuit(umValues, cValues, bValues, fValues, 
									enteringIndexValue, exitingIndexOut, updateColumnOut, pivotOut, provider);
							
							sce.runApplication(circuit);
							
							int exitIndex = 0;
							int count = 0;
							for (OInt e: exitingIndexOut) {
								count += e.getValue().intValue();
								if (count == 0) {
									exitIndex++;
								}
							}
							
							Assert.assertEquals(1, count);
							Assert.assertEquals(trueExit, exitIndex);
							Assert.assertEquals(pivot, pivotOut.getValue());
							for (int i = 0; i < updateColumn.length; i++) {
								Assert.assertEquals(updateColumn[i], updateColumnOut[i].getValue());
							}
						}
						for (int counter = 0; counter < 1; counter++) {
							int m = 40;
							int n = 50;
							int enteringIndexValue = rand.nextInt(n+m);
							BigInteger[][] umValues = new BigInteger[m+1][m+1];
							BigInteger[][] cValues = new BigInteger[m][n+m];
							BigInteger[] bValues = new BigInteger[m];
							BigInteger[] fValues = new BigInteger[n+m];
							umValues = randomFill(umValues, rand);
							cValues = randomFill(cValues, rand);							
							bValues = randomFill(bValues, rand);
							fValues = randomFill(fValues, rand);
							
							Matrix<BigInteger> updateMatrixValues = new Matrix<BigInteger>(umValues);
							Matrix<BigInteger> constraintValues = new Matrix<BigInteger>(cValues);
																				
							BigInteger[] updatedColumn = computeUpdatedColumn(constraintValues, fValues, updateMatrixValues, enteringIndexValue); 							
							int index = exitingIndex(updatedColumn, bValues, updateMatrixValues);
							BigInteger[] updateColumn = computeUpdateColumn(index, updatedColumn);
							BigInteger pivot = computePivot(updatedColumn, index);
							
							OInt[] exitingIndexOut = new OInt[m];
							OInt[] updateColumnOut = new OInt[m + 1];
							OInt pivotOut = provider.getOInt();
							
							ProtocolProducer circuit = setUpExitingCircuit(umValues, cValues, bValues, fValues, 
									enteringIndexValue, exitingIndexOut, updateColumnOut, pivotOut, provider);
							System.out.println("Eval");
							sce.runApplication(circuit);
							System.out.println("Post Eval");
							int exitIndex = 0;
							int count = 0;
							for (OInt e: exitingIndexOut) {
								count += e.getValue().intValue();
								if (count == 0) {
									exitIndex++;
								}
							}
							
							Assert.assertEquals(1, count);
							Assert.assertEquals(index, exitIndex);
							Assert.assertEquals(pivot, pivotOut.getValue());
							for (int i = 0; i < updateColumn.length; i++) {
								Assert.assertEquals(Util.convertRepresentation(updateColumn[i]), updateColumnOut[i].getValue());
							}
						}
						
						
					}
					
					
				};
			}
		}, 2);
	}
	
	private ProtocolProducer setUpExitingCircuit(
			BigInteger[][] umValues, 
			BigInteger[][] cValues, 
			BigInteger[] bValues, 
			BigInteger[] fValues, 
			int enteringIndexValue, 
			OInt[] exitingIndexOut, 
			OInt[] updateColumnOut, 
			OInt pivotOut, 
			SpdzProvider provider) {
		// Input
		SInt[][] updateMatrix = new SInt[umValues.length][umValues[0].length];
		SInt[][] C = new SInt[cValues.length][cValues[0].length];
		SInt[] B = new SInt[bValues.length];
		SInt[] F = new SInt[fValues.length];
		SInt[] enteringIndex = new SInt[cValues[0].length];
		// Output 
		SInt[] exitingIndex = new SInt[cValues.length];
		SInt[] updateColumn = new SInt[updateMatrix.length];
		SInt pivot;
		
		// Initialize 
		updateMatrix = sIntFill(updateMatrix, provider);
		C = sIntFill(C, provider);
		B = sIntFill(B, provider);
		F = sIntFill(F, provider);
		exitingIndex = sIntFill(exitingIndex, provider);
		updateColumn = sIntFill(updateColumn, provider);
		enteringIndex = sIntFill(enteringIndex, provider);
		LPTableau tableau = new LPTableau(new Matrix<SInt>(C), B, F, provider.getSInt());
		pivot = provider.getSInt();
		
		// Gate Producers
		BigInteger[] enteringValues = new BigInteger[enteringIndex.length];
		enteringValues = zeroFill(enteringValues);
		enteringValues[enteringIndexValue] = BigInteger.ONE;
		ProtocolProducer updateMatrixInputProducer = new ParallelGateProducer(makeInputGates(umValues, updateMatrix, provider));
		ProtocolProducer cInputProducer = new ParallelGateProducer(makeInputGates(cValues, C, provider));
		ProtocolProducer bInputProducer = new ParallelGateProducer(makeInputGates(bValues, B, provider));
		ProtocolProducer fInputProducer = new ParallelGateProducer(makeInputGates(fValues, F, provider));
		ProtocolProducer enteringInputProducer = new ParallelGateProducer(makeInputGates(enteringValues, enteringIndex, provider));
				
		ProtocolProducer inputProducer = new ParallelGateProducer(updateMatrixInputProducer, cInputProducer, bInputProducer, fInputProducer, enteringInputProducer);
		ProtocolProducer evc = provider.getExitingVariableCircuit(tableau, new Matrix<SInt>(updateMatrix), enteringIndex, exitingIndex, updateColumn, pivot);
				
		exitingIndexOut = oIntFill(exitingIndexOut, provider);
		updateColumnOut = oIntFill(updateColumnOut, provider);
		ProtocolProducer exitOutProducer = makeOpenCircuit(exitingIndex, exitingIndexOut, provider);
		ProtocolProducer updateOutProducer = makeOpenCircuit(updateColumn, updateColumnOut, provider);
		ProtocolProducer pivotOutProducer = makeOpenCircuit(new SInt[]{pivot}, new OInt[]{pivotOut}, provider);
		ProtocolProducer outputProducer = new ParallelGateProducer(exitOutProducer ,updateOutProducer, pivotOutProducer);
		
		MarkerCircuit startMark = provider.getMarkerCircuit("Starting");
		MarkerCircuit inputMark = provider.getMarkerCircuit("Did input");
		MarkerCircuit evcMark = provider.getMarkerCircuit("Did EVC");
		MarkerCircuit outMark = provider.getMarkerCircuit("Did Output");
		ProtocolProducer result = new SequentialProtocolProducer(startMark, inputProducer, inputMark, evc, evcMark, outputProducer, outMark);
		
		return result;
	}
	
	@Test 
	public void testUpdateMatrixCircuit() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					public void test() throws Exception {
						
						for (int counter = 0; counter < 1; counter++) { 
							int m = 100;
							NumericCircuitFactory builder = new NumericCircuitFactory(provider);
							BigInteger[][] oldUpdateMatrixValue = new BigInteger[m][m]; 
							BigInteger[] LValue = new BigInteger[m];
							BigInteger[] CValue = new BigInteger[m];
							BigInteger pValue;
							BigInteger p_primeValue;

							int index = 2;
							oldUpdateMatrixValue = indentityMatrix(m);
							LValue = zeroFill(LValue);
							LValue[index] = BigInteger.ONE;
							for (int i = 0; i < CValue.length; i++) {
								CValue[i] = BigInteger.valueOf((i + 2));
							}
							CValue[index] = BigInteger.ONE;
							pValue = BigInteger.valueOf(2);
							p_primeValue = BigInteger.valueOf(1);
							BigInteger pp = pValue.multiply(p_primeValue.modInverse(Util.getModulus())).mod(Util.getModulus());
							BigInteger p_primeInv = p_primeValue.modInverse(Util.getModulus()).mod(Util.getModulus());

							builder.beginParScope();
							SInt[][] oldUpdateMatrix = new SInt[m][m];
							for (int i = 0; i < m; i++) {
								oldUpdateMatrix[i] = builder.known(oldUpdateMatrixValue[i]);
							}
							SInt[][] newUpdateMatrix = builder.getSIntMatrix(m, m);
							SInt[] L = builder.known(LValue);
							SInt[] C = builder.known(CValue);
							SInt p = builder.known(pValue);
							SInt p_prime = builder.known(p_primeValue);
							builder.endCurScope();
							
							// DONE: setting up values
							
							ProtocolProducer umc = provider.getUpdateMatrixCircuit(new Matrix<SInt>(oldUpdateMatrix), L, C, p, p_prime, new Matrix<SInt>(newUpdateMatrix));

							OInt[][] newUpdateMatrixOut = new OInt[m][m];
							newUpdateMatrixOut = oIntFill(newUpdateMatrixOut, provider);

							ProtocolProducer output = new ParallelGateProducer(makeOpenCircuit(newUpdateMatrix, newUpdateMatrixOut, provider));
							
							ProtocolProducer gp = new SequentialProtocolProducer(builder.getCircuit(), umc, output);
							long time  = System.nanoTime();
							sce.runApplication(gp);
							
							long timedone = System.nanoTime();
							if (conf.getMyId() == 1) {
								for (int i = 0; i < newUpdateMatrixOut.length; i++) {
									for (int j = 0; j < newUpdateMatrixOut[i].length; j++) {
										if (j == index) {
											Assert.assertEquals(CValue[i].multiply(p_primeInv).mod(Util.getModulus()), newUpdateMatrixOut[i][j].getValue());
										} else if (i == j) {
											Assert.assertEquals(pp, newUpdateMatrixOut[i][j].getValue());
										}
									}
								}
							}
							System.out.println("Time: " + (timedone - time)/1000000);
						}
						
						for (int counter = 0; counter < 1; counter++) { 
							int m = 5;
							BigInteger[][] oldUpdateMatrixValue = new BigInteger[m][m]; 
							BigInteger[] LValue = new BigInteger[m];
							BigInteger[] CValue = new BigInteger[m];
							BigInteger pValue;
							BigInteger p_primeValue;

							int index = rand.nextInt(m);
							oldUpdateMatrixValue = indentityMatrix(m);
							LValue = zeroFill(LValue);
							LValue[index] = BigInteger.ONE;
							for (int i = 0; i < CValue.length; i++) {
								CValue[i] = new BigInteger(10, rand).subtract(BigInteger.valueOf(2).pow(9));
							}
							CValue[index] = BigInteger.ONE;
							pValue = new BigInteger(10, rand).subtract(BigInteger.valueOf(2).pow(9));
							p_primeValue = BigInteger.valueOf(1);
							BigInteger pp = pValue.multiply(p_primeValue.modInverse(Util.getModulus())).mod(Util.getModulus());
							BigInteger p_primeInv = p_primeValue.modInverse(Util.getModulus()).mod(Util.getModulus());

							SInt[][] oldUpdateMatrix = new SInt[m][m]; 
							SInt[][] newUpdateMatrix = new SInt[m][m];
							SInt[] L = new SInt[m];
							SInt[] C = new SInt[m];
							SInt p;
							SInt p_prime;

							oldUpdateMatrix = sIntFill(oldUpdateMatrix, provider);
							newUpdateMatrix = sIntFill(newUpdateMatrix, provider);
							L = sIntFill(L, provider);
							C = sIntFill(C, provider);
							p = provider.getSInt();
							p_prime = provider.getSInt();

							ProtocolProducer oldUpdateMatrixInputs = new ParallelGateProducer(makeInputGates(oldUpdateMatrixValue, oldUpdateMatrix, provider));
							ProtocolProducer LInputs = new ParallelGateProducer(makeInputGates(LValue, L, provider));
							ProtocolProducer CInputs = new ParallelGateProducer(makeInputGates(CValue, C, provider));
							ProtocolProducer pInputs = new ParallelGateProducer(makeInputGates(new BigInteger[] {pValue, p_primeValue}, new SInt[] {p, p_prime}, provider));
							ProtocolProducer inputProducer = new ParallelGateProducer(oldUpdateMatrixInputs, LInputs, CInputs, pInputs);

							ProtocolProducer umc = provider.getUpdateMatrixCircuit(new Matrix<SInt>(oldUpdateMatrix), L, C, p, p_prime, new Matrix<SInt>(newUpdateMatrix));

							OInt[][] newUpdateMatrixOut = new OInt[m][m];
							newUpdateMatrixOut = oIntFill(newUpdateMatrixOut, provider);

							ProtocolProducer output = new ParallelGateProducer(makeOpenCircuit(newUpdateMatrix, newUpdateMatrixOut, provider));

							ProtocolProducer gp = new SequentialProtocolProducer(inputProducer, umc, output);

							sce.runApplication(gp);

							if (conf.getMyId() == 1) {
								for (int i = 0; i < newUpdateMatrixOut.length; i++) {
									for (int j = 0; j < newUpdateMatrixOut[i].length; j++) {
										if (j == index) {
											Assert.assertEquals(Util.convertRepresentation(CValue[i].multiply(p_primeInv)), newUpdateMatrixOut[i][j].getValue());
										} else if (i == j) {
											Assert.assertEquals(pp.mod(Util.getModulus()), newUpdateMatrixOut[i][j].getValue());
										}
										//System.out.print(newUpdateMatrixOut[i][j].getValue() + " ");
									}
									//System.out.print("\n");
								}
							}
						}
						
						for (int counter = 0; counter < 1; counter++) { 
							int m = 5;
							BigInteger[][] oldUpdateMatrixValue = new BigInteger[m][m]; 
							BigInteger[] LValue = new BigInteger[m - 1];
							BigInteger[] CValue = new BigInteger[m];
							BigInteger pValue;
							BigInteger p_primeValue;

							int index = rand.nextInt(m - 1);
							oldUpdateMatrixValue = randomFill(oldUpdateMatrixValue, rand);
							LValue = zeroFill(LValue);
							LValue[index] = BigInteger.ONE;
							for (int i = 0; i < CValue.length; i++) {
								CValue[i] = new BigInteger(10, rand).subtract(BigInteger.valueOf(2).pow(9));
							}
							CValue[index] = BigInteger.ONE;
							pValue = new BigInteger(10, rand).subtract(BigInteger.valueOf(2).pow(9));
							BigInteger r = new BigInteger(10, rand).subtract(BigInteger.valueOf(2).pow(9));
							p_primeValue = pValue.multiply(r.modInverse(Util.getModulus())).mod(Util.getModulus());
							BigInteger pp = pValue.multiply(p_primeValue.modInverse(Util.getModulus())).mod(Util.getModulus());
							BigInteger p_primeInv = p_primeValue.modInverse(Util.getModulus()).mod(Util.getModulus());

							SInt[][] oldUpdateMatrix = new SInt[m][m]; 
							SInt[][] newUpdateMatrix = new SInt[m][m];
							SInt[] L = new SInt[m - 1];
							SInt[] C = new SInt[m];
							SInt p;
							SInt p_prime;

							oldUpdateMatrix = sIntFill(oldUpdateMatrix, provider);
							newUpdateMatrix = sIntFill(newUpdateMatrix, provider);
							L = sIntFill(L, provider);
							C = sIntFill(C, provider);
							p = provider.getSInt();
							p_prime = provider.getSInt();

							ProtocolProducer oldUpdateMatrixInputs = new ParallelGateProducer(makeInputGates(oldUpdateMatrixValue, oldUpdateMatrix, provider));
							ProtocolProducer LInputs = new ParallelGateProducer(makeInputGates(LValue, L, provider));
							ProtocolProducer CInputs = new ParallelGateProducer(makeInputGates(CValue, C, provider));
							ProtocolProducer pInputs = new ParallelGateProducer(makeInputGates(new BigInteger[] {pValue, p_primeValue}, new SInt[] {p, p_prime}, provider));
							ProtocolProducer inputProducer = new ParallelGateProducer(oldUpdateMatrixInputs, LInputs, CInputs, pInputs);

							ProtocolProducer umc = provider.getUpdateMatrixCircuit(new Matrix<SInt>(oldUpdateMatrix), L, C, p, p_prime, new Matrix<SInt>(newUpdateMatrix));

							OInt[][] newUpdateMatrixOut = new OInt[m][m];
							newUpdateMatrixOut = oIntFill(newUpdateMatrixOut, provider);

							ProtocolProducer output = new ParallelGateProducer(makeOpenCircuit(newUpdateMatrix, newUpdateMatrixOut, provider));

							ProtocolProducer gp = new SequentialProtocolProducer(inputProducer, umc, output);

							sce.runApplication(gp);

							BigInteger[][] thisIteration = new BigInteger[m][m];
							thisIteration = zeroFill(thisIteration);
							for (int i = 0; i < m; i++) {
								for (int j = 0; j < m; j++) {
									if (j == index) {
										thisIteration[i][j] = CValue[i].multiply(p_primeInv).mod(Util.getModulus());
									} else if (j == i) {
										thisIteration[i][j] = pp.mod(Util.getModulus());
									}
								}
							}
							thisIteration[index][index] = BigInteger.ONE;
														
							BigInteger[][] expectedResult = matrixMultiplication(new Matrix<BigInteger>(thisIteration), new Matrix<BigInteger>(oldUpdateMatrixValue));
														
							if (conf.getMyId() == 1) {
								for (int i = 0; i < newUpdateMatrixOut.length; i++) {
									for (int j = 0; j < newUpdateMatrixOut[i].length; j++) {
										Assert.assertEquals(Util.convertRepresentation(expectedResult[i][j]), newUpdateMatrixOut[i][j].getValue());
										//System.out.print(newUpdateMatrixOut[i][j].getValue() + " ");
									}
									//System.out.print("\n");
								}
							}
						}
						
						
					}
				};
			}
		},2);
	}
	
	@Test
	public void testOptimalValueCircuit() throws Exception{
		TestThreadRunner.run(new TestThreadFactory() {
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					public void test() throws Exception {
						
						for (int counter = 0; counter < 10; counter++) {
							int m = 5;
							BigInteger[][] updateMatrixValues = randomFill(new BigInteger[m + 1][m + 1], rand);
							BigInteger[] BValues = randomFill(new BigInteger[m], rand);
							BigInteger pivotValue = new BigInteger(10, rand).subtract(BigInteger.valueOf(2).pow(9));
							
							SInt[][] updateMatrix = sIntFill(new SInt[m + 1][m + 1], provider);
							SInt[] B = sIntFill(new SInt[m], provider);
							SInt pivot = provider.getSInt();
							SInt optimalValue = provider.getSInt();
							
							ProtocolProducer input = new ParallelGateProducer(new ParallelGateProducer(makeInputGates(updateMatrixValues, updateMatrix, provider)),
									new ParallelGateProducer(makeInputGates(BValues, B, provider)), 
									new ParallelGateProducer(makeInputGates(new BigInteger[] {pivotValue}, new SInt[] {pivot}, provider)));
							
							ProtocolProducer optimal = provider.getOptimalValueCircuit(new Matrix<SInt>(updateMatrix), B, pivot, optimalValue);
							
							OInt optimalValueOut = provider.getOInt();
							ProtocolProducer output = new ParallelGateProducer(makeOpenCircuit(new SInt[] {optimalValue}, new OInt[] {optimalValueOut}, provider));
							
							ProtocolProducer gp = new SequentialProtocolProducer(input, optimal, output);
							
							sce.runApplication(gp);
							
							BigInteger[] shortRow = new BigInteger[m];
							System.arraycopy(updateMatrixValues[m], 0, shortRow, 0, m);
							BigInteger numerator = innerProduct(shortRow, BValues).mod(Util.getModulus());
							BigInteger expected = numerator.multiply(pivotValue.modInverse(Util.getModulus())).mod(Util.getModulus());
							Assert.assertEquals(Util.convertRepresentation(expected), optimalValueOut.getValue());
						}
						
						
					}
				};
			}			
		}, 2);
	}
	
	@Test
	public void testLPSolver() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					public void test() throws Exception {
										
						SpdzBenchmarkData.enableTestMode();
						{
							// Define input values
							int n = 4;
							int m = 3;
							BigInteger[][] CValue = new BigInteger[m][n + m];
							BigInteger[] BValue = {BigInteger.valueOf(20), BigInteger.valueOf(50), BigInteger.valueOf(23)};
							BigInteger[] FValue = {BigInteger.valueOf(-2), BigInteger.valueOf(-3), BigInteger.valueOf(-4), BigInteger.valueOf(-10), 
									BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO};
							BigInteger zValue = BigInteger.valueOf(0);

							// Initialize input
							CValue[0] = new BigInteger[]{BigInteger.valueOf(1), BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(0), 
									BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO};
							CValue[1] = new BigInteger[]{BigInteger.valueOf(2), BigInteger.valueOf(-3), BigInteger.valueOf(5), BigInteger.valueOf(10),
									BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO};
							CValue[2] = new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(9), BigInteger.valueOf(1), BigInteger.valueOf(0),
									BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE};


							// Initial values of internal variables 
							BigInteger[][] updateMatrixValue = indentityMatrix(m + 1);
							BigInteger prevPivotValue = BigInteger.valueOf(1);

							// Secure values
							SInt[][] C = sIntFill(new SInt[m][n + m], provider);
							SInt[] B = sIntFill(new SInt[m], provider);
							SInt[] F = sIntFill(new SInt[n + m], provider);
							SInt z = provider.getSInt();
							LPTableau tableau = new LPTableau(new Matrix<SInt>(C), B, F, z);
							SInt[][] updateMatrix = sIntFill(new SInt[m + 1][m + 1], provider);
							SInt prevPivot = provider.getSInt();

							// Input 
							ProtocolProducer CInput = new ParallelGateProducer(makeInputGates(CValue, C, provider));
							ProtocolProducer BInput = new ParallelGateProducer(makeInputGates(BValue, B, provider));
							ProtocolProducer FInput = new ParallelGateProducer(makeInputGates(FValue, F, provider));
							ProtocolProducer updateMatrixInput = new ParallelGateProducer(makeInputGates(updateMatrixValue, updateMatrix, provider));
							ProtocolProducer miscInput = new ParallelGateProducer(makeInputGates(new BigInteger[]{zValue, prevPivotValue}, 
									new SInt[]{z, prevPivot}, provider));
							ProtocolProducer input = new ParallelGateProducer(CInput, BInput, FInput, updateMatrixInput, miscInput);
							long timeTotal = 0;
							long timeEntering = 0;
							long timeMisc = 0;
							long timeExiting = 0;
							long timeUpdating = 0;
								

							boolean termination = false;
							SInt[] enteringIndex = sIntFill(new SInt[m + n], provider);
							// Phase 1 (the first time we also need to evaluate inputs)
							{							
								SInt minimum = provider.getSInt();
								ProtocolProducer enteringProducer = provider.getEnteringVariableCircuit(tableau, new Matrix<SInt>(updateMatrix), 
										enteringIndex, minimum);
								SInt zero = provider.getSInt(0);
								SInt positive = provider.getSInt();
								OInt positiveOut = provider.getOInt();							
								ProtocolProducer comp = provider.getComparisonCircuit(zero, minimum, positive, false);
								ProtocolProducer output = provider.getOpenIntCircuit(positive, positiveOut);
								ProtocolProducer phase1 = new SequentialProtocolProducer(input, enteringProducer, comp, output);
								SpdzBenchmarkData.logTiming("testLPSolver-phase1-with-inputs", true, conf.getMyId());
								sce.runApplication(phase1);
								SpdzBenchmarkData.logTiming("testLPSolver-phase1-with-inputs", false, conf.getMyId());

								termination = positiveOut.getValue().equals(BigInteger.ONE);
							}						
							int i = 0;
							while (!termination) {
								i++;
								
								OInt[] enteringOut = oIntFill(new OInt[enteringIndex.length], provider);
								ProtocolProducer entOutProducer = makeOpenCircuit(enteringIndex, enteringOut, provider);
								sce.runApplication(entOutProducer);
								int count = 0;
								int entIndex = 0;
								for (OInt out: enteringOut) {
									count += out.getValue().intValue();
									if (count < 1) {
										entIndex++;
									}
								}
								System.out.println("Entering Index was: " + entIndex);

								// Phase 2 - Finding the exiting variable and updating the tableau
								SInt[] exitingIndex = sIntFill(new SInt[m], provider);
								SInt[] updateColumn = sIntFill(new SInt[m + 1], provider);
								SInt pivot = provider.getSInt();							
								ProtocolProducer exitingIndexProducer = provider.getExitingVariableCircuit(tableau, new Matrix<SInt>(updateMatrix), 
										enteringIndex, exitingIndex, updateColumn, pivot);
								SInt[][] newUpdateMatrix = sIntFill(new SInt[m + 1][m + 1], provider);
								ProtocolProducer updateMatrixProducer = provider.getUpdateMatrixCircuit(new Matrix<SInt>(updateMatrix), exitingIndex, 
										updateColumn, pivot, prevPivot, new Matrix<SInt>(newUpdateMatrix));
								updateMatrix = newUpdateMatrix;

								prevPivot = pivot;

								// Phase 1 - Finding the entering variable and deciding whether or not to terminate
								enteringIndex = sIntFill(new SInt[m + n], provider);
								SInt minimum = provider.getSInt();
								ProtocolProducer enteringProducer = provider.getEnteringVariableCircuit(tableau, new Matrix<SInt>(updateMatrix), 
										enteringIndex, minimum);
								SInt zero = provider.getSInt(0);
								SInt positive  = provider.getSInt();
								OInt positiveOut = provider.getOInt();
								ProtocolProducer comp = provider.getComparisonCircuit(zero, minimum, positive, false);
								ProtocolProducer output = provider.getOpenIntCircuit(positive, positiveOut);
								long startTime = System.nanoTime();
								sce.runApplication(exitingIndexProducer);
								long exitTime = System.nanoTime();
								sce.runApplication(updateMatrixProducer);
								long updateTime = System.nanoTime();
								sce.runApplication(enteringProducer);
								long enterTime = System.nanoTime();
								sce.runApplication(comp);
								sce.runApplication(output);
								long miscTime = System.nanoTime();
								timeExiting += exitTime - startTime;
								timeUpdating += updateTime - exitTime;
								timeEntering += enterTime - updateTime;
								timeMisc += miscTime - enterTime;
								timeTotal += timeExiting + timeUpdating + timeEntering + timeMisc;
								
								
								OInt[] exitingOut = oIntFill(new OInt[exitingIndex.length], provider);
								ProtocolProducer extOutProducer = makeOpenCircuit(exitingIndex, exitingOut, provider);
								sce.runApplication(extOutProducer);
								count = 0;
								int extIndex = 0;
								for (OInt out: exitingOut) {
									count += out.getValue().intValue();
									if (count < 1) {
										extIndex++;
									}
								}
								System.out.println("Exiting Index was: " + extIndex);

								termination = positiveOut.getValue().equals(BigInteger.ONE);
							}
							
							System.out.println("Enter: " + timeEntering/1000000);
							System.out.println("Exiting: " + timeExiting/1000000);
							System.out.println("Update: " + timeUpdating/1000000);
							System.out.println("Misc: " + timeMisc/1000000);
							System.out.println("Total: " + timeTotal/1000000);

							SInt optimal = provider.getSInt();
							OInt optimalOut = provider.getOInt();
							OInt pivotOut = provider.getOInt();
							
							ProtocolProducer optimalProducer = provider.getOptimalNumeratorCircuit(new Matrix<SInt>(updateMatrix), B, optimal);
							ProtocolProducer numeratorOutProducer = provider.getOpenIntCircuit(optimal, optimalOut);
							ProtocolProducer pivotOutProducer = provider.getOpenIntCircuit(prevPivot, pivotOut);
							ProtocolProducer gp = new SequentialProtocolProducer(optimalProducer, numeratorOutProducer, pivotOutProducer);
							SpdzBenchmarkData.logTiming("testLPSolver-phase2", true, conf.getMyId());
							sce.runApplication(gp);
							SpdzBenchmarkData.logTiming("testLPSolver-phase2", false, conf.getMyId());
							System.out.println("Found optimal value to be: " + optimalOut.getValue() + "/" + pivotOut.getValue());
							
						}
						SpdzBenchmarkData.printBenchmark(conf.getMyId());
						
					}
					
				};
			}
		}, 2);
	}
	
	
	private BigInteger[][] matrixMultiplication(Matrix<BigInteger> a, Matrix<BigInteger> b) {
		BigInteger[][] result = new BigInteger[a.getWidth()][a.getWidth()];
		for (int i = 0; i < a.getWidth(); i++) {
			for (int j = 0; j < a.getHeight(); j++) {
				BigInteger[] placeholder = new BigInteger[b.getHeight()]; 
				result[i][j] = innerProduct(a.getIthRow(i), b.getIthColumn(j, placeholder)); 
			}
		}
		return result;
	}
	
	// This value of infinity is not good. It works for the tests, but should be much larger when input size grows.
	// That would imply having to increase the value of Util.p to avoid overflow problems.
	private BigInteger getInfinity() {
		return BigInteger.valueOf(2).pow(30);
	}
	
	private BigInteger[][] indentityMatrix(int n) {
		BigInteger[][] indentity = zeroFill(new BigInteger[n][n]);
		for (int i = 0; i < n; i++) {
			indentity[i][i] = BigInteger.ONE;
		}
		return indentity;
	}
	
	private BigInteger computePivot(BigInteger[] updatedColumn, int exitingIndex) {
		return updatedColumn[exitingIndex];
	}
	
	private int exitingIndex(BigInteger[] updatedColumn, BigInteger[] B, Matrix<BigInteger> updateMatrix) {
		BigInteger infinity = getInfinity();
		
		BigInteger[] applicableColumn = new BigInteger[updatedColumn.length];
		for (int i = 0; i < B.length; i++) {
			if (compareModP(updatedColumn[i], BigInteger.ZERO) <= 0) {
				applicableColumn[i] = BigInteger.ONE;
			} else {
				applicableColumn[i] = updatedColumn[i];
			}
		}
		
		BigInteger[] updatedB = new BigInteger[B.length];
		for (int i = 0; i < B.length; i++) {
			BigInteger[] shortRow = new BigInteger[B.length];
			System.arraycopy(updateMatrix.getIthRow(i), 0, shortRow, 0, B.length);
			updatedB[i] = innerProduct(B, shortRow);
		}		
		BigInteger[] applicableB = new BigInteger[updatedB.length];
		for (int i = 0; i < B.length; i++) {
			if (compareModP(updatedColumn[i], BigInteger.ZERO) <= 0) {
				applicableB[i] = infinity;
			} else {
				applicableB[i] = updatedB[i];
			}
		}		
		
		int exitingIndex = 0;
		BigInteger minNominator = applicableB[0]; 
		BigInteger minDenominator = applicableColumn[0];
		
		for (int i = 1; i < applicableB.length; i++) {
			BigInteger leftHand = minNominator.multiply(applicableColumn[i]); 
			BigInteger rightHand = minDenominator.multiply(applicableB[i]);
			leftHand = leftHand.mod(Util.getModulus());
			rightHand = rightHand.mod(Util.getModulus());
			BigInteger big = Util.getModulus();
			if (compareModP(leftHand, rightHand) > 0) {
				minNominator = applicableB[i];
				minDenominator = applicableColumn[i];
				exitingIndex = i;
			}
		}
		
		
		return exitingIndex;
	}

	private BigInteger[] computeUpdatedColumn(Matrix<BigInteger> C, BigInteger[] F, Matrix<BigInteger> updateMatrix, int enteringIndex) {
		BigInteger[] column = new BigInteger[C.getHeight() + 1]; 
		System.arraycopy(C.getIthColumn(enteringIndex, new BigInteger[C.getHeight()]), 0, column, 0, C.getHeight());
		column[C.getHeight()] = F[enteringIndex];
		
		BigInteger[] updatedColumn = new BigInteger[column.length];
		for (int i = 0; i < column.length; i++) {
			updatedColumn[i] = innerProduct(column, updateMatrix.getIthRow(i));
		}
		return updatedColumn;
	}
	
	private BigInteger[] computeUpdateColumn(int exitingIndex, BigInteger[] updatedColumn) {
		BigInteger[] updateColumn = new BigInteger[updatedColumn.length]; 
		for (int i = 0; i < updatedColumn.length; i++) {
			if (i == exitingIndex) {
				updateColumn[i] = BigInteger.ONE;
			} else {
				updateColumn[i] = updatedColumn[i].negate().mod(Util.getModulus()); 
			}			
		}
		return updateColumn;
	}
	
	private BigInteger innerProduct(BigInteger[] a, BigInteger[] b) {
		if (a.length != b.length) {throw new RuntimeException("vectors not equal length");};
		BigInteger result = BigInteger.valueOf(0);
		for (int i = 0; i < a.length; i++) {
			result = (result.add(a[i].multiply(b[i]))).mod(Util.getModulus());
		}
		return result;
	}
	
	private ProtocolProducer makeOpenCircuit(SInt[][] closed, OInt[][] open, SpdzProvider provider) {
		ProtocolProducer[] openings = new ProtocolProducer[open.length];
		for (int i = 0; i < open.length; i++) {
			openings[i] = makeOpenCircuit(closed[i], open[i], provider); 
		}
		return new ParallelGateProducer(openings);
	}
	
	private ProtocolProducer makeOpenCircuit(SInt[] closed, OInt[] open, SpdzProvider provider) {
		OpenCircuit<SInt, OInt>[] openings = new OpenCircuit[closed.length]; 
		for (int i = 0; i < open.length; i++) {
			openings[i] = provider.getOpenIntCircuit(closed[i], open[i]);
		}
		return new ParallelGateProducer(openings);
	}
	
	private SpdzInputGate[] makeInputGates(BigInteger[][] values, SInt[][] matrix, SpdzProvider provider) {
		if (matrix.length != values.length || values[0].length != matrix[0].length ) {
			throw new RuntimeException("Matrix and values are not of equal dimensions");
		};
		SpdzInputGate[] inputGates = new SpdzInputGate[matrix.length * matrix[0].length];
		for(int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				inputGates[i * matrix[i].length + j] = (SpdzInputGate)provider.getInputCircuit(values[i][j], matrix[i][j], 1);
			}			
		}
		return inputGates;
	}
	
	private SpdzInputGate[] makeInputGates(BigInteger[] values, SInt[] vector, SpdzProvider provider) {
		if (vector.length != values.length) {throw new RuntimeException("Vector and values are not equal length");};
		SpdzInputGate[] inputGates = new SpdzInputGate[vector.length];
		for(int i = 0; i < vector.length; i++) {
			inputGates[i] = (SpdzInputGate)provider.getInputCircuit(values[i], vector[i], 1);
		}
		return inputGates;
	}
	
	
	private OInt[][] oIntFill(OInt[][] matrix, SpdzProvider provider) {
		for(OInt[] vector: matrix) {
			vector = oIntFill(vector, provider);
		}
		return matrix;
	}
	
	private OInt[] oIntFill(OInt[] vector, SpdzProvider provider) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = provider.getOInt();
		}
		return vector;
	}
		
	private SInt[][] sIntFill(SInt[][] matrix, SpdzProvider provider) {
		for(SInt[] vector: matrix) {
			vector = sIntFill(vector, provider);
		}
		return matrix;
	}
		
	private SInt[] sIntFill(SInt[] vector, SpdzProvider provider) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = provider.getSInt();
		}
		return vector;
	}
	
	private BigInteger[][] randomFill(BigInteger[][] matrix, Random rand) {
		for(BigInteger[] vector: matrix) {
			vector = randomFill(vector, rand);
		}
		return matrix;
	}
	
	private BigInteger[] randomFill(BigInteger[] vector, Random rand) {
		// 10 seems to be a safe value, if we go too high we can get overflow problems though.
		int bitLenght = 10;
		for(int i = 0; i < vector.length; i++) {
			vector[i] = new BigInteger(bitLenght, rand); 
			vector[i] = vector[i].subtract(BigInteger.valueOf(2).pow(bitLenght-1)).mod(Util.getModulus());
		}
		return vector;
	}
	
	
	
	private BigInteger[][] zeroFill(BigInteger[][] matrix) {
		for(BigInteger[] vector: matrix) {
			vector = zeroFill(vector);
		}
		return matrix;
	}
	
	private BigInteger[] zeroFill(BigInteger[] vector) {
		for(int i = 0; i < vector.length; i++) {
			vector[i] = BigInteger.valueOf(0);
		}
		return vector;
	}
	
	private int blandEnteringVariableIndex(
			Matrix<BigInteger> C, 
			Matrix<BigInteger> updateMatrix,
			BigInteger[] B, 
			BigInteger[] F) {
		BigInteger[] updatedF = new BigInteger[F.length];
		BigInteger[] updateRow = updateMatrix.getIthRow(updateMatrix.getHeight() - 1);
		for (int i = 0; i < F.length; i++) {
			updatedF[i] = BigInteger.valueOf(0);
			BigInteger[] column = new BigInteger[C.getHeight()];
			column = C.getIthColumn(i, column);
			for (int j = 0; j < C.getHeight(); j++) {
				updatedF[i] = updatedF[i].add(column[j].multiply(updateRow[j])).mod(Util.getModulus());
			}
			updatedF[i] = updatedF[i].add(F[i].multiply(updateRow[updateMatrix.getHeight()-1])).mod(Util.getModulus());
		}
		
		for(int i = 0; i < updatedF.length; i++){
			if (updatedF[i].intValue() != 0) {
				return i;
			}
		}
		return -1;
	}
	
	private int enteringVariableIndex(Matrix<BigInteger> C, Matrix<BigInteger> updateMatrix,
			BigInteger[] B, BigInteger[] F) {
		BigInteger[] updatedF = new BigInteger[F.length];
		BigInteger[] updateRow = updateMatrix.getIthRow(updateMatrix.getHeight() - 1);
		for (int i = 0; i < F.length; i++) {
			updatedF[i] = BigInteger.valueOf(0);
			BigInteger[] column = new BigInteger[C.getHeight()];
			column = C.getIthColumn(i, column);
			for (int j = 0; j < C.getHeight(); j++) {
				updatedF[i] = updatedF[i].add(column[j].multiply(updateRow[j])).mod(Util.getModulus());
			}
			updatedF[i] = updatedF[i].add(F[i].multiply(updateRow[updateMatrix.getHeight()-1])).mod(Util.getModulus());
		}
		
		BigInteger min = updatedF[0];
		int index = 0;
		
		for(int i = 0; i < updatedF.length; i++){
			if (compareModP(updatedF[i], min) < 0) {
				min = updatedF[i];
				index = i;
			}
		}
		return index;
	}
	
	private int compareModP(BigInteger a, BigInteger b) {
		BigInteger realA = a;
		BigInteger realB = b;
		BigInteger halfPoint = Util.getModulus().subtract(BigInteger.ONE).divide((BigInteger.valueOf(2)));
		if (a.compareTo(halfPoint) > 0) {
			realA = a.subtract(Util.getModulus());
		}
		if (b.compareTo(halfPoint) > 0) {
			realB = b.subtract(Util.getModulus());
		}
		return realA.compareTo(realB);
	}
	*/
}

