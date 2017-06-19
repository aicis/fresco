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
package dk.alexandra.fresco.suite.dummy.arithmetic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.config.DummyArithmeticConfiguration;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes
 * running a single test using different parameters quite easy.
 *
 */
public abstract class AbstractDummyArithmeticTest {

	protected void runTest(TestThreadRunner.TestThreadFactory f, EvaluationStrategy evalStrategy, NetworkingStrategy network, int noOfParties) throws Exception {
		Level logLevel = Level.INFO;

		int noOfVMThreads = 1;
		int noOfThreads = 1;
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

			BigInteger mod = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
			DummyArithmeticConfiguration dummyConf = new DummyArithmeticConfiguration() {
              
              @Override
              public ProtocolSuite createProtocolSuite(int myPlayerId) {
                return new DummyArithmeticProtocolSuite(this);
              }
              
              @Override
              public BigInteger getModulus() {
                return mod;
              }
              
              @Override
              public int getMaxBitLength() {
                return mod.bitLength()/2-1;
              }
            };

        
			boolean useSecureConnection = false; // No tests of secure
													// connection
													// here.

			ProtocolEvaluator evaluator = EvaluationStrategy.fromEnum(evalStrategy);
			Storage storage = new InMemoryStorage();			
      ttc.sceConf = new TestSCEConfiguration(dummyConf, network, evaluator, noOfThreads,
          noOfVMThreads, ttc.netConf,
          storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		TestThreadRunner.run(f, conf);
	}
}
