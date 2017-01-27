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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.alexandra.fresco.IntegrationTest;
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
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;

public class TestTinyTables {

	private static final int TRIPLES_BATCH_SIZE = 4096;
	
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

				/*
				 * Set path where the generated TinyTables should be stored
				 */
				((TinyTablesPreproConfiguration) config).setTinyTablesFile(new File(
						getFilenameForTest(playerId, name)));

				/*
				 * 
				 */
				((TinyTablesPreproConfiguration) config).setTriplesBatchSize(TRIPLES_BATCH_SIZE);

				((TinyTablesPreproConfiguration) config).setTriplesFile(new File(
						"triples_" + playerId));

				protocolSuite = TinyTablesPreproProtocolSuite.getInstance(playerId);
			} else {
				config = new TinyTablesConfiguration();
				protocolSuite = TinyTablesProtocolSuite.getInstance(playerId);

				/*
				 * Set path where TinyTables generated during preprocessing can
				 * be found
				 */
				File tinyTablesFile = new File(getFilenameForTest(playerId, name));
				((TinyTablesConfiguration) config).setTinyTablesFile(tinyTablesFile);
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

	}

	/*
	 * Helper methods
	 */

	private String getFilenameForTest(int playerId, String name) {
		return "tinytables/TinyTables_" + name + "_" + playerId;
	}

	private static void deleteFileOrFolder(final Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
			@Override public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
					throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override public FileVisitResult visitFileFailed(final Path file, final IOException e) {
				return handleException(e);
			}

			private FileVisitResult handleException(final IOException e) {
				e.printStackTrace(); // replace with more robust error handling
				return FileVisitResult.TERMINATE;
			}

			@Override public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
					throws IOException {
				if(e!=null) {
					return handleException(e);
				}
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	};
	
	/*
	 * Basic tests
	 */

	//ensure that the tinytables folder is new for each test and is deleted upon exiting each test.
	@Before
	public void checkFolderExists() throws IOException{
		File f = new File("tinytables");
		if(f.exists()) {
			deleteFileOrFolder(f.toPath());
			f.mkdir();
		} else {
			f.mkdir();
		}
	}
	
	@After
	public void removeFolder() throws IOException{
		File f = new File("tinytables");
		if(f.exists()) {
			deleteFileOrFolder(f.toPath());
		}
	}
	
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
	@Category(IntegrationTest.class)	
	@Test
	public void testMult() throws Exception {
		runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, true,
				"testMult");
		runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, false,
				"testMult");
	}

	@Category(IntegrationTest.class)
	@Test
	public void testAES() throws Exception {
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, true, "testAES");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, false, "testAES");
	}

	@Category(IntegrationTest.class)
	@Test
	public void testAES_parallel() throws Exception {
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, true,
				"testAESParallel");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, false,
				"testAESParallel");
	}

	@Category(IntegrationTest.class)
	@Test
	public void testAES_parallel_batched() throws Exception {
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL_BATCHED, true,
				"testAESParallelBatched");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL_BATCHED, false,
				"testAESParallelBatched");
	}

	@Category(IntegrationTest.class)
	@Test
	public void test_DES() throws Exception {
		runTest(new BristolCryptoTests.DesTest(), EvaluationStrategy.SEQUENTIAL, true, "testDES");
		runTest(new BristolCryptoTests.DesTest(), EvaluationStrategy.SEQUENTIAL, false, "testDES");
	}

	@Category(IntegrationTest.class)
	@Test
	public void test_SHA1() throws Exception {
		runTest(new BristolCryptoTests.Sha1Test(), EvaluationStrategy.SEQUENTIAL, true, "testSHA1");
		runTest(new BristolCryptoTests.Sha1Test(), EvaluationStrategy.SEQUENTIAL, false, "testSHA1");
	}

	@Category(IntegrationTest.class)
	@Test
	public void test_SHA256() throws Exception {
		runTest(new BristolCryptoTests.Sha256Test(), EvaluationStrategy.SEQUENTIAL, true,
				"testSHA256");
		runTest(new BristolCryptoTests.Sha256Test(), EvaluationStrategy.SEQUENTIAL, false,
				"testSHA256");
	}
	
}
