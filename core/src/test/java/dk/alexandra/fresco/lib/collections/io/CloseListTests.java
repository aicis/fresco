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

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.io.CloseList;
import dk.alexandra.fresco.lib.collections.io.OpenList;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test class for the CloseList protocol.
 */
public class CloseListTests {

  /**
   * Performs a CloseList computation on an empty list of BigIntegers. Checks that result is empty.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestCloseEmptyList<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {

        @Override
        public void test() throws Exception {
          // define input and output
          List<BigInteger> input = new ArrayList<>();

          // define functionality to be tested
          Application<List<SInt>, SequentialNumericBuilder> testApplication = root -> {
            return root.par(par -> {
              // close inputs
              return new CloseList(input, 1).build(par);
            }).seq((closed, seq) -> {
              // unwrap Computations to get SInts and set output
              return () -> closed.stream().map(Computation::out).collect(Collectors.toList());
            });
          };
          List<SInt> output = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          assertTrue(output.isEmpty());
        }
      };
    }
  }

  /**
   * Opens and closes an input list of BigIntegers. Checks that opened result is same as original
   * input.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestCloseAndOpenList<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {

        @Override
        public void test() throws Exception {
          // define input
          List<BigInteger> input = new ArrayList<>();
          input.add(BigInteger.valueOf(1));
          input.add(BigInteger.valueOf(2));
          input.add(BigInteger.valueOf(3));

          // define functionality to be tested
          Application<List<BigInteger>, SequentialNumericBuilder> testApplication = root -> {
            return root.par(par -> {
              // close inputs
              if (conf.getMyId() == 1) {
                // party 1 provides input
                return new CloseList(input, 1).build(par);
              }
              else {
                // other parties receive it
                return new CloseList(3, 1).build(par);
              }
            }).par((closed, par) -> {
              // open them again
              Computation<List<Computation<BigInteger>>> opened = new OpenList(closed).build(par);
              return () -> opened.out().stream().map(Computation::out).collect(Collectors.toList());
            });
          };
          // run test application
          List<BigInteger> output = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          // define expected result and assert
          List<BigInteger> expected = new ArrayList<>();
          expected.add(BigInteger.valueOf(1));
          expected.add(BigInteger.valueOf(2));
          expected.add(BigInteger.valueOf(3));
          assertThat(output, is(expected));
        }
      };
    }
  }
}
