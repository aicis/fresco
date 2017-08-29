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
package dk.alexandra.fresco.lib.conditional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;
import dk.alexandra.fresco.lib.collections.io.CloseMatrix;
import dk.alexandra.fresco.lib.collections.io.OpenList;

/**
 * Test class for the ConditionalSwapRowsTests protocol.
 */
public class ConditionalSwapRowsTests {

  /**
   * Performs a ConditionalSwapRows computation on matrix.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  private static class TestSwapGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    final BigInteger swapperOpen;
    final Pair<ArrayList<BigInteger>, ArrayList<BigInteger>> expected;
    final Matrix<BigInteger> input;

    private TestSwapGeneric(BigInteger selectorOpen,
        Pair<ArrayList<BigInteger>, ArrayList<BigInteger>> expected, Matrix<BigInteger> input) {
      this.swapperOpen = selectorOpen;
      this.expected = expected;
      this.input = input;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Pair<ArrayList<BigInteger>, ArrayList<BigInteger>>, SequentialNumericBuilder> testApplication =
              root -> {
                return root.par(par -> {
                  // close inputs
                  return new CloseMatrix(input, 1).build(par);
                }).seq((closed, seq) -> {
                  Computation<SInt> swapper = seq.numeric().input(swapperOpen, 1);
                  return seq.createSequentialSub(
                      new ConditionalSwapRows(swapper, closed.getRow(0), closed.getRow(1)));
                }).par((closed, par) -> {
                  ArrayList<Computation<SInt>> closedRow = closed.getFirst();
                  ArrayList<Computation<SInt>> closedOtherRow = closed.getSecond();
                  Computation<List<Computation<BigInteger>>> openedRow =
                      par.createParallelSub(new OpenList(closedRow));
                  Computation<List<Computation<BigInteger>>> openedOtherRow =
                      par.createParallelSub(new OpenList(closedOtherRow));
                  return () -> {
                    ArrayList<BigInteger> unwrappedRow = openedRow.out().stream()
                        .map(row -> row.out()).collect(Collectors.toCollection(ArrayList::new));;
                    ArrayList<BigInteger> unwrappedOtherRow = openedOtherRow.out().stream()
                        .map(row -> row.out()).collect(Collectors.toCollection(ArrayList::new));
                    return new Pair<>(unwrappedRow, unwrappedOtherRow);
                  };
                });
              };
          Pair<ArrayList<BigInteger>, ArrayList<BigInteger>> output =
              secureComputationEngine.runApplication(testApplication,
                  ResourcePoolCreator.createResourcePool(conf.sceConf));
          assertThat(output, is(expected));
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestSwapGeneric<ResourcePoolT> testSwapYes() {
    Matrix<BigInteger> input = new MatrixTestUtils().getInputMatrix(2, 3);
    Pair<ArrayList<BigInteger>, ArrayList<BigInteger>> expected =
        new Pair<>(input.getRow(1), input.getRow(0));
    return new TestSwapGeneric<>(BigInteger.valueOf(1), expected, input);
  }

  public static <ResourcePoolT extends ResourcePool> TestSwapGeneric<ResourcePoolT> testSwapNo() {
    Matrix<BigInteger> input = new MatrixTestUtils().getInputMatrix(2, 3);
    Pair<ArrayList<BigInteger>, ArrayList<BigInteger>> expected =
        new Pair<>(input.getRow(0), input.getRow(1));
    return new TestSwapGeneric<>(BigInteger.valueOf(0), expected, input);
  }
}
