/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.lib.arithmetic.AdvancedNumericTests;
import org.junit.Test;

public class TestSpdzAdvancedNumeric extends TestSpdz2Parties {

    @Test
    public void test_Division() throws Exception {
        runTest(new AdvancedNumericTests.TestDivision(),
                EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
    }

    @Test
    public void test_DivisionWithPrecision() throws Exception {
        runTest(new AdvancedNumericTests.TestDivisionWithPrecision(),
                EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
    }

    @Test
    public void test_DivisionWithKnownDenominator() throws Exception {
        runTest(new AdvancedNumericTests.TestDivisionWithKnownDenominator(),
                EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
    }

    @Test
    public void test_DivisionWithRemainder() throws Exception {
        runTest(new AdvancedNumericTests.TestDivisionWithRemainder(),
                EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
    }

    @Test
    public void test_Modulus() throws Exception {
        runTest(new AdvancedNumericTests.TestModulus(),
            EvaluationStrategy.SEQUENTIAL, StorageStrategy.IN_MEMORY);
    }
}
