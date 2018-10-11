package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.MiMCTests;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzMiMC extends AbstractSpdzTest {

  @Test
  public void test_mimc_same_enc() {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_mimc_diff_enc() {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_mimc_det_enc() {
    runTest(
        new MiMCTests.TestMiMCEncryptsDeterministically<>(),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }

  @Test
  public void test_mimc_enc_dec() {
    runTest(new MiMCTests.TestMiMCEncDec<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testMimcWithMascot() {
    runTest(new MiMCTests.TestMiMCEncDec<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 16, 8, 4,
        8);
  }

}
