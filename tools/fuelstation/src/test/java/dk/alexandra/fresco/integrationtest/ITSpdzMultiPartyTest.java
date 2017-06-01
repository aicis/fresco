package dk.alexandra.fresco.integrationtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.services.DataGeneratorImpl;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, 
classes= {DataGeneratorImpl.class, Application.class})
public class ITSpdzMultiPartyTest {

	@LocalServerPort
	private int port;

	@Autowired
	private DataGeneratorImpl generator;
	
	private void runTest(TestThreadFactory f, EvaluationStrategy evalStrategy,
			StorageStrategy storageStrategy, int noOfParties) throws Exception {
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
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator,
					noOfThreads, noOfVMThreads, ttc.netConf, new InMemoryStorage(),
					useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}
	
	@Test
	public void test_3_parties_mult_single() throws Exception {
		generator.setNoOfParties(3);
		generator.resetAndInit();
		runTest(new BasicArithmeticTests.TestSumAndMult(),
				EvaluationStrategy.SEQUENTIAL_BATCHED, StorageStrategy.IN_MEMORY, 3);		
	}
}
