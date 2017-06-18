/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.bool;

import dk.alexandra.fresco.framework.FactoryProducer;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import org.junit.Assert;

public class BasicBooleanTests {

  public static class TestInput extends TestThreadFactory {

    private boolean assertAsExpected;

    public TestInput(boolean assertAsExpected) {
      this.assertAsExpected = assertAsExpected;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              SBool inp = builder.knownSBool(true);
              OBool output = builder.output(inp);
              this.outputs = new OBool[]{output};
              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app,
              SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          if (!assertAsExpected) {
            return;
          }
          Assert.assertEquals(true,
              app.getOutputs()[0].getValue());
        }
      };
    }
  }

  public static class TestXOR extends TestThreadFactory {

    private boolean assertAsExpected;

    public TestXOR(boolean assertAsExpected) {
      this.assertAsExpected = assertAsExpected;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              SBool inp100 = builder.knownSBool(false);
              SBool inp200 = builder.knownSBool(false);

              SBool xor00 = builder.xor(inp100, inp200);

              SBool inp110 = builder.knownSBool(true);
              SBool inp210 = builder.knownSBool(false);

              SBool xor10 = builder.xor(inp110, inp210);

              SBool inp101 = builder.knownSBool(false);
              SBool inp201 = builder.knownSBool(true);

              SBool xor01 = builder.xor(inp101, inp201);

              SBool inp111 = builder.knownSBool(true);
              SBool inp211 = builder.knownSBool(true);

              SBool xor11 = builder.xor(inp111, inp211);

              this.outputs = builder.output(xor00, xor10, xor01, xor11);
              return builder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          if (!assertAsExpected) {
            return;
          }
          Assert.assertEquals(false,
              app.getOutputs()[0].getValue());
          Assert.assertEquals(true,
              app.getOutputs()[1].getValue());
          Assert.assertEquals(true,
              app.getOutputs()[2].getValue());
          Assert.assertEquals(false,
              app.getOutputs()[3].getValue());
        }
      };
    }
  }

  public static class TestAND extends TestThreadFactory {

    private boolean assertAsExpected;

    public TestAND(boolean assertAsExpected) {
      this.assertAsExpected = assertAsExpected;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);

              SBool inp1 = prov.getSBool();
              SBool inp2 = prov.getSBool();

              builder.addProtocolProducer(
                  SingleProtocolProducer.wrap(
                      prov.getCloseProtocol(1, prov.getKnownConstantOBool(true), inp1)));
              builder.addProtocolProducer(
                  SingleProtocolProducer.wrap(
                      prov.getCloseProtocol(2, prov.getKnownConstantOBool(true), inp2)));

              SBool and = builder.and(inp1, inp2);

              OBool output = builder.output(and);

              this.outputs = new OBool[]{output};
              return builder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          if (!assertAsExpected) {
            return;
          }
          Assert.assertEquals(true,
              app.getOutputs()[0].getValue());
        }
      };
    }
  }

  public static class TestNOT extends TestThreadFactory {

    private boolean assertAsExpected;

    public TestNOT(boolean assertAsExpected) {
      this.assertAsExpected = assertAsExpected;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              SBool inp1 = builder.knownSBool(true);

              SBool not = builder.not(inp1);

              OBool output = builder.output(not);
              this.outputs = new OBool[]{output};
              return builder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          if (!assertAsExpected) {
            return;
          }
          Assert.assertEquals(false,
              app.getOutputs()[0].getValue());
        }
      };
    }
  }

  /**
   * Tests both input, xor, not, and and output.
   * Computes all variants of: NOT((i1 XOR i2) AND i1)
   */
  public static class TestBasicProtocols extends TestThreadFactory {

    private boolean assertAsExpected;

    public TestBasicProtocols(boolean assertAsExpected) {
      this.assertAsExpected = assertAsExpected;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              SBool inp100 = builder.knownSBool(false);
              SBool inp200 = builder.knownSBool(false);
              SBool xor00 = builder.xor(inp100, inp200);
              SBool and00 = builder.and(inp100, xor00);
              SBool not00 = builder.not(and00);

              SBool inp110 = builder.knownSBool(true);
              SBool inp210 = builder.knownSBool(false);
              SBool xor10 = builder.xor(inp110, inp210);
              SBool and10 = builder.and(inp110, xor10);
              SBool not10 = builder.not(and10);

              SBool inp101 = builder.knownSBool(false);
              SBool inp201 = builder.knownSBool(true);
              SBool xor01 = builder.xor(inp101, inp201);
              SBool and01 = builder.and(inp101, xor01);
              SBool not01 = builder.not(and01);

              SBool inp111 = builder.knownSBool(true);
              SBool inp211 = builder.knownSBool(true);
              SBool xor11 = builder.xor(inp111, inp211);
              SBool and11 = builder.and(inp111, xor11);
              SBool not11 = builder.not(and11);

              //maybe remove again - test for having not before and
              SBool ainp111 = builder.knownSBool(true);
              SBool ainp211 = builder.knownSBool(true);
              SBool anot11 = builder.not(ainp211);
              SBool aand11 = builder.and(ainp111, anot11);

              this.outputs = builder.output(not00, not10, not01, not11, aand11);
              return builder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          if (!assertAsExpected) {
            return;
          }

          Assert.assertEquals(true,
              app.getOutputs()[0].getValue());
          Assert.assertEquals(false,
              app.getOutputs()[1].getValue());
          Assert.assertEquals(true,
              app.getOutputs()[2].getValue());
          Assert.assertEquals(true,
              app.getOutputs()[3].getValue());
          Assert.assertEquals(false,
              app.getOutputs()[4].getValue());
        }
      };
    }
  }
}
