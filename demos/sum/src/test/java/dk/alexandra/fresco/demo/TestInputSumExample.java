/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

public class TestInputSumExample {
  private static void runTest(TestThreadFactory test, int n) {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<Integer>(n);
    for (int i=1; i<=n; i++) {
      ports.add(9000 + i*10);
    }
    Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(n, ports, Level.FINE);
    Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
    for (int i : netConf.keySet()) {
      TestThreadConfiguration ttc = new TestThreadConfiguration();
      ttc.netConf = netConf.get(i);			
      ProtocolSuiteConfiguration suite = new SpdzConfiguration() {

        @Override
        public ProtocolSuite createProtocolSuite(int myPlayerId) {
          return new SpdzProtocolSuite(myPlayerId, this);
        }

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
      ttc.sceConf = new TestSCEConfiguration(suite, NetworkingStrategy.KRYONET, new SequentialEvaluator(), 1, 1, netConf.get(i), new InMemoryStorage(), false);
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);

  }

  @Test
  public void testInput() throws Exception {
    final TestThreadFactory f = new TestThreadFactory() {
      @Override
      public TestThread next(TestThreadConfiguration conf) {
        return new TestThread() {
          @Override
          public void test() throws Exception {		
            ResourcePoolImpl rp = SecureComputationEngineImpl.createResourcePool(conf.sceConf);
            InputSumExample.runApplication(conf.getMyId(), secureComputationEngine, rp);						
          }
        };
      };
    };		
    runTest(f, 3);
  }
}