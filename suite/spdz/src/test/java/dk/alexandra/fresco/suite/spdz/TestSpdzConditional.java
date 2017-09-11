/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.spdz;

import org.junit.Test;

import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapNeighborsTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapRowsTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;

public class TestSpdzConditional extends AbstractSpdzTest {

  @Test
  public void test_conditional_select_left() throws Exception {
    runTest(ConditionalSelectTests.testSelectLeft(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_select_right() throws Exception {
    runTest(ConditionalSelectTests.testSelectRight(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_swap_yes() throws Exception {
    runTest(ConditionalSwapTests.testSwapYes(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_swap_no() throws Exception {
    runTest(ConditionalSwapTests.testSwapNo(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_swap_rows_yes() throws Exception {
    runTest(ConditionalSwapRowsTests.testSwapYes(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_swap_rows_no() throws Exception {
    runTest(ConditionalSwapRowsTests.testSwapNo(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_swap_neighbors_yes() throws Exception {
    runTest(ConditionalSwapNeighborsTests.testSwapYes(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_swap_neighbors_no() throws Exception {
    runTest(ConditionalSwapNeighborsTests.testSwapNo(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }
}
