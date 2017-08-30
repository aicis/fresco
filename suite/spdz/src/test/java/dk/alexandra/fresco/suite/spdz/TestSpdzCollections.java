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

import java.util.ArrayList;

import org.junit.Test;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.io.CloseListTests;
import dk.alexandra.fresco.lib.collections.io.CloseMatrixTests;
import dk.alexandra.fresco.lib.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.collections.permute.PermuteRowsTests;
import dk.alexandra.fresco.lib.collections.shuffle.ShuffleRowsTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;

public class TestSpdzCollections extends AbstractSpdzTest {

  @Test
  public void test_close_empty_list() throws Exception {
    runTest(new CloseListTests.TestCloseEmptyList<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_list() throws Exception {
    runTest(new CloseListTests.TestCloseEmptyList<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_empty_matrix() throws Exception {
    runTest(new CloseMatrixTests.TestCloseEmptyMatrix<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_matrix() throws Exception {
    runTest(new CloseMatrixTests.TestCloseAndOpenMatrix<>(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_permute_rows() throws Exception {
    runTest(PermuteRowsTests.permuteRows(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.SCAPI,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_permute_empty_rows() throws Exception {
    runTest(PermuteRowsTests.permuteEmptyRows(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 2);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test_permute_rows_non_power_of_two() throws Throwable {
    ArrayList<ArrayList<Computation<SInt>>> fakeRows = new ArrayList<>();
    // we don't need to populate matrix since the exception is thrown
    // based on the height of the matrix
    Matrix<Computation<SInt>> fakeMatrix = new Matrix<>(3, 2, fakeRows);
    new PermuteRows(fakeMatrix, 1);
  }

  @Test
  public void test_shuffle_rows_two_parties() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsTwoParties(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_shuffle_rows_three_parties() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsThreeParties(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_shuffle_rows_empty() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsEmpty(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.SCAPI, PreprocessingStrategy.DUMMY, 2);
  }
}
