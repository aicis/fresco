package dk.alexandra.fresco.suite.spdz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
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
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

public class TestBasicArithmeticWithEncryption {

	private void runTest(TestThreadFactory f, int noOfParties) throws Exception {
		Level logLevel = Level.INFO;

		// Since SCAPI currently does not work with ports > 9999 we use fixed
		// ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(noOfParties);
		int noOfVMThreads = 2;
		for (int i = 1; i <= noOfParties; i++) {
			ports.add(9000 + i*10);
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
			boolean useSecureConnection = true; 
			
			int noOfThreads = 3;
			ProtocolSuite suite = new SpdzProtocolSuite();
			ProtocolEvaluator evaluator = EvaluationStrategy
					.fromEnum(EvaluationStrategy.SEQUENTIAL_BATCHED);
			dk.alexandra.fresco.framework.sce.resources.storage.Storage storage = new InMemoryStorage();
			ttc.sceConf = new TestSCEConfiguration(suite, NetworkingStrategy.KRYONET,
					evaluator, noOfThreads, noOfVMThreads, ttc.netConf, storage,
					useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}
	
	@Test
	public void testManyMultsWithEnc2Parties() throws Exception {
		runTest(new BasicArithmeticTests.TestLotsMult(), 2);
	}
	
	@Test
	public void testManyMultsWithEnc3Parties() throws Exception {
		runTest(new BasicArithmeticTests.TestLotsMult(), 3);
	}
}
