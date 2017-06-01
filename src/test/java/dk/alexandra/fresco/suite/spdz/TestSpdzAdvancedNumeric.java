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

import org.junit.Test;

import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.AdvancedNumericTests;

public class TestSpdzAdvancedNumeric extends AbstractSpdzTest {

    @Test
    public void test_Division() throws Exception {
        int[][] examples = new int[][]{
            new int[]{9, 4},
            new int[]{82, 2},
            new int[]{3, 3},
            new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE},
            new int[]{1, Integer.MAX_VALUE},
            new int[]{-9, 4},
            new int[]{9, -4},
            new int[]{-9, -4},
        };
        for (int[] example: examples) {
            test_Division(example[0], example[1]);
        }
    }

    private void test_Division(int numerator, int denominator) throws Exception {
        runTest(new AdvancedNumericTests.TestDivision(numerator, denominator),
                EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
    }

    @Test
    public void test_DivisionWithPrecision() throws Exception {
        runTest(new AdvancedNumericTests.TestDivisionWithPrecision(),
                EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
    }

    @Test
    public void test_Division_Known_Denominator() throws Exception {
        int[][] examples = new int[][]{
            new int[]{9, 4},
            new int[]{82, 2},
            new int[]{3, 3},
            new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE},
            new int[]{1, Integer.MAX_VALUE},
            new int[]{-9, 4},
            new int[]{9, -4},
            new int[]{-9, -4}
        };
        for (int[] example: examples) {
            test_DivisionWithKnownDenominator(example[0], example[1]);
        }
    }
    
    private void test_DivisionWithKnownDenominator(int numerator, int denominator) throws Exception {
        runTest(new AdvancedNumericTests.TestDivisionWithKnownDenominator(numerator, denominator),
                EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
    }

    @Test
    public void test_DivisionWithRemainder() throws Exception {
        runTest(new AdvancedNumericTests.TestDivisionWithRemainder(),
                EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
    }

    @Test
    public void test_Modulus() throws Exception {
        runTest(new AdvancedNumericTests.TestModulus(),
            EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
    }
}
