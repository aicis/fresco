package dk.alexandra.fresco.suite.dummy.bool;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.collections.sort.CollectionsSortingTests;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.crypto.BadBristolCryptoTests;
import dk.alexandra.fresco.lib.crypto.BristolCryptoTests;
import dk.alexandra.fresco.lib.debug.BinaryDebugTests;
import dk.alexandra.fresco.lib.field.bool.generic.FieldBoolTests;
import dk.alexandra.fresco.lib.math.bool.add.AddTests;
import dk.alexandra.fresco.lib.math.bool.log.LogTests;
import dk.alexandra.fresco.lib.math.bool.mult.MultTests;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.binary.BinaryLoggingDecorator;
import org.junit.Test;


/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestDummyProtocolSuite extends AbstractDummyBooleanTest {

  // Basic tests for boolean suites
  @Test
  public void test_basic_logic() {
    runTest(new BasicBooleanTests.TestInput<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestInputDifferentSender<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        true, 2);
    runTest(new BasicBooleanTests.TestXOR<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestAND<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestNOT<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestRandomBit<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
        true);

    assertThat(performanceLoggers.get(1).get(2).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_XOR), is((long) 4));
    assertThat(performanceLoggers.get(1).get(3).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_AND), is((long) 4));
    assertThat(performanceLoggers.get(1).get(5).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_RANDOM), is((long) 1));
  }

  // lib.field.bool.generic
  // Slightly more advanced protocols for lowlevel logic operations
  @Test
  public void test_XNor() {
    runTest(new FieldBoolTests.TestXNorFromXorAndNot<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new FieldBoolTests.TestXNorFromOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_OR() {
    runTest(new FieldBoolTests.TestOrFromXorAnd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new FieldBoolTests.TestOrFromCopyConst<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testOpen() {
    runTest(new FieldBoolTests.TestOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    assertThat(performanceLoggers.get(1).get(0).getLoggedValues()
        .get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), is((long) 4));
  }

  @Test
  public void test_NAND() {
    runTest(new FieldBoolTests.TestNandFromAndAndNot<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new FieldBoolTests.TestNandFromOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_AndFromCopy() {
    runTest(new FieldBoolTests.TestAndFromCopyConst<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  // lib.math.bool
  @Test
  public void test_One_Bit_Half_Adder() {
    runTest(new AddTests.TestOnebitHalfAdder<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_One_Bit_Full_Adder() {
    runTest(new AddTests.TestOnebitFullAdder<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Adder() {
    runTest(new AddTests.TestFullAdder<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_BitIncrementAdder() {
    runTest(new AddTests.TestBitIncrement<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Mult() {
    runTest(new MultTests.TestBinaryMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  // Bristol tests
  @Test
  public void test_Mult32x32_Sequential() {
    runTest(new BristolCryptoTests.Mult32x32Test<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_AES_Sequential() {
    runTest(new BristolCryptoTests.AesTest<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_AES_Multi_Sequential() {
    runTest(new BristolCryptoTests.MultiAesTest<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_AES_SequentialBatched() {
    runTest(new BristolCryptoTests.AesTest<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_DES_Sequential() {
    runTest(new BristolCryptoTests.DesTest<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_MD5_Sequential() {
    runTest(new BristolCryptoTests.MD5Test<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_SHA1_Sequential() {
    runTest(new BristolCryptoTests.Sha1Test<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_SHA256_Sequential() {
    runTest(new BristolCryptoTests.Sha256Test<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Bristol_Errors_XOR() {
    runTest(new BadBristolCryptoTests.XorTest1<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.XorTest2<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.XorTest3<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.XorTest4<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.XorTest5<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Bristol_Errors_AND() {
    runTest(new BadBristolCryptoTests.AndTest1<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.AndTest2<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.AndTest3<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.AndTest4<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.AndTest5<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Bristol_Errors_INV() {
    runTest(new BadBristolCryptoTests.InvTest1<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.InvTest2<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.InvTest3<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.InvTest4<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Bristol_Errors_NOSUCHOPERATION() {
    runTest(new BadBristolCryptoTests.BadOperationTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new BadBristolCryptoTests.NoCircuitTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_basic_logic_all_in_one() {
    runTest(new BasicBooleanTests.TestBasicProtocols<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_comparison() {
    runTest(new ComparisonBooleanTests.TestGreaterThan<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_comparison_unequal_length() {
    runTest(new ComparisonBooleanTests.TestGreaterThanUnequalLength<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_equality() {
    runTest(new ComparisonBooleanTests.TestEquality<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  // collections.sort
  @Test
  public void test_Uneven_Odd_Even_Merge_2_parties() {
    runTest(new CollectionsSortingTests.TestOddEvenMerge<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Keyed_Compare_And_Swap_2_parties() {
    runTest(new CollectionsSortingTests.TestKeyedCompareAndSwap<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Compare_And_Swap() {
    runTest(new CompareTests.CompareAndSwapTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }


  @Test
  public void test_Debug_Marker() {
    runTest(new BinaryDebugTests.TestBinaryOpenAndPrint<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Debug_OpenAndPrintSysout() {
    runTest(new BinaryDebugTests.TestBinaryDebugToNullStream<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Binary_Log_Nice() {
    runTest(new LogTests.TestLogNice<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }
}
