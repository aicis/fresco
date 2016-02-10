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
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.lib.lp.LPSolverTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

public class TestSpdzLPSolver2Parties {

	
	private void runTest(TestThreadFactory f, int noPlayers,
			EvaluationStrategy evalStrategy, StorageStrategy storageStrategy)
			throws Exception {
		Level logLevel = Level.FINE;
		Reporter.init(logLevel);

		// Since SCAPI currently does not work with ports > 9999 we use fixed
		// ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(noPlayers);
		for (int i = 1; i <= noPlayers; i++) {
			ports.add(9000 + i);
		}

		Map<Integer, NetworkConfiguration> netConf = TestConfiguration
				.getNetworkConfigurations(noPlayers, ports, logLevel);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);

			SpdzConfiguration spdzConf = new SpdzConfiguration() {
				
				@Override
				public boolean useDummyData() {
					return true;
				}
				
				@Override
				public String getTriplePath() {
					return null;
				}
				
				@Override
				public int getMaxBitLength() {
					return 150;
				}
			};
			ttc.protocolSuiteConf = spdzConf;
			boolean useSecureConnection = false; // No tests of secure connection
												// here.
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			ProtocolSuite suite = SpdzProtocolSuite.getInstance(playerId);
			ProtocolEvaluator evaluator = EvaluationStrategy.fromEnum(evalStrategy);			
			Storage storage = null;
			switch (storageStrategy) {
			case IN_MEMORY:
				storage = inMemStore;
			case MYSQL:
				//ttc.storage = mySQLStore;
			}
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator, noOfThreads, noOfVMThreads, ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}

	private static final InMemoryStorage inMemStore = new InMemoryStorage();
	//private static final MySQLStorage mySQLStore = MySQLStorage.getInstance();

	/**
	 * Makes sure that the preprocessed data exists in the storage's used in
	 * this test class. Not needed if we set useDummyData to true in spdz config. 
	 */
	/*
	@BeforeClass
	public static void initStorage() {
		Storage[] storages = new Storage[] {
				inMemStore};//, mySQLStore };
		InitializeStorage.initStorage(storages, 2, 10000, 1000, 500000, 2000);
	}
	*/

	@Test
	public void test_LPSolver_2_Sequential() throws Exception {
		runTest(new LPSolverTests.TestLPSolver(), 2,
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_LPSolver_2_Parallel() throws Exception {
		runTest(new LPSolverTests.TestLPSolver(), 2,
				EvaluationStrategy.PARALLEL, StorageStrategy.IN_MEMORY);
	}

	
	/*
	@Test
	public void testSolverPar() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						
						{
							File pattern = new File("lpinputs/pattern7.csv");
							File program = new File("lpinputs/program7.csv");
							LPInputReader inputreader = PlainLPInputReader.getFileInputReader(program, pattern, conf.getMyId());
							ParallelGateProducer par = new ParallelGateProducer();
							for (int i = 0; i < 1; i++) {
								LPPrefix prefix = new PlainSpdzLPPrefix(inputreader, provider);
								ProtocolProducer lpsolver = new LPSolverCircuit(
										prefix.getTableau(),
										prefix.getUpdateMatrix(), 
										prefix.getPivot(), 
										provider, provider);
								SInt sout = provider.getSInt();
								OInt out = provider.getOInt();
								ProtocolProducer outputter = provider.getOptimalValueCircuit(
										prefix.getUpdateMatrix(), 
										prefix.getTableau().getB(), 
										prefix.getPivot(), sout);
								ProtocolProducer open = provider.getOpenIntCircuit(sout, out);
								ProtocolProducer seq = new SequentialProtocolProducer(prefix.getProducer(), lpsolver, outputter, open);
								par.append(seq);
							}
							sce.runApplication(par);
						}
						{	
							File pattern = new File("lpinputs/pattern7.csv");
							File program = new File("lpinputs/program7.csv");
							LPInputReader inputreader = PlainLPInputReader.getFileInputReader(program, pattern, conf.getMyId());
							ParallelGateProducer par = new ParallelGateProducer();
							for (int i = 0; i < 5; i++) {
								LPPrefix prefix = new PlainSpdzLPPrefix(inputreader, provider);
								ProtocolProducer lpsolver = new LPSolverCircuit(
										prefix.getTableau(),
										prefix.getUpdateMatrix(), 
										prefix.getPivot(), 
										provider, provider);
								SInt sout = provider.getSInt();
								OInt out = provider.getOInt();
								ProtocolProducer outputter = provider.getOptimalValueCircuit(
										prefix.getUpdateMatrix(), 
										prefix.getTableau().getB(), 
										prefix.getPivot(), sout);
								ProtocolProducer open = provider.getOpenIntCircuit(sout, out);
								ProtocolProducer seq = new SequentialProtocolProducer(prefix.getProducer(), lpsolver, outputter, open);
								par.append(seq);
							}
							long startTime = System.nanoTime();
							sce.runApplication(par);
							long endTime = System.nanoTime();
							System.out.println("==================== Par Time: " + ((endTime - startTime)/1000000));
						}
					}
				};
			}
		}, 2);

	}
	
	@Test
	public void testSolverSeq() throws Exception {
		TestThreadRunner.run(new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						
						{
							File pattern = new File("lpinputs/pattern7.csv");
							File program = new File("lpinputs/program7.csv");
							LPInputReader inputreader = PlainLPInputReader.getFileInputReader(program, pattern, conf.getMyId());
							SequentialProtocolProducer sseq = new SequentialProtocolProducer();
							for (int i = 0; i < 1; i++) {
								LPPrefix prefix = new PlainSpdzLPPrefix(inputreader, provider);
								ProtocolProducer lpsolver = new LPSolverCircuit(
										prefix.getTableau(),
										prefix.getUpdateMatrix(), 
										prefix.getPivot(), 
										provider, provider);
								SInt sout = provider.getSInt();
								OInt out = provider.getOInt();
								ProtocolProducer outputter = provider.getOptimalValueCircuit(
										prefix.getUpdateMatrix(), 
										prefix.getTableau().getB(), 
										prefix.getPivot(), sout);
								ProtocolProducer open = provider.getOpenIntCircuit(sout, out);
								ProtocolProducer seq = new SequentialProtocolProducer(prefix.getProducer(), lpsolver, outputter, open);
								sseq.append(seq);
							}
							sce.runApplication(sseq);
						}
						{
							File pattern = new File("lpinputs/pattern7.csv");
							File program = new File("lpinputs/program7.csv");
							LPInputReader inputreader = PlainLPInputReader.getFileInputReader(program, pattern, conf.getMyId());
							SequentialProtocolProducer sseq = new SequentialProtocolProducer();
							for (int i = 0; i < 5; i++) {
								LPPrefix prefix = new PlainSpdzLPPrefix(inputreader, provider);
								ProtocolProducer lpsolver = new LPSolverCircuit(
										prefix.getTableau(),
										prefix.getUpdateMatrix(), 
										prefix.getPivot(), 
										provider, provider);
								SInt sout = provider.getSInt();
								OInt out = provider.getOInt();
								ProtocolProducer outputter = provider.getOptimalValueCircuit(
										prefix.getUpdateMatrix(), 
										prefix.getTableau().getB(), 
										prefix.getPivot(), sout);
								ProtocolProducer open = provider.getOpenIntCircuit(sout, out);
								ProtocolProducer seq = new SequentialProtocolProducer(prefix.getProducer(), lpsolver, outputter, open);
								sseq.append(seq);
							}
							long startTime = System.nanoTime();
							sce.runApplication(sseq);
							long endTime = System.nanoTime();
							System.out.println("============ Seq Time: " + ((endTime - startTime)/1000000));
						}
					}
				};
			}
		}, 2);

	}
	
	*/
	
}
