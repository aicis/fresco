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
 *******************************************************************************/
package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;
import org.junit.Assert;

public class ExponentiationTests {

  /**
   * Test binary right shift of a shared secret.
   */
  public static class TestExponentiation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger input = BigInteger.valueOf(12332157);
        private final int exp = 12;

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return null;
            }

            @Override
            public Computation<List<BigInteger>> prepareApplication(
                ProtocolBuilderNumeric producer) {
              NumericBuilder numeric = producer.numeric();
              Computation<SInt> base = numeric.input(input, 1);
              Computation<SInt> exponent = numeric.input(BigInteger.valueOf(exp), 1);

              Computation<SInt> result = producer.advancedNumeric().exp(base, exponent, 5);

              outputs.add(numeric.open(result));

              return outputToBigInteger();
            }
          };
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger result = app.getOutputs()[0];

          Assert.assertEquals(input.pow(exp), result);
        }
      };
    }
  }

  public static class TestExponentiationOpenExponent extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {

      return new TestThread() {
        private final BigInteger input = BigInteger.valueOf(12332157);
        private final int exp = 12;

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return null;
            }

            @Override
            public Computation<List<BigInteger>> prepareApplication(
                ProtocolBuilderNumeric producer) {
              NumericBuilder numeric = producer.numeric();
              Computation<SInt> base = numeric.known(input);
              BigInteger exponent = BigInteger.valueOf(exp);

              Computation<SInt> result = producer.advancedNumeric().exp(base, exponent);

              outputs.add(numeric.open(result));

              return outputToBigInteger();
            }
          };
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger result = app.getOutputs()[0];

          Assert.assertEquals(input.pow(exp), result);
        }
      };
    }
  }

  public static class TestExponentiationOpenBase extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {

      return new TestThread() {
        private final BigInteger input = BigInteger.valueOf(12332157);
        private final int exp = 12;

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return null;
            }

            @Override
            public Computation<List<BigInteger>> prepareApplication(
                ProtocolBuilderNumeric producer) {
              NumericBuilder numeric = producer.numeric();
              BigInteger base = input;
              Computation<SInt> exponent = numeric.known(BigInteger.valueOf(exp));

              Computation<SInt> result = producer.advancedNumeric().exp(base, exponent, 5);

              outputs.add(numeric.open(result));

              return outputToBigInteger();
            }
          };
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger result = app.getOutputs()[0];

          Assert.assertEquals(input.pow(exp), result);
        }
      };
    }
  }

  public static class TestExponentiationZeroExponent extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {

      return new TestThread() {
        private final BigInteger input = BigInteger.valueOf(12332157);

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return null;
            }

            @Override
            public Computation<List<BigInteger>> prepareApplication(
                ProtocolBuilderNumeric producer) {
              NumericBuilder numeric = producer.numeric();
              Computation<SInt> base = numeric.known(input);
              BigInteger exponent = BigInteger.ZERO;

              Computation<SInt> result = producer.advancedNumeric().exp(base, exponent);

              outputs.add(numeric.open(result));

              return outputToBigInteger();
            }
          };
          try {
            secureComputationEngine.runApplication(app,
                ResourcePoolCreator.createResourcePool(conf.sceConf));
          } catch (RuntimeException e) {
            // Cause is wrapped in an intermediate concurrent exception.
            if (e.getCause().getCause() instanceof IllegalArgumentException) {
              return;
            }
          }
          Assert.fail(
              "Should have thrown an Illegal argument exception since exponent is not allowed to be 0");
        }
      };
    }
  }

}
