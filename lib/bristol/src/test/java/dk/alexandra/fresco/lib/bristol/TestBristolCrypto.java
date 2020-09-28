package dk.alexandra.fresco.lib.bristol;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.logging.binary.BinaryLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.bool.AbstractDummyBooleanTest;
import org.junit.Test;


/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestBristolCrypto extends AbstractDummyBooleanTest {

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

}
