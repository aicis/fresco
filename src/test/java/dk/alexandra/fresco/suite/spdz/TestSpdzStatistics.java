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
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.lib.math.integer.stat.StatisticsTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

public class TestSpdzStatistics {

	private static final int noOfParties = 2;
	private Map<Integer, TestThreadConfiguration> conf; 
	
	private void configure(EvaluationStrategy evalStrategy,
			StorageStrategy storageStrategy) {
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
		conf = new HashMap<Integer, TestThreadConfiguration>();
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
			boolean useSecureConnection = false; // No tests of secure
													// connection
													// here.
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			ProtocolSuite suite = SpdzProtocolSuite.getInstance(playerId);
			ProtocolEvaluator evaluator = EvaluationStrategy
					.fromEnum(evalStrategy);
			dk.alexandra.fresco.framework.sce.resources.storage.Storage storage = inMemStore;
			
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator,
					noOfThreads, noOfVMThreads, ttc.netConf, storage,
					useSecureConnection);
			conf.put(playerId, ttc);
		}
	}

	private void runTest(TestThreadFactory f) throws Exception {
		TestThreadRunner.run(f, conf);
	}

	private static InMemoryStorage inMemStore = new InMemoryStorage();
	
	@Test
	public void test_Exiting_Variable_Sequential() throws Exception {
		configure(EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
		runTest(new StatisticsTests.TestStatistics());
	}
}
