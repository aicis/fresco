/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.tinytables;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestFrameworkException;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.math.mult.BristolMultTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTableConfiguration;
import dk.alexandra.fresco.suite.tinytables.online.TinyTableProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablePreproConfiguration;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablePreproFactory;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablePreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTableStorage;

public class TestTinyTables {

	private void runTest(TestThreadFactory f, EvaluationStrategy evalStrategy,
			boolean preprocessing, String name) throws Exception {
		int noPlayers = 2;
		Level logLevel = Level.FINE;
		Reporter.init(logLevel);

		// Since SCAPI currently does not work with ports > 9999 we use fixed
		// ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(noPlayers);
		for (int i = 1; i <= noPlayers; i++) {
			ports.add(9000 + i);
		}

		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(
				noPlayers, ports, logLevel);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();

		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);

			ProtocolSuite protocolSuite = null;
			ProtocolEvaluator evaluator = null;

			ProtocolSuiteConfiguration config = null;
			if (preprocessing) {
				config = new TinyTablePreproConfiguration();
				((TinyTablePreproConfiguration) config)
						.setTinyTableFactory(new TinyTablePreproFactory());
				protocolSuite = TinyTablePreproProtocolSuite.getInstance(playerId);
			} else {
				config = new TinyTableConfiguration();
				protocolSuite = TinyTableProtocolSuite.getInstance(playerId);

				/*
				 * Load TinyTables from storage
				 */
				String filename = getFilenameForTest(playerId, name);
				TinyTableStorage storage = loadTinyTables(filename);
				
				if (storage != null) {
					Reporter.info("Found TinyTables for " + name + " for player " + playerId);
					TinyTableProtocolSuite.getInstance(playerId).setStorage(storage);
				} else {
					throw new MPCException("No TinyTables found for " + name + " for player "
							+ playerId);
				}
			}

			evaluator = EvaluationStrategy.fromEnum(evalStrategy);
			ttc.protocolSuiteConf = config;

			boolean useSecureConnection = false; // No tests of secure
													// connection here.
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			Storage storage = new InMemoryStorage();
			ttc.sceConf = new TestSCEConfiguration(protocolSuite, evaluator, noOfThreads,
					noOfVMThreads, ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);

		/*
		 * If preprocessing, save all TinyTables
		 */
		if (preprocessing) {
			for (int playerId : netConf.keySet()) {
				TinyTableStorage storage = TinyTablePreproProtocolSuite.getInstance(playerId)
						.getStorage();
				String filename = getFilenameForTest(playerId, name);
				storeTinyTables(storage, filename);
			}
		}
	}

	/*
	 * Helper methods
	 */
	
	private String getFilenameForTest(int playerId, String name) {
		return "tinytables/TinyTables_" + name + "_" + playerId;
	}
	
	private void storeTinyTables(TinyTableStorage tinyTableStorage, String filename) throws IOException {
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(tinyTableStorage);
		System.out.println("Saving tables in file " + filename);
		oos.close();
	}
	
	private TinyTableStorage loadTinyTables(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream is = new ObjectInputStream(fin);
		TinyTableStorage storage = (TinyTableStorage) is.readObject();
		is.close();
		return storage;
	}
	
	/*
	 * Basic tests
	 */
	
	@Test
	public void testInput() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestInput(), EvaluationStrategy.SEQUENTIAL, true,
					"testInput");
			runTest(new BasicBooleanTests.TestInput(), EvaluationStrategy.SEQUENTIAL, false,
					"testInput");
		} catch (TestFrameworkException ex) {
			// likely an assertion error
		}
	}

	@Test
	public void testXOR() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestXOR(), EvaluationStrategy.SEQUENTIAL, true, "testXOR");
			runTest(new BasicBooleanTests.TestXOR(), EvaluationStrategy.SEQUENTIAL, false, "testXOR");
		} catch (TestFrameworkException ex) {
			// likely an assertion error
		}
	}
	
	@Test
	public void testAND() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestAND(), EvaluationStrategy.SEQUENTIAL, true, "testAND");
			runTest(new BasicBooleanTests.TestAND(), EvaluationStrategy.SEQUENTIAL, false, "testAND");
		} catch (TestFrameworkException ex) {
			// likely an assertion error
		}
	}

	@Test
	public void testNOT() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestNOT(), EvaluationStrategy.SEQUENTIAL, true, "testNOT");
			runTest(new BasicBooleanTests.TestNOT(), EvaluationStrategy.SEQUENTIAL, false, "testNOT");
		} catch (TestFrameworkException ex) {
			// likely an assertion error
		}
	}

	@Test
	public void testBasicProtocols() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.SEQUENTIAL,
					true, "testBasicProtocols");
			runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.SEQUENTIAL,
					false, "testBasicProtocols");
		} catch (TestFrameworkException ex) {
			// likely an assertion error
		}
	}
	
	/*
	 * Advanced tests
	 */
	
	@Test
	public void testMult() throws Exception {
		try {
			runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, true, "testMult");
			runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, false, "testMult");
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
	}
	
	@Test
	public void testAES() throws Exception {
		try {
			runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, true, "testAES");
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, false,"testAES");
	}
	
	@Test
	public void testAES_parallel() throws Exception {
		try {
			runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, true, "testAESParallel");
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, false, "testAESParallel");
	}
	
	@Test
	public void testAES_parallel_batched() throws Exception {
		try {
			runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL_BATCHED, true, "testAESParallelBatched");
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL_BATCHED, false, "testAESParallelBatched");
	}
	
	@Test
	public void testGreaterThan() throws Exception{
		try {
			runTest(new ComparisonBooleanTests.TestGreaterThan(), EvaluationStrategy.SEQUENTIAL, true, "testGreaterThan");
			runTest(new ComparisonBooleanTests.TestGreaterThan(), EvaluationStrategy.SEQUENTIAL, false, "testGreaterThan");
		} catch(TestFrameworkException ex) {
			//likely an assertion error			
		}
	}
	
}
