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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 */
package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 *
 * Can be reused by a test case for any protocol suite that implements the basic field protocol
 * factory.
 *
 */
public class DivisionTests {

  public static Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric>
  createDivideApplication(BigInteger x, BigInteger d) {
    return (producer) -> {
      NumericBuilder numeric = producer.numeric();
      Computation<SInt> input1 = numeric.input(x, 1);
      Computation<SInt> division = producer.seq(new KnownDivisor(input1, d));
      Computation<SInt> remainder = producer.seq(new KnownDivisorRemainder(input1, d));
      Computation<BigInteger> output1 = numeric.open(division);
      Computation<BigInteger> output2 = numeric.open(remainder);
      return () -> new Pair<>(output1.out(), output2.out());
    };
  }

  /**
   * Test Euclidian division
   */
  public static class TestEuclidianDivision<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger x = new BigInteger("123978634193227335452345761");
        private final BigInteger d = new BigInteger("6543212341214412");

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app =
              DivisionTests.createDivideApplication(x, d);
          Pair<BigInteger, BigInteger> result = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger quotient = result.getFirst();
          BigInteger remainder = result.getSecond();

          Assert.assertThat(quotient, Is.is(x.divide(d)));
          Assert.assertThat(remainder, Is.is(x.mod(d)));


        }

      };
    }
  }

  /**
   * Test Euclidian division
   */
  public static class TestEuclidianDivisionLargeDivisor<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {


    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger x = new BigInteger("123978634193227335452345761");
        private final BigInteger d = new BigInteger("345195198248564927489350624"
            + "95619070576369242887355682637129830065132507683532321771277227216"
            + "22139694727529444746715611975582643235287997037145872954097664");

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app =
              DivisionTests.createDivideApplication(x, d);
          Pair<BigInteger, BigInteger> result = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger quotient = result.getFirst();
          BigInteger remainder = result.getSecond();

          Assert.assertThat(quotient, Is.is(x.divide(d)));
          Assert.assertThat(remainder, Is.is(x.mod(d)));

        }
      };
    }
  }

  /**
   * Test division with secret shared divisor
   */
  public static class TestSecretSharedDivision<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger[] x = new BigInteger[]{new BigInteger("1234567"),
            BigInteger.valueOf(1230121230), BigInteger.valueOf(313222110),
            BigInteger.valueOf(5111215), BigInteger.valueOf(6537)};
        private final BigInteger d = BigInteger.valueOf(1110);
        private final int n = x.length;


        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = (builder) -> {
            List<Computation<BigInteger>> results = new ArrayList<>(n);
            NumericBuilder numericBuilder = builder.numeric();
            Computation<SInt> divisor = numericBuilder.input(d, 1);
            for (BigInteger value : x) {
              Computation<SInt> dividend = numericBuilder.input(value, 1);
              Computation<SInt> division = builder.advancedNumeric().div(dividend, divisor);
              results.add(builder.numeric().open(division));
            }
            return () -> results.stream().map(Computation::out).collect(Collectors.toList());
          };
          List<BigInteger> results = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          for (int i = 0; i < n; i++) {
            BigInteger actual = results.get(i);

            BigInteger expected = x[i].divide(d);

            boolean isCorrect = expected.equals(actual);

            System.out.println(x[i] + "/" + d + " = " + actual + ", expected " + expected + ". ");
            Assert.assertTrue(isCorrect);
          }
        }
      };
    }
  }
}
