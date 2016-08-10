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
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.math.mult.BristolMultTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesConfiguration;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproConfiguration;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproFactory;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;

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
				config = new TinyTablesPreproConfiguration();
				((TinyTablesPreproConfiguration) config)
						.setTinyTableFactory(new TinyTablesPreproFactory());

				/*
				 * We assume that both players are running on localhost in the
				 * tests.
				 */
				((TinyTablesPreproConfiguration) config).setHostname("localhost");
				((TinyTablesPreproConfiguration) config).setPort(9005);

				protocolSuite = TinyTablesPreproProtocolSuite.getInstance(playerId);
			} else {
				config = new TinyTablesConfiguration();
				protocolSuite = TinyTablesProtocolSuite.getInstance(playerId);

				/*
				 * Load TinyTables from storage
				 */
				String filename = getFilenameForTest(playerId, name);
				TinyTablesStorage storage = loadTinyTables(filename);

				if (storage != null) {
					Reporter.info("Found TinyTables for " + name + " for player " + playerId);
					TinyTablesProtocolSuite.getInstance(playerId).setStorage(storage);
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
				TinyTablesStorage storage = TinyTablesPreproProtocolSuite.getInstance(playerId)
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

	private void storeTinyTables(TinyTablesStorage tinyTablesStorage, String filename)
			throws IOException {
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(tinyTablesStorage);
		Reporter.info("Saving tables in file " + filename);
		oos.close();
	}

	private TinyTablesStorage loadTinyTables(String filename) throws IOException,
			ClassNotFoundException {
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream is = new ObjectInputStream(fin);
		TinyTablesStorage storage = (TinyTablesStorage) is.readObject();
		is.close();
		return storage;
	}

	/*
	 * Basic tests
	 */

	@Test
	public void testInput() throws Exception {
		runTest(new BasicBooleanTests.TestInput(), EvaluationStrategy.SEQUENTIAL, true, "testInput");
		runTest(new BasicBooleanTests.TestInput(), EvaluationStrategy.SEQUENTIAL, false,
				"testInput");
	}

	@Test
	public void testXOR() throws Exception {
		runTest(new BasicBooleanTests.TestXOR(), EvaluationStrategy.SEQUENTIAL, true, "testXOR");
		runTest(new BasicBooleanTests.TestXOR(), EvaluationStrategy.SEQUENTIAL, false, "testXOR");
	}

	@Test
	public void testAND() throws Exception {
		runTest(new BasicBooleanTests.TestAND(), EvaluationStrategy.SEQUENTIAL, true, "testAND");
		runTest(new BasicBooleanTests.TestAND(), EvaluationStrategy.SEQUENTIAL, false, "testAND");
	}

	@Test
	public void testNOT() throws Exception {
		runTest(new BasicBooleanTests.TestNOT(), EvaluationStrategy.SEQUENTIAL, true, "testNOT");
		runTest(new BasicBooleanTests.TestNOT(), EvaluationStrategy.SEQUENTIAL, false, "testNOT");
	}

	@Test
	public void testBasicProtocols() throws Exception {
		runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.SEQUENTIAL, true,
				"testBasicProtocols");
		runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.SEQUENTIAL, false,
				"testBasicProtocols");
	}

	/*
	 * Advanced tests
	 */

	@Test
	public void testMult() throws Exception {
		runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, true,
				"testMult");
		runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, false,
				"testMult");
	}

	@Test
	public void testAES() throws Exception {
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, true, "testAES");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, false, "testAES");
	}

	@Test
	public void testAES_parallel() throws Exception {
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, true,
				"testAESParallel");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, false,
				"testAESParallel");
	}

	@Test
	public void testAES_parallel_batched() throws Exception {
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL_BATCHED, true,
				"testAESParallelBatched");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL_BATCHED, false,
				"testAESParallelBatched");
	}

	@Test
	public void test_DES() throws Exception {
		runTest(new BristolCryptoTests.DesTest(), EvaluationStrategy.SEQUENTIAL, true, "testDES");
		runTest(new BristolCryptoTests.DesTest(), EvaluationStrategy.SEQUENTIAL, false, "testDES");
	}

	@Test
	public void test_SHA1() throws Exception {
		runTest(new BristolCryptoTests.Sha1Test(), EvaluationStrategy.SEQUENTIAL, true, "testSHA1");
		runTest(new BristolCryptoTests.Sha1Test(), EvaluationStrategy.SEQUENTIAL, false, "testSHA1");
	}

	@Test
	public void test_SHA256() throws Exception {
		runTest(new BristolCryptoTests.Sha256Test(), EvaluationStrategy.SEQUENTIAL, true,
				"testSHA256");
		runTest(new BristolCryptoTests.Sha256Test(), EvaluationStrategy.SEQUENTIAL, false,
				"testSHA256");
	}

}
