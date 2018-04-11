package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import org.junit.Test;

public abstract class Spdz2kBasicArithmeticTestSuite<Spdz2kResourcePoolT extends Spdz2kResourcePool<?>>
    extends AbstractSpdz2kTest<Spdz2kResourcePoolT> {

  @Test
  public void testInput() {
    runTest(new BasicArithmeticTests.TestInput<>());
  }

  @Test
  public void testAdd() {
    runTest(new BasicArithmeticTests.TestAdd<>());
  }

  @Test
  public void testAddWithOverflow() {
    runTest(new BasicArithmeticTests.TestAddWithOverflow<>()
    );
  }

  @Test
  public void testMultiply() {
    runTest(new BasicArithmeticTests.TestMultiply<>());
  }

  @Test
  public void testMultiplyByZero() {
    runTest(new BasicArithmeticTests.TestMultiplyByZero<>());
  }

  @Test
  public void testMultiplyWithOverflow() {
    runTest(new BasicArithmeticTests.TestMultiplyWithOverflow<>()
    );
  }

  @Test
  public void testKnown() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>());
  }

  @Test
  public void testAddPublic() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>());
  }

  @Test
  public void testInputOutputMany() {
    runTest(new TestCloseAndOpenList<>());
  }

  @Test
  public void testMultiplyMany() {
    runTest(new BasicArithmeticTests.TestLotsMult<>());
  }

  @Test
  public void testSumAndMult() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>());
  }

  @Test
  public void testSimpleMultAndAdd() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>());
  }

  @Test
  public void testAlternatingMultAdd() {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>());
  }

  @Test
  public void testMultiplyByPublicValue() {
    runTest(new BasicArithmeticTests.TestMultiplyByPublicValue<>());
  }

  @Test
  public void testSubtract() {
    runTest(new BasicArithmeticTests.TestSubtract<>());
  }

  @Test
  public void testSubtractNegative() {
    runTest(new BasicArithmeticTests.TestSubtractNegative<>());
  }

  @Test
  public void testSubtractPublic() {
    runTest(new BasicArithmeticTests.TestSubtractPublic<>());
  }

  @Test
  public void testSubtractFromPublic() {
    runTest(new BasicArithmeticTests.TestSubtractFromPublic<>());
  }

  @Test
  public void testOutputToSingleParty() {
    runTest(new BasicArithmeticTests.TestOutputToSingleParty<>());
  }

  @Test
  public void testRandomBit() {
    runTest(new BasicArithmeticTests.TestRandomBit<>());
  }

  @Test
  public void testRandomElement() {
    runTest(new BasicArithmeticTests.TestRandomElement<>());
  }

}
