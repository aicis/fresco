/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.PerformanceLogger.Flag;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.DetermSecureRandom;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractDummyArithmeticTest {

  /**
   * Runs test with default modulus and no performance logging. i.e. standard test setup.
   */
  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, NetworkingStrategy strategy, int noOfParties)
          throws Exception {
    BigInteger mod = new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
    runTest(f, evalStrategy, strategy, noOfParties, mod, null);
  }

  /**
   * Runs test with all parameters free. Only the starting port of 9000 is chosen by default.
   */
  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, NetworkingStrategy networkStrategy, int noOfParties,
      BigInteger mod, EnumSet<Flag> performanceLoggerFlags) throws Exception {
    List<Integer> ports = new ArrayList<Integer>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>>();
    for (int playerId : netConf.keySet()) {

      NetworkConfiguration partyNetConf = netConf.get(playerId);

      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(mod, 200);

      boolean useSecureConnection = false; // No tests of secure
      // connection
      // here.

      ProtocolEvaluator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> evaluator =
          EvaluationStrategy.fromEnum(evalStrategy);
      PerformanceLogger pl = null;
      if (performanceLoggerFlags != null && !performanceLoggerFlags.isEmpty()) {
        pl = new PerformanceLogger(playerId, performanceLoggerFlags);
      }
      Network network =
          NetworkCreator.getNetworkFromConfiguration(networkStrategy, partyNetConf, pl);
      DummyArithmeticResourcePool rp = new DummyArithmeticResourcePoolImpl(playerId, noOfParties,
          network, new Random(0), new DetermSecureRandom(), mod);
      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>(
              partyNetConf,
              new TestSCEConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>(ps,
                  evaluator, partyNetConf, useSecureConnection, pl),
              rp);
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }
}
