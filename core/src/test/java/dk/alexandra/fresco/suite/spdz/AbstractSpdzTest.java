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
package dk.alexandra.fresco.suite.spdz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes
 * running a single test using different parameters quite easy.
 *
 */
public abstract class AbstractSpdzTest {

	protected void runTest(TestThreadRunner.TestThreadFactory f, EvaluationStrategy evalStrategy, NetworkingStrategy network,
			PreprocessingStrategy preProStrat, int noOfParties) throws Exception {
		Level logLevel = Level.INFO;

		// Since SCAPI currently does not work with ports > 9999 we use fixed
		// ports
		// here instead of relying on ephemeral ports which are often > 9999.
		int noOfVMThreads = 3;
		int noOfThreads = 3;
		List<Integer> ports = new ArrayList<Integer>(noOfParties);
		for (int i = 1; i <= noOfParties; i++) {
			ports.add(9000 + i * noOfVMThreads*(noOfParties-1));
		}

		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(noOfParties, ports,
				logLevel);
		Map<Integer, TestThreadRunner.TestThreadConfiguration> conf = new HashMap<Integer, TestThreadRunner.TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadRunner.TestThreadConfiguration ttc = new TestThreadRunner.TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);

			SpdzConfiguration spdzConf = new SpdzConfiguration() {

				@Override
				public PreprocessingStrategy getPreprocessingStrategy() {
					return preProStrat;
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
			boolean useSecureConnection = false; // No tests of secure
													// connection
													// here.

			ProtocolSuite suite = new SpdzProtocolSuite();
			ProtocolEvaluator evaluator = EvaluationStrategy.fromEnum(evalStrategy);
			Storage storage = new InMemoryStorage();
			if (preProStrat == PreprocessingStrategy.STATIC) {
				storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
			}
			ttc.sceConf = new TestSCEConfiguration(suite, network, evaluator, noOfThreads, noOfVMThreads, ttc.netConf,
					storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}
}
