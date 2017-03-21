/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.spdz;

import java.math.BigInteger;

import org.junit.Test;

import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.lib.arithmetic.MiMCTests;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;

public class TestSpdzMiMC extends AbstractSpdzTest {

	@Test
	public void test_mimc_same_enc() throws Exception {
		runTest(new MiMCTests.TestMiMCEncSameEnc(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET,
				PreprocessingStrategy.DUMMY, 2);
	}

	@Test
	public void test_mimc_enc_dec() throws Exception {
		// Use a static preprocessing approach since otherwise, we would have to
		// create an array of size ~2^500 since 3^-1 in the dummy field is of
		// approximately that size.
		int noOfThreads = 3;
		InitializeStorage.cleanup();
		try {
			InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()), 2,
					noOfThreads, 10000, 1000, 0, 0, new BigInteger("1031"));
			runTest(new MiMCTests.TestMiMCEncDec(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET,
					PreprocessingStrategy.STATIC, 2);
		} catch (Exception e) {
			throw e;
		} finally {
			InitializeStorage.cleanup();
		}
	}
}
