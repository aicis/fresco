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
package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.lib.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.junit.Test;


/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestDummyProtocolSuite {

  private void runTest(TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f, EvaluationStrategy evalStrategy) throws Exception {
    // The dummy protocol suite has the nice property that it can be run by just one player.
    int noPlayers = 1;
    Level logLevel = Level.INFO;

    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(noPlayers);
    for (int i = 1; i <= noPlayers; i++) {
      ports.add(9000 + i * 10);
    }

    Map<Integer, NetworkConfiguration> netConf = TestConfiguration
        .getNetworkConfigurations(noPlayers, ports);
    Map<Integer, TestThreadConfiguration> conf = new HashMap<>();
    for (int playerId : netConf.keySet()) {
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc = new TestThreadConfiguration<>();
      ttc.netConf = netConf.get(playerId);
      ProtocolEvaluator<ResourcePoolImpl> evaluator = EvaluationStrategy.fromEnum(evalStrategy);
      ttc.sceConf = new TestSCEConfiguration(new DummyProtocolSuite(), NetworkingStrategy.KRYONET,
          evaluator, ttc.netConf, false);
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  @Test
  public void test_Mult32x32_Sequential() throws Exception {
    runTest(new BristolCryptoTests.Mult32x32Test(true), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_AES_Sequential() throws Exception {
    runTest(new BristolCryptoTests.AesTest(true), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_AES_SequentialBatched() throws Exception {
    runTest(new BristolCryptoTests.AesTest(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_DES_Sequential() throws Exception {
    runTest(new BristolCryptoTests.DesTest(true), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_SHA1_Sequential() throws Exception {
    runTest(new BristolCryptoTests.Sha1Test(true), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_SHA256_Sequential() throws Exception {
    runTest(new BristolCryptoTests.Sha256Test(true), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_comparison() throws Exception {
    runTest(new ComparisonBooleanTests.TestGreaterThan(), EvaluationStrategy.SEQUENTIAL);
  }

}
