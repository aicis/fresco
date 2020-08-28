package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzMiMC extends AbstractSpdzTest {

  @Test
  public void test_mimc_same_enc() {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(false),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_mimc_same_enc_reduced() {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(true),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_mimc_diff_enc() {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(false),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_mimc_diff_enc_reduced() {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(true),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_mimc_enc_dec() {
    runTest(new MiMCTests.TestMiMCEncDec<>(false),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_mimc_enc_dec_reduced() {
    runTest(new MiMCTests.TestMiMCEncDec<>(true),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testMimcWithMascot() {
    runTest(new MiMCTests.TestMiMCEncDec<>(false), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 16, 16, 4);
  }

  @Test
  public void testMimcWithMascotReduced() {
    runTest(new MiMCTests.TestMiMCEncDec<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 16, 16, 4);
  }

}
