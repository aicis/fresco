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

import org.junit.BeforeClass;
import org.junit.Ignore;
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
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.lib.arithmetic.SortingTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfigurationFromProperties;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;

public class TestSpdzSorting {
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

			// This fixes parameters, e.g., security parameter 80 is always
			// used.
			// To run tests with varying parameters, do as in the BGW case with
			// different thresholds.
			SpdzConfiguration spdzConf = new SpdzConfigurationFromProperties();
			ttc.protocolSuiteConf = spdzConf;
			boolean useSecureConnection = false; // No tests of secure
													// connection
													// here.
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			ProtocolSuite suite = SpdzProtocolSuite.getInstance(playerId);
			ProtocolEvaluator evaluator = EvaluationStrategy
					.fromEnum(evalStrategy);
			dk.alexandra.fresco.framework.sce.resources.storage.Storage storage = null;
			switch (storageStrategy) {
			case IN_MEMORY:
				storage = inMemStore;
				break;
			case MYSQL:
				break;
			}
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator,
					noOfThreads, noOfVMThreads, ttc.netConf, storage,
					useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}

	private static InMemoryStorage inMemStore = new InMemoryStorage();

	/**
	 * Makes sure that the preprocessed data exists in the storage's used in
	 * this test class.
	 */
	@BeforeClass
	public static void initStorage() {
		Reporter.init(Level.INFO);
		// dk.alexandra.fresco.framework.sce.resources.storage.Storage[]
		// storages = new
		// dk.alexandra.fresco.framework.sce.resources.storage.Storage[] {
		// inMemStore, mySQLStore };
		dk.alexandra.fresco.framework.sce.resources.storage.Storage[] storages = new dk.alexandra.fresco.framework.sce.resources.storage.Storage[] { inMemStore };
		InitializeStorage.initStorage(storages, noOfParties, 10000, 1000,
				100000, 100);
	}

	@Test
	public void test_isSorted() throws Exception {
		runTest(new SortingTests.TestIsSorted(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_compareAndSwap() throws Exception {
		runTest(new SortingTests.TestCompareAndSwap(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
	@Test
	public void test_Sort() throws Exception {
		runTest(new SortingTests.TestSort(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
	@Test
	@Ignore
	public void test_Big_Sort() throws Exception {
		runTest(new SortingTests.TestBigSort(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
}
