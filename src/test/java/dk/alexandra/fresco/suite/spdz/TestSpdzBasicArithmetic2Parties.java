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
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

/**
 * Basic arithmetic tests using the SPDZ protocol suite with 2 parties. Have to
 * hardcode the number of parties for now, since the storage is currently build
 * to handle a fixed number of parties.
 * 
 */
public class TestSpdzBasicArithmetic2Parties {

	private static final int noOfParties = 2;

	private void runTest(TestThreadFactory f, EvaluationStrategy evalStrategy,
			StorageStrategy storageStrategy) throws Exception {
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
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
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
			ProtocolSuite suite = new SpdzProtocolSuite();
			ProtocolEvaluator evaluator = EvaluationStrategy
					.fromEnum(evalStrategy);
			dk.alexandra.fresco.framework.sce.resources.storage.Storage storage = inMemStore;
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator,
					noOfThreads, noOfVMThreads, ttc.netConf, storage,
					useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}

	private static InMemoryStorage inMemStore = new InMemoryStorage();
	
	@Test
	public void test_Copy_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestCopyProtocol(),
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_Input_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestInput(),
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_OutputToTarget_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestOutputToSingleParty(),
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_AddPublicValue_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestAddPublicValue(),
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_Lots_Of_Inputs_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestLotsOfInputs(),
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_MultAndAdd_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestLotsOfInputs(),
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_Lots_Of_Inputs_Parallel() throws Exception {
		runTest(new BasicArithmeticTests.TestLotsOfInputs(),
				EvaluationStrategy.PARALLEL, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_Lots_Of_Inputs_ParallelBatched() throws Exception {
		runTest(new BasicArithmeticTests.TestLotsOfInputs(),
				EvaluationStrategy.PARALLEL_BATCHED, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_Sum_And_Output_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestSumAndMult(),
				EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_Sum_And_Output_ParallelBatched() throws Exception {
		runTest(new BasicArithmeticTests.TestSumAndMult(),
				EvaluationStrategy.PARALLEL_BATCHED, StorageStrategy.IN_MEMORY);
	}

	@Test
	public void test_Lots_Of_Inputs_SequentialBatched() throws Exception {
		runTest(new BasicArithmeticTests.TestLotsOfInputs(),
				EvaluationStrategy.SEQUENTIAL_BATCHED,
				StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_MinInfFrac_Sequential() throws Exception {
		runTest(new BasicArithmeticTests.TestMinInfFrac(),
				EvaluationStrategy.SEQUENTIAL,
				StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_MinInfFrac_SequentialBatched() throws Exception {
		runTest(new BasicArithmeticTests.TestMinInfFrac(),
				EvaluationStrategy.SEQUENTIAL_BATCHED,
				StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_MinInfFrac_Parallel() throws Exception {
		runTest(new BasicArithmeticTests.TestMinInfFrac(),
				EvaluationStrategy.PARALLEL,
				StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_MinInfFrac_ParallelBatched() throws Exception {
		runTest(new BasicArithmeticTests.TestMinInfFrac(),
				EvaluationStrategy.PARALLEL_BATCHED,
				StorageStrategy.IN_MEMORY);
	}
}
