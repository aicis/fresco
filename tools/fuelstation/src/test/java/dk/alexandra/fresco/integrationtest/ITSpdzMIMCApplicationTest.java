/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.integrationtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import dk.alexandra.fresco.Application;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.arithmetic.MiMCTests;
import dk.alexandra.fresco.lib.statistics.DEASolverTests;
import dk.alexandra.fresco.rest.FuelEndpoint;
import dk.alexandra.fresco.services.DataGeneratorImpl;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.storage.rest.RetrieverThread;
import org.junit.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, 
classes= {DataGeneratorImpl.class, Application.class})
public class ITSpdzMIMCApplicationTest {

	@LocalServerPort
	private int port;

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
				public int getMaxBitLength() {
					return 150;
				}

				@Override
				public PreprocessingStrategy getPreprocessingStrategy() {
					return PreprocessingStrategy.FUELSTATION;
				}

				@Override
				public String fuelStationBaseUrl() {
					return "http://localhost:"+port;
				}
			};
			ttc.protocolSuiteConf = spdzConf;
			boolean useSecureConnection = false; // No tests of secure
			// connection
			// here.
			int noOfVMThreads = 1;
			int noOfThreads = 1;
			ProtocolSuite suite = new SpdzProtocolSuite();
			ProtocolEvaluator evaluator = EvaluationStrategy
					.fromEnum(evalStrategy);
			ttc.sceConf = new TestSCEConfiguration(suite, NetworkingStrategy.KRYONET, evaluator,
					noOfThreads, noOfVMThreads, ttc.netConf, new InMemoryStorage(),
					useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}

	@Test
	public void test_mimc_same_enc() throws Exception {
		runTest(new MiMCTests.TestMiMCEncSameEnc(),
				EvaluationStrategy.SEQUENTIAL_BATCHED, StorageStrategy.IN_MEMORY);				
	}
	
	@Test
	public void test_dea() throws Exception {
		runTest(new DEASolverTests.TestDEASolver(5, 1, 5, 1),
				EvaluationStrategy.PARALLEL_BATCHED, StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_mult_single() throws Exception {
		runTest(new BasicArithmeticTests.TestSumAndMult(),
				EvaluationStrategy.SEQUENTIAL_BATCHED, StorageStrategy.IN_MEMORY);
	}
}
