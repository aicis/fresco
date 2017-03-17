/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.bgw;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Ignore;
import org.junit.Test;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.lib.arithmetic.ComparisonTests;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.bgw.configuration.BgwConfiguration;

public class TestBgwComparison {

	private void runTest(TestThreadFactory f, int noPlayers, final int threshold, EvaluationStrategy evalStrategy) throws Exception {
		Level logLevel = Level.FINE;
		Reporter.init(logLevel);
		
		// Since SCAPI currently does not work with ports > 9999 we use fixed ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(noPlayers);
		for (int i=1; i<=noPlayers; i++) {
			ports.add(9000 + i*10);
		}
		
		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(noPlayers, ports,logLevel);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);
			ttc.protocolSuiteConf = new BgwConfiguration() {
				@Override
				public int getThreshold() {
					return threshold;
				}

				@Override
				public BigInteger getModulus() {
					return new BigInteger("618970019642690137449562111");
				}
			};
			NetworkConfiguration net = netConf.get(playerId);
			boolean useSecureConnection = false; // No tests of secure connection here.
			ProtocolSuite suite = new BgwProtocolSuite();
			ProtocolEvaluator evaluator = EvaluationStrategy.fromEnum(evalStrategy);			
			Storage storage = new InMemoryStorage();
			int noOfThreads = 1;
			int noOfVMThreads = 3;
			ttc.sceConf = new TestSCEConfiguration(suite, NetworkingStrategy.KRYONET, evaluator, noOfThreads, noOfVMThreads, net, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}

	//BgwFactory cannot yet support the needed interfaces. Requires preprocessed data. We refer to SPDZ for this.
	@Ignore
	@Test
	public void test_compareLT_3_1_Sequential() throws Exception {
		runTest(new ComparisonTests.TestCompareLT(), 3, 1,
				EvaluationStrategy.SEQUENTIAL);
	}
	
	@Ignore
	@Test
	public void test_compareEQ_3_1_Sequential() throws Exception {
		runTest(new ComparisonTests.TestCompareEQ(), 3, 1,
				EvaluationStrategy.SEQUENTIAL);
	}
	
}
