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
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.ninja.prepro.DummyPreprocessingFactory;

public class TestNinjaBasics {

	private void runTest(TestThreadFactory f, EvaluationStrategy evalStrategy, boolean preprocessing) throws Exception {		
		int noPlayers = 2;
		if(preprocessing) {
			noPlayers = 1;
		}
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
	public void testInput() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestInput(), EvaluationStrategy.SEQUENTIAL, true);
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		runTest(new BasicBooleanTests.TestInput(), EvaluationStrategy.SEQUENTIAL, false);
	}
	
	@Test
	public void testXOR() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestXOR(), EvaluationStrategy.SEQUENTIAL, true);
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		runTest(new BasicBooleanTests.TestXOR(), EvaluationStrategy.SEQUENTIAL, false);
	}
	
	@Test
	public void testAND() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestAND(), EvaluationStrategy.SEQUENTIAL, true);
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		runTest(new BasicBooleanTests.TestAND(), EvaluationStrategy.SEQUENTIAL, false);
	}
	
	@Test
	public void testNOT() throws Exception {
		try {
			runTest(new BasicBooleanTests.TestNOT(), EvaluationStrategy.SEQUENTIAL, true);
		} catch(TestFrameworkException ex) {
			//likely an assertion error
		}
		runTest(new BasicBooleanTests.TestNOT(), EvaluationStrategy.SEQUENTIAL, false);
	}
	
	@Test
	public void testBasicProtocols() throws Exception	{
		try {
			runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.SEQUENTIAL, true);
		} catch(TestFrameworkException ex) {
			runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.SEQUENTIAL, false);
		}
	}
	
	@Test
	public void testBasicProtocols_parallel() throws Exception	{
		try {
			runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.PARALLEL, true);
		} catch(TestFrameworkException ex) {
			runTest(new BasicBooleanTests.TestBasicProtocols(), EvaluationStrategy.PARALLEL, false);
		}
	}
}
