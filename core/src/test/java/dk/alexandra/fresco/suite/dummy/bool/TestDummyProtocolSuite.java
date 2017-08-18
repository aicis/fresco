/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.collections.sort.CollectionsSortingTests;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.debug.BinaryDebugTests;
import dk.alexandra.fresco.lib.field.bool.generic.FieldBoolTests;
import dk.alexandra.fresco.lib.math.bool.add.AddTests;
import dk.alexandra.fresco.lib.math.bool.log.LogTests;
import dk.alexandra.fresco.lib.math.bool.mult.MultTests;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestDummyProtocolSuite<ResourcePoolT extends ResourcePool>
    extends AbstractDummyBooleanTest {

  // Basic tests for boolean suites
  @Test
  public void test_basic_logic() throws Exception {
    runTest(new BasicBooleanTests.TestInput<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
    runTest(new BasicBooleanTests.TestXOR<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
    runTest(new BasicBooleanTests.TestAND<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
    runTest(new BasicBooleanTests.TestNOT<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
    runTest(new BasicBooleanTests.TestCOPY<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  // lib.field.bool.generic
  // Slightly more advanced protocols for lowlevel logic operations
  @Test
  public void test_XNor() throws Exception {
    runTest(new FieldBoolTests.TestXNorFromXorAndNot<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
    runTest(new FieldBoolTests.TestXNorFromOpen<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_OR() throws Exception {
    runTest(new FieldBoolTests.TestOrFromXorAnd<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
    runTest(new FieldBoolTests.TestOrFromCopyConst<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_NAND() throws Exception {
    runTest(new FieldBoolTests.TestNandFromAndAndNot<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
    runTest(new FieldBoolTests.TestNandFromOpen<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_AndFromCopy() throws Exception {
    runTest(new FieldBoolTests.TestAndFromCopyConst<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_NotFromXor() throws Exception {
    runTest(new FieldBoolTests.TestNotFromXor<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }


  // lib.math.bool
  @Test
  public void test_One_Bit_Half_Adder() throws Exception {
    runTest(new AddTests.TestOnebitHalfAdder<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_One_Bit_Full_Adder() throws Exception {
    runTest(new AddTests.TestOnebitFullAdder<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_Binary_Adder() throws Exception {
    runTest(new AddTests.TestFullAdder<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_Binary_BitIncrementAdder() throws Exception {
    runTest(new AddTests.TestBitIncrement<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_Binary_Mult() throws Exception {
    runTest(new MultTests.TestBinaryMult<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET);
  }

  // Bristol tests

  @Test
  public void test_Mult32x32_Sequential() throws Exception {
    runTest(new BristolCryptoTests.Mult32x32Test<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_AES_Sequential() throws Exception {
    runTest(new BristolCryptoTests.AesTest<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_AES_SequentialBatched() throws Exception {
    runTest(new BristolCryptoTests.AesTest<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_DES_Sequential() throws Exception {
    runTest(new BristolCryptoTests.DesTest<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_MD5_Sequential() throws Exception {
    runTest(new BristolCryptoTests.MD5Test<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_SHA1_Sequential() throws Exception {
    runTest(new BristolCryptoTests.Sha1Test<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_SHA256_Sequential() throws Exception {
    runTest(new BristolCryptoTests.Sha256Test<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  // TODO Perhaps this test should be moved to a dedicated BasicLogicBuilder
  // test class, as the exception is thrown there
  @Test(expected = RuntimeException.class)
  public void test_comparisonBadLength() throws Exception {
    runTest(new ComparisonBooleanTests.TestGreaterThanUnequalLength<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_comparisonPar() throws Exception {
    // runTest(new ComparisonBooleanTests.TestGreaterThanPar(), EvaluationStrategy.SEQUENTIAL,
    // NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_basic_logic_all_in_one() throws Exception {
    runTest(new BasicBooleanTests.TestBasicProtocols<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_comparison() throws Exception {
    runTest(new ComparisonBooleanTests.TestGreaterThan<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_equality() throws Exception {
    runTest(new ComparisonBooleanTests.TestEquality<ResourcePoolT>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  // collections.sort
  @Ignore // for now
  @Test
  public void test_Uneven_Odd_Even_Merge_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestOddEvenMerge(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Ignore // for now
  @Test
  public void test_Uneven_Odd_Even_Merge_Rec_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestOddEvenMergeRec(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Ignore // for now
  @Test
  public void test_Uneven_Odd_Even_Merge_Rec_Large_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestOddEvenMergeRecLarge(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  @Ignore // for now
  @Test
  public void test_Keyed_Compare_And_Swap_2_parties() throws Exception {
    runTest(new CollectionsSortingTests.TestKeyedCompareAndSwap(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET);
  }

  // TODO
  @Test
  public void test_Compare_And_Swap() throws Exception {
    // runTest(new CompareTests.TestCompareAndSwap(), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_Debug_Marker() throws Exception {
    runTest(new BinaryDebugTests.TestBinaryOpenAndPrint<ResourcePoolT>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET);
  }


  @Test
  public void test_Binary_Log_Nice() throws Exception {
    runTest(new LogTests.TestLogNice(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET);
  }

  @Test
  public void test_Binary_Log_Bad_length() throws Exception {
    runTest(new LogTests.TestLogBadLength(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET);
  }

}
