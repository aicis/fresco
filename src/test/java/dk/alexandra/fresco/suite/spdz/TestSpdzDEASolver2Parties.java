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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.alexandra.fresco.IntegrationTest;
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
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.SQLStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.lib.statistics.DEASolverTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;


/**
 * Tests for the DEASolver.
 * 
 */
public class TestSpdzDEASolver2Parties {
	
	private void runTest(TestThreadFactory f, int noPlayers, int noOfVmThreads,
			EvaluationStrategy evalStrategy, StorageStrategy storageStrategy, boolean useDummyData)
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
				public int getMaxBitLength() {
					return 150;
				}

				@Override
				public PreprocessingStrategy getPreprocessingStrategy() {
					if(useDummyData) {
						return PreprocessingStrategy.DUMMY;
					} else {
						return PreprocessingStrategy.STATIC;
					}
				}

				@Override
				public String fuelStationBaseUrl() {
					return null;
				}
			};
			ttc.protocolSuiteConf = spdzConf;
			boolean useSecureConnection = false; // No tests of secure connection
												// here.
			int noOfPSThreads = 3;
			ProtocolSuite suite = SpdzProtocolSuite.getInstance(playerId);
			ProtocolEvaluator evaluator = EvaluationStrategy.fromEnum(evalStrategy);			
			Storage storage = null;
			switch (storageStrategy) {
			case IN_MEMORY:
				storage = inMemStore;
				break;
			case STREAMED_STORAGE:
				storage = new FilebasedStreamedStorageImpl(inMemStore);
				break;
			case SQL:
				storage = SQLStorage.getInstance();
			}
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator, noOfPSThreads, noOfVmThreads, ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
		for (int i : conf.keySet()) {
			SpdzProtocolSuite.getInstance(i).destroy();
		}
	}

	private static final InMemoryStorage inMemStore = new InMemoryStorage();
	private static final FilebasedStreamedStorageImpl streamedStorage = new FilebasedStreamedStorageImpl(inMemStore);

	@Test
	public void test_DEASolver_2_Parallel_batched_dummy() throws Exception {
		int numberOfVMThreads = 4;
		runTest(new DEASolverTests.TestDEASolver(5, 1, 30, 5), 2, numberOfVMThreads,
				EvaluationStrategy.PARALLEL_BATCHED, StorageStrategy.IN_MEMORY, true);
	}
	
	@Test
	public void test_DEASolver_2_Sequential_batched_dummy() throws Exception {
		runTest(new DEASolverTests.TestDEASolver(5, 1, 30, 5), 2, 1,
				EvaluationStrategy.SEQUENTIAL_BATCHED, StorageStrategy.IN_MEMORY, true);
	}

	// Using a non-batched evaulation strategy has extremely poor performance.
	// Hence the problem size is reduced
	// TODO figure out what the problem is
	@Category(IntegrationTest.class)
	@Test
	public void test_DEASolver_2_Sequential_dummy() throws Exception {
		runTest(new DEASolverTests.TestDEASolver(2, 1, 5, 1), 2, 1,
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY, true);
	}
	
	@Category(IntegrationTest.class)
	@Test
	public void test_DEASolver_2_Parallel_dummy() throws Exception {
		runTest(new DEASolverTests.TestDEASolver(5, 1, 5, 1), 2, 1,
				EvaluationStrategy.PARALLEL, StorageStrategy.IN_MEMORY, true);
	}
	
	// Ignoring the streamed tests since they take too long with respect to generating preprocessed material
	// Note that the poor performance of non-batched evaulation is most likely also the case here.
	//TODO: Maybe add the @Category(IntegrationTest.class) instead of @Ignore. 
	@Ignore
	@Test
	public void test_DEASolver_2_Sequential_batched_streamed() throws Exception {
		int noOfThreads = 1;
		InitializeStorage.cleanup();
		try {
			InitializeStorage.initStreamedStorage(streamedStorage, 2, noOfThreads, 20000, 500, 800000, 3000);
			runTest(new DEASolverTests.TestDEASolver(4, 1, 10, 2), 2, noOfThreads,
					EvaluationStrategy.SEQUENTIAL_BATCHED, StorageStrategy.STREAMED_STORAGE, false);
		} finally {
			InitializeStorage.cleanup();
		}
	}
	
	@Test
	@Ignore
	public void test_DEASolver_2_Parallel_streamed() throws Exception {
		int noOfThreads = 2;
		InitializeStorage.cleanup();
		try {
			InitializeStorage.initStreamedStorage(streamedStorage, 2, noOfThreads, 20000, 500, 800000, 3000);
			runTest(new DEASolverTests.TestDEASolver(4, 1, 10, 2), 2, noOfThreads,
					EvaluationStrategy.PARALLEL, StorageStrategy.STREAMED_STORAGE, false);
		} finally {
			InitializeStorage.cleanup();
		}
	}
	
	@Test
	@Ignore
	public void test_DEASolver_2_ParallelBatched_streamed() throws Exception {
		int noOfThreads = 2;		
		InitializeStorage.cleanup();
		try {
			InitializeStorage.initStreamedStorage(streamedStorage, 2, noOfThreads, 20000, 500, 800000, 3000);
			runTest(new DEASolverTests.TestDEASolver(4, 1, 10, 2), 2, noOfThreads,
					EvaluationStrategy.PARALLEL_BATCHED, StorageStrategy.STREAMED_STORAGE, false);
		} finally {
			InitializeStorage.cleanup();
		}
	}
	
	@Test
	@Ignore
	public void test_DEASolver_2_SequentialBatched_streamed() throws Exception {
		int noOfThreads = 2;		
		InitializeStorage.cleanup();
		try {
			InitializeStorage.initStreamedStorage(streamedStorage, 2, noOfThreads, 20000, 500, 800000, 3000);
			runTest(new DEASolverTests.TestDEASolver(4, 1, 10, 2), 2, noOfThreads,
					EvaluationStrategy.SEQUENTIAL_BATCHED, StorageStrategy.STREAMED_STORAGE, false);
		} finally {
			InitializeStorage.cleanup();
		}
	}	
}
