/*
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
 * THE SOFTWARE IS facIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFrac;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.core.Is;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 *
 * Can be reused by a test case for any protocol suite that implements the basic field protocol
 * factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in mpc.lib or something.
 */
public class BasicArithmeticTests {

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      BigInteger value = BigInteger.valueOf(10);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              producer -> {
                NumericBuilder numeric = producer.numeric();

                Computation<SInt> input = numeric.input(value, 1);
                return numeric.open(input);
              };
          BigInteger output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          Assert.assertEquals(value, output);
        }
      };
    }
  }

  public static class TestOutputToSingleParty<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      BigInteger value = BigInteger.valueOf(10);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              producer -> {
                NumericBuilder numeric = producer.numeric();

                Computation<SInt> input = numeric.input(value, 1);
                return numeric.open(input, 2);
              };
          BigInteger output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          if (conf.netConf.getMyId() == 2) {
            Assert.assertEquals(BigInteger.valueOf(10), output);
          } else {
            Assert.assertNull(output);
          }
        }
      };
    }
  }

  public static class TestAddPublicValue<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      BigInteger value = BigInteger.valueOf(10);
      BigInteger add = BigInteger.valueOf(4);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              producer -> {
                NumericBuilder numeric = producer.numeric();

                Computation<SInt> input = numeric.input(value, 1);
                Computation<SInt> result = numeric.add(add, input);
                return numeric.open(result);
              };
          BigInteger output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          Assert.assertEquals(value.add(add), output);
        }
      };
    }
  }

  public static class TestKnownSInt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      List<BigInteger> openInputs = Stream.of(200, 300, 1, 2)
          .map(BigInteger::valueOf)
          .collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer ->
                  producer.par(par -> {
                    NumericBuilder numeric = par.numeric();
                    List<Computation<SInt>> result = openInputs.stream()
                        .map(numeric::known)
                        .collect(Collectors.toList());
                    return () -> result;
                  }).par((par, closed) -> {
                    NumericBuilder numeric = par.numeric();
                    List<Computation<BigInteger>> result = closed.stream()
                        .map(numeric::open)
                        .collect(Collectors.toList());
                    return () -> result.stream().map(Computation::out).collect(Collectors.toList());
                  });
          List<BigInteger> output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          Assert.assertEquals(openInputs, output);
        }
      };
    }
  }


  public static class TestSumAndMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      List<BigInteger> openInputs =
          Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
              .map(BigInteger::valueOf)
              .collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              producer ->
                  producer.par(par -> {
                    NumericBuilder numeric = par.numeric();
                    List<Computation<SInt>> result = openInputs.stream()
                        .map(numeric::known)
                        .collect(Collectors.toList());
                    return () -> result;
                  }).seq((seq, closed) -> {
                    AdvancedNumericBuilder advancedNumeric = seq.advancedNumeric();
                    Computation<SInt> sum = advancedNumeric.sum(closed);
                    Computation<SInt> mult = seq.numeric().mult(sum, sum);
                    return seq.numeric().open(mult);
                  });
          BigInteger output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          int sum = 0;
          for (BigInteger openInput : openInputs) {
            sum += openInput.intValue();
          }
          sum = sum * sum;
          Assert.assertEquals(sum, output.intValue());
        }
      };
    }
  }

  public static class TestSimpleMultAndAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      BigInteger first = BigInteger.valueOf(10);
      BigInteger second = BigInteger.valueOf(5);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              producer -> {
                NumericBuilder numeric = producer.numeric();

                Computation<SInt> firstClosed = numeric.known(first);
                Computation<SInt> secondClosed = numeric.known(second);

                Computation<SInt> add = numeric.add(firstClosed, secondClosed);
                Computation<SInt> mult = numeric.mult(firstClosed, add);

                return numeric.open(mult);
              };
          BigInteger output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          Assert.assertEquals(first.add(second).multiply(first), output);
        }
      };
    }
  }

  /**
   * Test a large amount (defined by the REPS constant) multiplication protocols in order to
   * stress-test the protocol suite.
   */
  public static class TestLotsMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      BigInteger first = BigInteger.valueOf(10);
      BigInteger second = BigInteger.valueOf(5);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private static final int REPS = 20000;

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer ->
                  producer.par(par -> {
                    NumericBuilder numeric = par.numeric();
                    Computation<SInt> firstClosed = numeric.known(first);
                    Computation<SInt> secondClosed = numeric.known(second);
                    return Pair.lazy(firstClosed, secondClosed);
                  }).par((par, pair) -> {
                    Computation<SInt> firstClosed = pair.getFirst();
                    Computation<SInt> secondClosed = pair.getSecond();
                    NumericBuilder numeric = par.numeric();
                    ArrayList<Computation<SInt>> computations = new ArrayList<>();
                    for (int i = 0; i < REPS; i++) {
                      computations.add(numeric.mult(firstClosed, secondClosed));
                    }
                    return () -> computations;
                  }).seq((seq, computations) -> {
                    NumericBuilder numeric = seq.numeric();
                    List<Computation<BigInteger>> opened = computations.stream().map(numeric::open)
                        .collect(Collectors.toList());
                    return () -> opened.stream().map(Computation::out).collect(Collectors.toList());
                  });
          List<BigInteger> output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          BigInteger multiply = first.multiply(second);
          Assert.assertThat(output.size(), Is.is(REPS));
          for (BigInteger result : output) {
            Assert.assertEquals(multiply, result);
          }
        }
      };
    }
  }

  public static class TestMinInfFrac<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> {
                List<BigInteger> bns = Arrays.asList(BigInteger.valueOf(10), BigInteger.valueOf(2),
                    BigInteger.valueOf(30), BigInteger.valueOf(1), BigInteger.valueOf(50),
                    BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.valueOf(30),
                    BigInteger.valueOf(5), BigInteger.valueOf(1));
                List<BigInteger> bds = Arrays.asList(BigInteger.valueOf(10), BigInteger.valueOf(10),
                    BigInteger.valueOf(10), BigInteger.valueOf(10), BigInteger.valueOf(10),
                    BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.valueOf(30),
                    BigInteger.valueOf(500), BigInteger.valueOf(50));
                List<BigInteger> binfs = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(0),
                    BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(0),
                    BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0),
                    BigInteger.valueOf(1), BigInteger.valueOf(1));
                NumericBuilder numeric = producer.numeric();
                List<Computation<SInt>> ns =
                    bns.stream().map((n) -> numeric.input(n, 1)).collect(Collectors.toList());
                List<Computation<SInt>> ds =
                    bds.stream().map((n) -> numeric.input(n, 1)).collect(Collectors.toList());
                List<Computation<SInt>> infs =
                    binfs.stream().map((n) -> numeric.input(n, 1)).collect(Collectors.toList());

                return producer.seq(new MinInfFrac(ns, ds, infs)).seq((seq2, infOutput) -> {
                  NumericBuilder innerNumeric = seq2.numeric();
                  List<Computation<BigInteger>> collect =
                      infOutput.cs.stream().map(innerNumeric::open).collect(Collectors.toList());
                  return () -> collect.stream().map(Computation::out).collect(Collectors.toList());
                });
          };
          List<BigInteger> outputs = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));
          int sum = 0;
          for (int i = 0; i < outputs.size(); i++) {
            sum += outputs.get(i).intValue();
            if (i == 1) {
              Assert.assertEquals(BigInteger.ONE, outputs.get(i));
            } else {
              Assert.assertEquals(BigInteger.ZERO, outputs.get(i));
            }
          }
          Assert.assertEquals(1, sum);

        }
      };
    }
  }

  /**
   * Test a computation of doing a many multiplications and additions alternating between the two.
   * This should ensure batches with both types of protocols.
   */
  public static class TestAlternatingMultAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      BigInteger first = BigInteger.valueOf(10);
      BigInteger second = BigInteger.valueOf(5);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          final int numberOfComputations = 1000;
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer ->
                  producer.par(par -> {
                    NumericBuilder numeric = par.numeric();
                    Computation<SInt> firstClosed = numeric.known(first);
                    Computation<SInt> secondClosed = numeric.known(second);
                    return Pair.lazy(firstClosed, secondClosed);
                  }).par((par, pair) -> {
                    Computation<SInt> firstClosed = pair.getFirst();
                    Computation<SInt> secondClosed = pair.getSecond();
                    NumericBuilder numeric1 = par.numeric();
                    ArrayList<Computation<SInt>> computations = new ArrayList<>();
                    for (int i = 0; i < numberOfComputations; i++) {
                      if (i % 2 == 0) {
                        computations.add(numeric1.mult(firstClosed, secondClosed));
                      } else {
                        computations.add(numeric1.add(firstClosed, secondClosed));
                      }

                    }
                    return () -> computations;
                  }).seq((seq, computations) -> {
                    NumericBuilder numeric1 = seq.numeric();
                    List<Computation<BigInteger>> opened = computations.stream().map(numeric1::open)
                        .collect(Collectors.toList());
                    return () -> opened.stream().map(Computation::out).collect(Collectors.toList());
                  });
          List<BigInteger> output = secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          BigInteger multiply = first.multiply(second);
          BigInteger add = first.add(second);
          Assert.assertThat(output.size(), Is.is(numberOfComputations));
          for (int i = 0; i < output.size(); i++) {
            BigInteger result = output.get(i);
            if (i % 2 == 0) {
              Assert.assertEquals(multiply, result);
            } else {
              Assert.assertEquals(add, result);
            }

          }
        }
      };
    }
  }
}
