/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.DetermSecureRandom;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDummyImpl;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;

public class TestInputSumExample {

  @SuppressWarnings("unchecked")
  private static <ResourcePoolT extends ResourcePool> void runTest(
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> test, boolean dummy, int n) {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(n);
    for (int i = 1; i <= n; i++) {
      ports.add(9000 + i * 10);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(n, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int i : netConf.keySet()) {
      ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> suite;
      ResourcePoolT resourcePool = null;
      Network network = new KryoNetNetwork();
      network.init(netConf.get(i), 1);
      if (dummy) {
        BigInteger mod = new BigInteger(
            "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
        suite =
            (ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric>) new DummyArithmeticProtocolSuite(
                mod, 150);
        resourcePool = (ResourcePoolT) new DummyArithmeticResourcePoolImpl(i, n, network,
            new Random(), new DetermSecureRandom(), mod);
      } else {
        suite = (ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric>) new SpdzProtocolSuite(150);
        resourcePool = (ResourcePoolT) new SpdzResourcePoolImpl(i, n, network, new Random(),
            new DetermSecureRandom(), new SpdzStorageDummyImpl(i, n));
      }      
      TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric>(
              new SecureComputationEngineImpl<>(suite,
                  new BatchedProtocolEvaluator<>(new BatchedStrategy<>())),
              resourcePool);
      conf.put(i, ttc);
    }
    TestThreadRunner.run(test, conf);

  }

  @Test
  public <ResourcePoolT extends ResourcePool> void testInput() throws Exception {
    final TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> f =
        new TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                InputSumExample.runApplication(conf.sce, conf.resourcePool);
              }
            };
          }

      ;
        };
    runTest(f, false, 3);
  }

  @Test
  public <ResourcePoolT extends ResourcePool> void testInput_dummy() throws Exception {
    final TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> f =
        new TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                InputSumExample.runApplication(conf.sce, conf.resourcePool);
              }
            };
          }

      ;
        };
    runTest(f, true, 3);
  }
  
  @Test
  public void testInputCmdLine() throws Exception {
    Runnable p1 = new Runnable() {
      
      @Override
      public void run() {
        try {
          InputSumExample.main(new String[]{"-i", "1", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s", "dummyArithmetic"});
        } catch (IOException e) {
          System.exit(-1);
        }
      }
    };
    
    Runnable p2 = new Runnable() {
      
      @Override
      public void run() {
        try {
          InputSumExample.main(new String[]{"-i", "2", "-p", "1:localhost:8081", "-p", "2:localhost:8082", "-s", "dummyArithmetic"});
        } catch (IOException e) {
          System.exit(-1);
        }
      }
    }; 
    Thread t1 = new Thread(p1);
    Thread t2 = new Thread(p2);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
