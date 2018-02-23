package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt32;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.util.function.Supplier;
import org.junit.Test;

public class TestMarlinBasicArithmetic96 extends AbstractMarlinTest<
    UInt64,
    UInt32,
    CompUInt96,
    MarlinResourcePool<UInt64, UInt32, CompUInt96>> {

  @Test
  public void testInput() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testInputThree() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testAdd() {
    runTest(new BasicArithmeticTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testAddThree() {
    runTest(new BasicArithmeticTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testMultiply() {
    runTest(new BasicArithmeticTests.TestMultiply<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testKnown() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testAddPublic() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testAddPublicThree() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testKnownThree() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testMultiplyThree() {
    runTest(new BasicArithmeticTests.TestMultiply<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testInputOutputMany() {
    runTest(new TestCloseAndOpenList<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testInputOutputManyThree() {
    runTest(new TestCloseAndOpenList<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testMultiplyMany() {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testMultiplyManyThree() {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testSumAndMult() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testSumAndMultThree() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Test
  public void testSimpleMultAndAdd() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testSimpleMultAndAddThree() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Test
  public void testAlternatingMultAdd() {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testAlternatingMultAddThree() {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Test
  public void testMultiplyByPublicValue() {
    runTest(new BasicArithmeticTests.TestMultiplyByPublicValue<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testMultiplyByPublicValueThree() {
    runTest(new BasicArithmeticTests.TestMultiplyByPublicValue<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Test
  public void testSubtract() {
    runTest(new BasicArithmeticTests.TestSubtract<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testSubtractThree() {
    runTest(new BasicArithmeticTests.TestSubtract<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Test
  public void testSubtractNegative() {
    runTest(new BasicArithmeticTests.TestSubtractNegative<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testSubtractNegativeThree() {
    runTest(new BasicArithmeticTests.TestSubtractNegative<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Test
  public void testSubtractPublic() {
    runTest(new BasicArithmeticTests.TestSubtractPublic<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testSubtractPublicThree() {
    runTest(new BasicArithmeticTests.TestSubtractPublic<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Test
  public void testSubtractFromPublic() {
    runTest(new BasicArithmeticTests.TestSubtractFromPublic<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        2,
        false);
  }

  @Test
  public void testSubtractFromPublicThree() {
    runTest(new BasicArithmeticTests.TestSubtractFromPublic<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        3,
        false);
  }

  @Override
  protected MarlinResourcePool<UInt64, UInt32, CompUInt96> createResourcePool(int playerId,
      int noOfParties, MarlinOpenedValueStore<CompUInt96> store,
      MarlinDataSupplier<CompUInt96> supplier,
      CompUIntFactory<UInt64, UInt32, CompUInt96> factory, Supplier<Network> networkSupplier) {
    MarlinResourcePool<UInt64, UInt32, CompUInt96> resourcePool =
        new MarlinResourcePoolImpl<>(
            playerId,
            noOfParties, null, store, supplier, factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected CompUIntFactory<UInt64, UInt32, CompUInt96> createFactory() {
    return new CompUInt96Factory();
  }

  @Override
  protected ProtocolSuiteNumeric<MarlinResourcePool<UInt64, UInt32, CompUInt96>> createProtocolSuite() {
    return new MarlinProtocolSuite<>();
  }

}
