package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  @Test
  public void test_leaky_aggregate_two() {
    runTest(LeakyAggregationTests.aggregate(), new TestParameters().numParties(2));
  }

  @Test
  public void test_leaky_aggregate_unique_keys_two() {
    runTest(LeakyAggregationTests.aggregateUniqueKeys(), new TestParameters().numParties(2));
  }

  @Test
  public void test_leaky_aggregate_three() {
    runTest(LeakyAggregationTests.aggregate(), new TestParameters().numParties(3));
  }

  @Test
  public void test_leaky_aggregate_empty() {
    runTest(LeakyAggregationTests.aggregateEmpty(), new TestParameters().numParties(2));
  }

  @Test
  public void test_MiMC_DifferentPlainTexts() {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(false), new TestParameters());
  }

  @Test
  public void test_MiMC_DifferentPlainTexts_Reduced() {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(true), new TestParameters());
  }

  @Test
  public void test_MiMC_EncSameEnc() {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(false), new TestParameters());
  }

  @Test
  public void test_MiMC_EncSameEncReduced() {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(true), new TestParameters());
  }

  @Test
  public void test_MiMC_EncDec_Reduced() {
    runTest(new MiMCTests.TestMiMCEncDec<>(true), new TestParameters().field(getModulus(512)));
  }

  @Test
  public void test_MiMC_EncDec() {
    runTest(new MiMCTests.TestMiMCEncDec<>(false), new TestParameters().field(getModulus(512)));
  }

  @Test
  public void test_MiMC_EncDecFixedRounds() {
    runTest(
        new MiMCTests.TestMiMCEncDecFixedRounds<>(), new TestParameters().field(getModulus(512)));
  }

  private FieldDefinition getModulus(int i) {
    return new BigIntegerFieldDefinition(ModulusFinder.findSuitableModulus(i));
  }
}
