/*
 * Copyright (c) 2015, 2016, 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.collections.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;

/**
 * Test class for the CloseList protocol.
 */
public class CloseMatrixTests {

  /**
   * Closes an empty matrix of BigIntegers. Checks that result is empty.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestCloseEmptyMatrix<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // input
          Matrix<BigInteger> input = new Matrix<>(0, 0, new ArrayList<>());
          // functionality to be tested
          Application<Matrix<SInt>, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            Collections collections = root.collections();
            DRes<Matrix<DRes<SInt>>> mat = collections.closeMatrix(input, 1);
            // unwrap and return result
            return () -> new MatrixUtils().unwrapMatrix(mat);
          };
          Matrix<SInt> output = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          assertTrue(output.getRows().isEmpty());
        }
      };
    }
  }

  /**
   * Closes, then opens a matrix of BigIntegers.
   * 
   * Checks that result equals original matrix.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestCloseAndOpenMatrix<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define input and output
          ArrayList<BigInteger> rowOne = new ArrayList<>();
          rowOne.add(BigInteger.valueOf(1));
          rowOne.add(BigInteger.valueOf(2));
          rowOne.add(BigInteger.valueOf(3));
          ArrayList<BigInteger> rowTwo = new ArrayList<>();
          rowTwo.add(BigInteger.valueOf(4));
          rowTwo.add(BigInteger.valueOf(5));
          rowTwo.add(BigInteger.valueOf(6));
          ArrayList<ArrayList<BigInteger>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          Matrix<BigInteger> input = new Matrix<>(2, 3, mat);

          // define functionality to be tested
          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = root -> {
            Collections collections = root.collections();
            DRes<Matrix<DRes<SInt>>> closed = collections.closeMatrix(input, 1);
            DRes<Matrix<DRes<BigInteger>>> opened = collections.openMatrix(closed);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };

          Matrix<BigInteger> output = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          assertThat(output.getRows(), is(input.getRows()));
        }
      };
    }
  }
}
