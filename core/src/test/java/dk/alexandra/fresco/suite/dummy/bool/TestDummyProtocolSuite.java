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
import dk.alexandra.fresco.lib.collections.sort.CollectionsSortingTests;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.field.bool.generic.FieldBoolTests;
import dk.alexandra.fresco.lib.math.bool.add.AddTests;
import dk.alexandra.fresco.lib.math.bool.log.LogTests;
import dk.alexandra.fresco.lib.math.bool.mult.MultTests;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
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
  public void test_MD5_Sequential() throws Exception {
    runTest(new BristolCryptoTests.MD5Test(true), EvaluationStrategy.SEQUENTIAL);
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
  
  //TODO Perhaps this test should be moved to a dedicated BasicLogicBuilder
  // test class, as the exception is thrown there 
  @Test (expected=RuntimeException.class)
  public void test_comparisonBadLength() throws Exception {
    runTest(new ComparisonBooleanTests.TestGreaterThanUnequalLength(), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_comparisonPar() throws Exception {
    runTest(new ComparisonBooleanTests.TestGreaterThanPar(), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_equality() throws Exception {
    runTest(new ComparisonBooleanTests.TestBinaryEqual(), EvaluationStrategy.SEQUENTIAL);
  }

  @Test //Tested protocol is not referenced and is likely replaced by
  // the one tested above
  public void test_equalityBasicProtocol() throws Exception {
    runTest(new ComparisonBooleanTests.TestBinaryEqualBasicProtocol(), EvaluationStrategy.SEQUENTIAL);
  }
  //collections.sort
  @Test
  public void test_Uneven_Odd_Even_Merge_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestOddEvenMerge(), EvaluationStrategy.SEQUENTIAL);
  }
  @Ignore //for now
  @Test
  public void test_Uneven_Odd_Even_Merge_Rec_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestOddEvenMergeRec(), EvaluationStrategy.SEQUENTIAL);
  }
  @Ignore //for now
  @Test
  public void test_Uneven_Odd_Even_Merge_Rec_Large_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestOddEvenMergeRecLarge(), EvaluationStrategy.SEQUENTIAL);
  }
  @Ignore //for now
  @Test
  public void test_Keyed_Compare_And_Swap_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestKeyedCompareAndSwap(), EvaluationStrategy.SEQUENTIAL);
  }
  
  //TODO
  @Test
  public void test_Compare_And_Swap() throws Exception {
//    runTest(new CompareTests.TestCompareAndSwap(), EvaluationStrategy.SEQUENTIAL);
  }
  //TODO Utilbuilder is gone
  @Test
  public void test_Debug_Marker() throws Exception {
//    runTest(new DebugTests.TestOpenAndPrint(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  //lib.field.bool.generic
  @Test
  public void test_XNor() throws Exception {
    runTest(new FieldBoolTests.TestXNorFromXorAndNotProtocol(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_OrFromXorAnd() throws Exception {
    runTest(new FieldBoolTests.TestOrFromXorAndProtocol(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_OrFromCopy() throws Exception {
    runTest(new FieldBoolTests.TestOrFromCopyConstProtocol(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_NandFromAndAndNot() throws Exception {
    runTest(new FieldBoolTests.TestNandFromAndAndNotProtocol(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }
  
  @Test
  public void test_AndFromCopy() throws Exception {
    runTest(new FieldBoolTests.TestAndFromCopyConstProtocol(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }  
  @Test
  public void test_NotFromXor() throws Exception {
    runTest(new FieldBoolTests.TestNotFromXorProtocol(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }
  //lib.math.bool
  @Test
  public void test_One_Bit_Half_Adder() throws Exception {
    runTest(new AddTests.TestOneBitHalfAdder(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_One_Bit_Full_Adder() throws Exception {
    runTest(new AddTests.TestOneBitFullAdder(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }
  
  @Test
  public void test_Binary_Adder() throws Exception {
    runTest(new AddTests.TestFullAdder(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }  
  
  @Test
  public void test_Binary_BitIncrementAdder() throws Exception {
    runTest(new AddTests.TestBitIncrement(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }  

  @Test
  public void test_Binary_Log_Nice() throws Exception {
    runTest(new LogTests.TestLogNice(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Log_Bad_length() throws Exception {
    runTest(new LogTests.TestLogBadLength(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Mult() throws Exception {
    runTest(new MultTests.TestBinaryMult(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }  
  
}
