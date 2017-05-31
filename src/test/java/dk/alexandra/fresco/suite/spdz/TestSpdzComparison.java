/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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

import org.junit.Test;

import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.ComparisonTests;

public class TestSpdzComparison extends AbstractSpdzTest{	

	@Test
	public void test_compareLT_Sequential() throws Exception {
		runTest(new ComparisonTests.TestCompareLT(),
				EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
	}
	
	@Test
	public void test_compareEQ_Sequential() throws Exception {
		runTest(new ComparisonTests.TestCompareEQ(),
				EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
	}
	
	@Test
	public void test_isSorted() throws Exception {
		runTest(new SortingTests.TestIsSorted(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
	
	@Test
	public void test_compareAndSwap() throws Exception {
		runTest(new SortingTests.TestCompareAndSwap(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
	@Test
	public void test_Sort() throws Exception {
		runTest(new SortingTests.TestSort(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
	@Test
	@Ignore
	public void test_Big_Sort() throws Exception {
		runTest(new SortingTests.TestBigSort(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
	@Test
	public void test_logic() throws Exception {
		runTest(new LogicTests.TestLogic(),
				EvaluationStrategy.SEQUENTIAL,StorageStrategy.IN_MEMORY);
	}
}
