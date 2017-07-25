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
package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import org.junit.Assert;

public class FieldBoolTests {

  public static class TestXNorFromXorAndNotProtocol extends TestThreadFactory {

    public TestXNorFromXorAndNotProtocol() {
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
                ProtocolFactory provider) {
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool inp100 = builder.knownSBool(false);
              SBool inp200 = builder.knownSBool(false);
              SBool out1 = prov.getSBool();
              
              SBool inp110 = builder.knownSBool(true);
              SBool inp210 = builder.knownSBool(false);
              SBool out2 = prov.getSBool();
              

              SBool inp101 = builder.knownSBool(false);
              SBool inp201 = builder.knownSBool(true);
              SBool out3 = prov.getSBool();
              
              
              SBool inp111 = builder.knownSBool(true);
              SBool inp211 = builder.knownSBool(true);
              SBool out4 = prov.getSBool();
              
              seq.append(builder.getProtocol());
              
              seq.append(prov.getXnorProtocol(inp100, inp200, out1));
              seq.append(prov.getXnorProtocol(inp110, inp210, out2));
              seq.append(prov.getXnorProtocol(inp101, inp201, out3));
              seq.append(prov.getXnorProtocol(inp111, inp211, out4));

              
              this.outputs = new OBool[]{builder.output(out1), builder.output(out2),
                  builder.output(out3), builder.output(out4)};
              seq.append(builder.getProtocol());
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(true,
              app.getOutputs()[0].getValue());
          
          Assert.assertEquals(false,
              app.getOutputs()[1].getValue());

          Assert.assertEquals(false,
              app.getOutputs()[2].getValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].getValue());

        }
      };
    }
  }
  
  public static class TestOrFromXorAndProtocol extends TestThreadFactory {

    public TestOrFromXorAndProtocol() {
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
                ProtocolFactory provider) {
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SBool inp100 = builder.knownSBool(false);
              SBool inp200 = builder.knownSBool(false);
              SBool out1 = builder.or(inp100, inp200);
              
              SBool inp110 = builder.knownSBool(true);
              SBool inp210 = builder.knownSBool(false);
              SBool out2 = builder.or(inp110, inp210);
              

              SBool inp101 = builder.knownSBool(false);
              SBool inp201 = builder.knownSBool(true);
              SBool out3 = builder.or(inp101, inp201);
              
              
              SBool inp111 = builder.knownSBool(true);
              SBool inp211 = builder.knownSBool(true);
              SBool out4 = builder.or(inp111, inp211);
                            
              this.outputs = new OBool[]{builder.output(out1), builder.output(out2),
                  builder.output(out3), builder.output(out4)};
              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app,
              NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(false,
              app.getOutputs()[0].getValue());
          
          Assert.assertEquals(true,
              app.getOutputs()[1].getValue());

          Assert.assertEquals(true,
              app.getOutputs()[2].getValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].getValue());

        }
      };
    }
  }

  
  public static class TestNandFromAndAndNotProtocol extends TestThreadFactory {

    public TestNandFromAndAndNotProtocol() {
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
                ProtocolFactory provider) {
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool inp100 = builder.knownSBool(false);
              SBool inp200 = builder.knownSBool(false);
              SBool out1 = prov.getSBool();
              
              SBool inp110 = builder.knownSBool(true);
              SBool inp210 = builder.knownSBool(false);
              SBool out2 = prov.getSBool();

              SBool inp101 = builder.knownSBool(false);
              SBool inp201 = builder.knownSBool(true);
              SBool out3 = prov.getSBool();
              
              SBool inp111 = builder.knownSBool(true);
              SBool inp211 = builder.knownSBool(true);
              SBool out4 = prov.getSBool();
              seq.append(builder.getProtocol());


              seq.append(prov.getNandProtocol(inp100, inp200, out1));
              seq.append(prov.getNandProtocol(inp110, inp210, out2));
              seq.append(prov.getNandProtocol(inp101, inp201, out3));
              seq.append(prov.getNandProtocol(inp111, inp211, out4));

              this.outputs = new OBool[]{builder.output(out1), builder.output(out2),
                  builder.output(out3), builder.output(out4)};
              seq.append(builder.getProtocol());
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(true,
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
  
  public static class TestOrFromCopyConstProtocol extends TestThreadFactory {

    public TestOrFromCopyConstProtocol() {
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
                ProtocolFactory provider) {
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SBool inp100 = builder.knownSBool(false);
              OBool inp200 = builder.getOBool(false);
              SBool out1 = builder.or(inp100, inp200);
              
              SBool inp110 = builder.knownSBool(true);
              OBool inp210 = builder.getOBool(false);
              SBool out2 = builder.or(inp110, inp210);
              

              SBool inp101 = builder.knownSBool(false);
              OBool inp201 = builder.getOBool(true);
              SBool out3 = builder.or(inp101, inp201);
              
              
              SBool inp111 = builder.knownSBool(true);
              OBool inp211 = builder.getOBool(true);
              SBool out4 = builder.or(inp111, inp211);
                            
              this.outputs = new OBool[]{builder.output(out1), builder.output(out2),
                  builder.output(out3), builder.output(out4)};
              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app,
              NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(false,
              app.getOutputs()[0].getValue());
          
          Assert.assertEquals(true,
              app.getOutputs()[1].getValue());

          Assert.assertEquals(true,
              app.getOutputs()[2].getValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].getValue());

        }
      };
    }
  }

  public static class TestAndFromCopyConstProtocol extends TestThreadFactory {

    public TestAndFromCopyConstProtocol() {
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
                ProtocolFactory provider) {
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              SBool inp100 = builder.knownSBool(false);
              OBool inp200 = builder.getOBool(false);
              SBool out1 = prov.getSBool();
              
              SBool inp110 = builder.knownSBool(true);
              OBool inp210 = builder.getOBool(false);
              SBool out2 = prov.getSBool();
              

              SBool inp101 = builder.knownSBool(false);
              OBool inp201 = builder.getOBool(true);
              SBool out3 = prov.getSBool();
              
              
              SBool inp111 = builder.knownSBool(true);
              OBool inp211 = builder.getOBool(true);
              SBool out4 = prov.getSBool();

              seq.append(builder.getProtocol());
              seq.append(new AndFromCopyConstProtocol(prov, prov, inp100, inp200, out1));
              seq.append(new AndFromCopyConstProtocol(prov, prov, inp110, inp210, out2));
              seq.append(new AndFromCopyConstProtocol(prov, prov, inp101, inp201, out3));
              seq.append(new AndFromCopyConstProtocol(prov, prov, inp111, inp211, out4));
              
              this.outputs = new OBool[]{builder.output(out1), builder.output(out2),
                  builder.output(out3), builder.output(out4)};
              seq.append(builder.getProtocol());
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(false,
              app.getOutputs()[0].getValue());
          
          Assert.assertEquals(false,
              app.getOutputs()[1].getValue());

          Assert.assertEquals(false,
              app.getOutputs()[2].getValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].getValue());

        }
      };
    }
  }


  
  
  public static class TestNotFromXorProtocol extends TestThreadFactory {

    public TestNotFromXorProtocol() {
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
                ProtocolFactory provider) {
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              SBool inp11 = builder.knownSBool(true);
              SBool out1 = prov.getSBool();
                           
              SBool inp10 = builder.knownSBool(false);
              SBool out2 = prov.getSBool();
              
              seq.append(builder.getProtocol());
              seq.append(new NotFromXorProtocol(prov, prov, inp11, out1));
              seq.append(new NotFromXorProtocol(prov, prov, inp10, out2));
              
              this.outputs = new OBool[]{builder.output(out1), builder.output(out2)};
              seq.append(builder.getProtocol());
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(false,
              app.getOutputs()[0].getValue());
          
          Assert.assertEquals(true,
              app.getOutputs()[1].getValue());
        }
      };
    }
  }
  
}
