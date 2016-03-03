package dk.alexandra.fresco.suite.ninja;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestFrameworkException;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.math.mult.BristolMultTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.ninja.prepro.DummyPreprocessingFactory;

public class TestNinjaAdvanced {

	private void runTest(TestThreadFactory f, EvaluationStrategy evalStrategy, boolean preprocessing) throws Exception {
		int noPlayers = 2;
		Level logLevel = Level.FINE;
		Reporter.init(logLevel);
		
		// Since SCAPI currently does not work with ports > 9999 we use fixed ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(noPlayers);
		for (int i=1; i<=noPlayers; i++) {
			ports.add(9000 + i);
		}
		
		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(noPlayers, ports, logLevel);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);
			NinjaConfiguration config = new NinjaConfiguration();
			config.setDummy(true);
			if(preprocessing) {
				config.setNinjaFactory(new DummyPreprocessingFactory());
			}
			ttc.protocolSuiteConf = config;
			boolean useSecureConnection = false; // No tests of secure connection here.
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			ProtocolSuite protocolSuite = NinjaProtocolSuite.getInstance(playerId);
			ProtocolEvaluator evaluator = EvaluationStrategy.fromEnum(evalStrategy);
			Storage storage = new InMemoryStorage();
			ttc.sceConf = new TestSCEConfiguration(protocolSuite, evaluator, noOfThreads, noOfVMThreads, ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);			
		}
		TestThreadRunner.run(f, conf);
	}
	
	@Test
	public void testMult() throws Exception {
		try {
			runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, true);
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		System.out.println("Got past prepro");
		runTest(new BristolMultTests.Mult32x32Test(), EvaluationStrategy.SEQUENTIAL, false);
	}
	
	@Test
	public void testAES() throws Exception {
		try {
			runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, true);
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		System.out.println("Got past prepro");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.SEQUENTIAL, false);
	}
	
	@Test
	public void testAES_parallel() throws Exception {
		try {
			runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, true);
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		System.out.println("Got past prepro");
		runTest(new BristolCryptoTests.AesTest(), EvaluationStrategy.PARALLEL, false);
	}
}
