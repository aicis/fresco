/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
 * THE SOFTWARE IS facIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
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
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFrac;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 *
 * Can be reused by a test case for any protocol suite that implements the basic
 * field protocol factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in
 * mpc.lib or something.
 */
public class BasicArithmeticTests {

  public static class TestInput<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(
                  fac);
              SInt input1 = ioBuilder.input(BigInteger.valueOf(10), 1);

              Computation<BigInteger> output = ioBuilder.output(input1);

              this.outputs.add(output);
              return ioBuilder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(BigInteger.valueOf(10),
              app.getOutputs()[0]);
        }
      };
    }
  }

  public static class TestOutputToSingleParty<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();

              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(
                  fac);
              SInt input1 = ioBuilder.input(
                  BigInteger.valueOf(10), 1);

              Computation<BigInteger> output = ioBuilder.outputToParty(1, input1);

              this.outputs.add(output);
              return ioBuilder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          if (conf.netConf.getMyId() == 1) {
            Assert.assertEquals(BigInteger.valueOf(10),
                app.getOutputs()[0]);
          } else {
            Assert.assertNull(app.getOutputs()[0]);
          }
        }
      };
    }
  }

  public static class TestAddPublicValue<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              SequentialProtocolProducer gp = new SequentialProtocolProducer();
              NumericIOBuilder ioBuilder = new NumericIOBuilder(
                  fac);
              SInt input1 = ioBuilder.input(
                  BigInteger.valueOf(10), 1);

              gp.append(ioBuilder.getProtocol());
              ioBuilder.reset();

              BigInteger publicVal = BigInteger.valueOf(4);
              SInt out = fac.getSInt();
              Computation addProtocol = fac.getAddProtocol(input1, publicVal, out);
              gp.append(addProtocol);

              Computation<BigInteger> output = ioBuilder.output(out);
              ProtocolProducer io = ioBuilder.getProtocol();
              gp.append(io);

              this.outputs.add(output);
              return gp;
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          Assert.assertEquals(BigInteger.valueOf(14),
              app.getOutputs()[0]);
        }
      };
    }
  }


  public static class TestLotsOfInputs<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          final int[] openInputs = new int[]{11, 2, 3, 4, 5, 6, 7,
              8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(fac);
              SInt knownInput = fac.getSInt(BigInteger.valueOf(200));
              SInt[] inputs = createInputs(ioBuilder, openInputs);
              inputs[0] = knownInput;

              this.outputs = ioBuilder.outputArray(inputs);

              return ioBuilder.getProtocol();
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          checkOutputs(openInputs, app.getOutputs());
        }
      };
    }
  }

  public static class TestKnownSInt<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          final int[] openInputs = new int[]{200, 300, 1, 2};
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(
                  fac);
              SInt knownInput1 = fac.getSInt(BigInteger.valueOf(200));
              SInt knownInput2 = fac.getSInt(BigInteger.valueOf(300));
              SInt knownInput3 = fac.getSInt(BigInteger.valueOf(1));
              SInt knownInput4 = fac.getSInt(BigInteger.valueOf(2));
              Computation<BigInteger> knownOutput1 = ioBuilder.output(knownInput1);
              Computation<BigInteger> knownOutput2 = ioBuilder.output(knownInput2);
              Computation<BigInteger> knownOutput3 = ioBuilder.output(knownInput3);
              Computation<BigInteger> knownOutput4 = ioBuilder.output(knownInput4);
              this.outputs.addAll(Arrays.asList(knownOutput1, knownOutput2, knownOutput3,
                  knownOutput4));
              return ioBuilder.getProtocol();
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          checkOutputs(openInputs, app.getOutputs());
        }
      }

          ;
    }
  }


  public static class TestSumAndMult<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          final int[] openInputs = new int[]{1, 2, 3, 4, 5, 6, 7,
              8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(
                  fac);

              SInt[] inputs = createInputs(ioBuilder, openInputs
              );

              ProtocolProducer inp = ioBuilder.getProtocol();
              ioBuilder.reset();

              // create wire
              SInt sum = fac.getSInt();

              // create Sequence of protocols which eventually
              // will compute the sum
              SequentialProtocolProducer sumProtocol = new SequentialProtocolProducer();

              sumProtocol.append(fac.getAddProtocol(inputs[0],
                  inputs[1], sum));
              if (inputs.length > 2) {
                for (int i = 2; i < inputs.length; i++) {
                  // Add sum and next secret shared input and
                  // store in sum.
                  sumProtocol.append(fac.getAddProtocol(sum,
                      inputs[i], sum));
                }
              }

              sumProtocol.append(fac.getMultProtocol(sum, sum,
                  sum));

              this.outputs.add(ioBuilder.output(sum));

              ProtocolProducer io = ioBuilder.getProtocol();

              return new SequentialProtocolProducer(
                  inp, sumProtocol, io);
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          int sum = 0;
          for (int i : openInputs) {
            sum += i;
          }
          sum = sum * sum;
          Assert.assertEquals(BigInteger.valueOf(sum),
              app.getOutputs()[0]);
        }
      };
    }
  }

  private static void checkOutputs(int[] openInputs, BigInteger[] outputs) {
    for (int i = 0; i < openInputs.length; i++) {
      Assert.assertEquals(BigInteger.valueOf(openInputs[i]),
          outputs[i]);
    }
  }

  private static SInt[] createInputs(NumericIOBuilder ioBuilder, int[] input) {
    BigInteger[] bs = new BigInteger[input.length];
    int inx = 0;
    for (int i : input) {
      bs[inx] = BigInteger.valueOf(i);
      inx++;
    }
    return ioBuilder.inputArray(bs, 1);
  }

  public static class TestSimpleMultAndAdd<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(
                  fac);
              SInt input1 = ioBuilder.input(
                  BigInteger.valueOf(10), 1);
              SInt input2 = ioBuilder.input(
                  BigInteger.valueOf(5), 1);

              ProtocolProducer inputs = ioBuilder.getProtocol();
              ioBuilder.reset();
              NumericProtocolBuilder builder = new NumericProtocolBuilder(
                  fac);
              SInt addAndMult = builder.mult(input1,
                  builder.add(input1, input2));
              ProtocolProducer circ = builder.getProtocol();

              Computation<BigInteger> output = ioBuilder.output(addAndMult);
              this.outputs.add(output);
              ProtocolProducer outputs = ioBuilder.getProtocol();

              return new SequentialProtocolProducer(
                  inputs, circ, outputs);
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(BigInteger.valueOf(10 * (10 + 5)),
              app.getOutputs()[0]);
        }
      };
    }
  }

  /**
   * Test a large amount (defined by the REPS constant) multiplication protocols in order to
   * stress-test the protocol suite.
   */
  public static class TestLotsMult<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {
            private static final int REPS = 20000;


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(fac);
              NumericProtocolBuilder builder = new NumericProtocolBuilder(fac);
              SInt input1 = builder.getSInt(10);
              SInt input2 = builder.getSInt(5);
              SInt[] results = new SInt[REPS];
              builder.beginParScope();
              for (int i = 0; i < REPS; i++) {
                results[i] = builder.mult(input1, input2);
              }
              builder.endCurScope();
              ioBuilder.addProtocolProducer(builder.getProtocol());
              outputs = ioBuilder.outputArray(results);
              return ioBuilder.getProtocol();
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          BigInteger[] outputs = app.getOutputs();
          for (BigInteger o : outputs) {
            Assert.assertEquals(o, BigInteger.valueOf(50));
          }
        }
      };
    }
  }

  public static class TestMinInfFrac<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              List<BigInteger> bns = Arrays.asList(
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(2),
                  BigInteger.valueOf(30),
                  BigInteger.valueOf(1),
                  BigInteger.valueOf(50),
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(20),
                  BigInteger.valueOf(30),
                  BigInteger.valueOf(5),
                  BigInteger.valueOf(1)
              );
              List<BigInteger> bds = Arrays.asList(
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(10),
                  BigInteger.valueOf(20),
                  BigInteger.valueOf(30),
                  BigInteger.valueOf(500),
                  BigInteger.valueOf(50)
              );
              List<BigInteger> binfs = Arrays.asList(
                  BigInteger.valueOf(0),
                  BigInteger.valueOf(0),
                  BigInteger.valueOf(0),
                  BigInteger.valueOf(1),
                  BigInteger.valueOf(0),
                  BigInteger.valueOf(0),
                  BigInteger.valueOf(0),
                  BigInteger.valueOf(0),
                  BigInteger.valueOf(1),
                  BigInteger.valueOf(1)
              );
              SequentialProtocolBuilder seq = ProtocolBuilderNumeric
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer);
              NumericBuilder numeric = seq.numeric();
              List<Computation<SInt>> ns = bns.stream().map(numeric::known)
                  .collect(Collectors.toList());
              List<Computation<SInt>> ds = bds.stream().map(numeric::known)
                  .collect(Collectors.toList());
              List<Computation<SInt>> infs = binfs.stream().map(numeric::known)
                  .collect(Collectors.toList());

              seq.seq(
                  new MinInfFrac(
                      ns, ds, infs
                  )
              ).seq((infOutput, seq2) -> {
                NumericBuilder innerNumeric = seq2.numeric();
                List<Computation<BigInteger>> collect = infOutput.cs.stream()
                    .map(innerNumeric::open)
                    .collect(Collectors.toList());
                return () -> collect;
              }).seq((outputList, ignored) -> {
                outputs = outputList;
                return () -> null;
              });
              return seq.build();
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          BigInteger[] outputs = app.getOutputs();
          int sum = 0;
          for (int i = 0; i < outputs.length; i++) {
            sum += outputs[i].intValue();
            if (i == 1) {
              Assert.assertEquals(BigInteger.ONE, outputs[i]);
            } else {
              Assert.assertEquals(BigInteger.ZERO, outputs[i]);
            }
          }
          Assert.assertEquals(1, sum);
          //System.out.println(outputs[0].getValue() +  " / " + outputs[1].getValue() + " " + outputs[2].getValue());
          //Assert.assertEquals(BigInteger.valueOf(1), outputs[0].getValue());
          //Assert.assertEquals(BigInteger.valueOf(10), outputs[1].getValue());
          //Assert.assertEquals(BigInteger.valueOf(0), outputs[2].getValue());

        }
      };
    }
  }

  /**
   * Test a computation of doing a many multiplications and additions
   * alternating between the two. This should ensure batches with both
   * types of protocols.
   */
  public static class TestAlternatingMultAdd<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory fac = (BasicNumericFactory) producer;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(fac);
              NumericProtocolBuilder builder = new NumericProtocolBuilder(fac);
              ioBuilder.beginSeqScope();
              ioBuilder.beginParScope();
              SInt input1 = ioBuilder.input(BigInteger.valueOf(10), 1);
              SInt input2 = ioBuilder.input(BigInteger.valueOf(5), 1);
              ioBuilder.endCurScope();
              builder.beginParScope();
              for (int i = 0; i < 1000; i++) {
                if (i % 2 == 0) {
                  builder.mult(input1, input2);
                } else {
                  builder.add(input1, input2);
                }
              }
              builder.endCurScope();

              ioBuilder.addProtocolProducer(builder.getProtocol());
              ioBuilder.endCurScope();
              return ioBuilder.getProtocol();
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
        }
      };
    }
  }
}
