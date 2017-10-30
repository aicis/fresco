package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.MiMCTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzMiMC extends AbstractSpdzTest {

	@Test
	public void test_mimc_same_enc() throws Exception {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET,
				PreprocessingStrategy.DUMMY, 2);
	}
	
	@Test
	public void test_mimc_diff_enc() throws Exception {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET,
				PreprocessingStrategy.DUMMY, 2);
	}
	
	@Test
	public void test_mimc_det_enc() throws Exception {
    runTest(new MiMCTests.TestMiMCEncryptsDeterministically<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET,
				PreprocessingStrategy.DUMMY, 2);
	}

	@Test
	public void test_mimc_enc_dec() throws Exception {
    runTest(new MiMCTests.TestMiMCEncDec<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET,
				PreprocessingStrategy.DUMMY, 2);
	}
}
