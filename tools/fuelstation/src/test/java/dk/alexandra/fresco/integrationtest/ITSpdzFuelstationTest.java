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
package dk.alexandra.fresco.integrationtest;

import dk.alexandra.fresco.Application;
import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.arithmetic.MiMCTests;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.statistics.DeaSolver;
import dk.alexandra.fresco.lib.statistics.DEASolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {Application.class})
public class ITSpdzFuelstationTest {

  @LocalServerPort
  private int port;

  private void runTest(TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) throws Exception {

    // Since SCAPI currently does not work with ports > 9999 we use fixed
    // ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<Integer>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i);
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>();
      ttc.netConf = netConf.get(playerId);

      ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> suite =
          new SpdzProtocolSuite(150, PreprocessingStrategy.FUELSTATION, "http://localhost:" + port);

      ProtocolEvaluator<SpdzResourcePool, ProtocolBuilderNumeric> evaluator =
          EvaluationStrategy.fromEnum(evalStrategy);
      ttc.sceConf = new TestSCEConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>(suite,
          NetworkingStrategy.KRYONET, evaluator, ttc.netConf, false);
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_mimc_same_enc() throws Exception {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_division() throws Exception {
    runTest(new DivisionTests.TestSecretSharedDivision<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_dea() throws Exception {
    runTest(new RandomDataDeaTest<>(2, 1, 5, 1, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_mult_single() throws Exception {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

}
