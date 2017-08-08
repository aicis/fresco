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
package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

import org.hamcrest.core.Is;
import org.junit.Assert;

public class AddTests {

  public static class TestOneBitHalfAdder extends TestThreadFactory {

    public TestOneBitHalfAdder() {
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
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
          
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool inp1 = builder.knownSBool(true);
              SBool inp0 = builder.knownSBool(false);
              System.out.println("assign");
              SBool out00 = prov.getSBool();
              SBool carry00 = prov.getSBool();
              
              SBool out01 = prov.getSBool();
              SBool carry01 = prov.getSBool();
             
              SBool out10 = prov.getSBool();
              SBool carry10 = prov.getSBool();
             
              SBool out11 = prov.getSBool();
              SBool carry11 = prov.getSBool();
             
              System.out.println("protocols");
              seq.append(builder.getProtocol());
              seq.append(prov.getOneBitHalfAdderProtocol(inp0, inp0, out00, carry00));
              seq.append(prov.getOneBitHalfAdderProtocol(inp0, inp1, out01, carry01));
              seq.append(prov.getOneBitHalfAdderProtocol(inp1, inp0, out10, carry10));
              seq.append(prov.getOneBitHalfAdderProtocol(inp1, inp1, out11, carry11));
             
              OBool[] open = builder.output(new SBool[] {out00, out01, out10, out11});
              OBool[] openedCarry = builder.output(new SBool[] {carry00, carry01, carry10, carry11});
              seq.append(builder.getProtocol());
              this.outputs = new OBool[8];
              for(int i = 0; i< open.length; i++) {
                this.outputs[i] = open[i];
                this.outputs[i+4] = openedCarry[i];
              }
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertThat(app.getOutputs()[0].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[1].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[2].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[3].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[4].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[5].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[6].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[7].getValue(), Is.is(true));
          
        }
      };
    }
  }

  public static class TestOneBitFullAdder extends TestThreadFactory {

    public TestOneBitFullAdder() {
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
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
          
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool inp1 = builder.knownSBool(true);
              SBool inp0 = builder.knownSBool(false);
              
              SBool out000 = prov.getSBool();
              SBool carry000 = prov.getSBool();
              
              SBool out010 = prov.getSBool();
              SBool carry010 = prov.getSBool();
             
              SBool out100 = prov.getSBool();
              SBool carry100 = prov.getSBool();
             
              SBool out110 = prov.getSBool();
              SBool carry110 = prov.getSBool();
             
              SBool out001 = prov.getSBool();
              SBool carry001 = prov.getSBool();
            
              SBool out011 = prov.getSBool();
              SBool carry011 = prov.getSBool();
             
              SBool out101 = prov.getSBool();
              SBool carry101 = prov.getSBool();
             
              SBool out111 = prov.getSBool();
              SBool carry111 = prov.getSBool();
             
              
              seq.append(builder.getProtocol());
              seq.append(prov.getOneBitFullAdderProtocol(inp0, inp0, inp0, out000, carry000));
              seq.append(prov.getOneBitFullAdderProtocol(inp0, inp1, inp0, out010, carry010));
              seq.append(prov.getOneBitFullAdderProtocol(inp1, inp0, inp0, out100, carry100));
              seq.append(prov.getOneBitFullAdderProtocol(inp1, inp1, inp0, out110, carry110));
              seq.append(prov.getOneBitFullAdderProtocol(inp0, inp0, inp1, out001, carry001));
              seq.append(prov.getOneBitFullAdderProtocol(inp0, inp1, inp1, out011, carry011));
              seq.append(prov.getOneBitFullAdderProtocol(inp1, inp0, inp1, out101, carry101));
              seq.append(prov.getOneBitFullAdderProtocol(inp1, inp1, inp1, out111, carry111));
              
              
              OBool[] open = builder.output(
                  new SBool[] {out000, out010, out100, out110,
                      out001, out011, out101, out111});
              OBool[] openedCarry = builder.output(
                  new SBool[] {carry000, carry010, carry100, carry110,
                      carry001, carry011, carry101, carry111});
              seq.append(builder.getProtocol());
              this.outputs = new OBool[16];
              for(int i = 0; i< open.length; i++) {
                this.outputs[i] = open[i];
                this.outputs[i+8] = openedCarry[i];
              }
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertThat(app.getOutputs()[0].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[1].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[2].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[3].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[4].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[5].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[6].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[7].getValue(), Is.is(true));
          
          Assert.assertThat(app.getOutputs()[8].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[9].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[10].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[11].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[12].getValue(), Is.is(false));
          Assert.assertThat(app.getOutputs()[13].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[14].getValue(), Is.is(true));
          Assert.assertThat(app.getOutputs()[15].getValue(), Is.is(true));
        }
      };
    }
  }
  
  public static class TestFullAdder extends TestThreadFactory {

    public TestFullAdder() {
    }
    
    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          
          boolean[] rawFirst = ByteArithmetic.toBoolean("11");
          boolean[] rawSecond = ByteArithmetic.toBoolean("22");
                
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
          
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool[] first = builder.knownSBool(rawFirst);
              SBool[] second = builder.knownSBool(rawSecond);
              SBool carry = builder.knownSBool(false);
              SBool[] result = prov.getSBools(8); 
              SBool outCarry = prov.getSBool();
              
              seq.append(builder.getProtocol());
              seq.append(prov.getFullAdderProtocol(first, second, carry, result, outCarry));
              OBool[] open = builder.output(result);
              OBool openedCarry = builder.output(outCarry);
              seq.append(builder.getProtocol());
              this.outputs = new OBool[open.length+1];
              for(int i = 0; i< open.length; i++) {
                this.outputs[i] = open[i];
              }
              this.outputs[open.length] = openedCarry;
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          boolean[] raw = convert(app.getOutputs());
          
          boolean[] value = new boolean[raw.length-1];
          System.arraycopy(raw, 0, value, 0, raw.length-1);
          Assert.assertThat(raw[raw.length-1], Is.is(false));    
          Assert.assertThat(ByteArithmetic.toHex(value), Is.is("33"));
        }
      };
    }
    private boolean[] convert(OBool[] outputs) {
      boolean[] output = new boolean[outputs.length];
      for(int i = 0; i< outputs.length; i++) {
        output[i] = outputs[i].getValue();
      }
      return output;
    }
  }

  public static class TestBitIncrement extends TestThreadFactory {

    public TestBitIncrement() {
    }
    
    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          
          boolean[] rawSmall = ByteArithmetic.toBoolean("11");
          boolean[] rawBig = ByteArithmetic.toBoolean("ff");
          
          
          OBool[] res1 = new OBool[8];
          OBool[] res2 = new OBool[8];
          OBool[] res3 = new OBool[9];
          OBool[] res4 = new OBool[9];
          
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
          
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool[] small = builder.knownSBool(rawSmall);
              SBool[] big = builder.knownSBool(rawBig);
              SBool noIncrement = builder.knownSBool(false);
              SBool withIncrement = builder.knownSBool(true);
              
              SBool[] result = prov.getSBools(8); 
              SBool[] result2 = prov.getSBools(8);
              SBool[] result3 = prov.getSBools(9);
              SBool[] result4 = prov.getSBools(9);
              
        
              seq.append(builder.getProtocol());
              seq.append(prov.getBitIncrementerProtocol(small, noIncrement, result));
              seq.append(prov.getBitIncrementerProtocol(small, withIncrement, result2));

              seq.append(prov.getBitIncrementerProtocol(big, noIncrement, result3));
              seq.append(prov.getBitIncrementerProtocol(big, withIncrement, result4));

              
              OBool[] openFirst = builder.output(result);
              OBool[] openSecond = builder.output(result2);
              OBool[] openThird = builder.output(result3);
              OBool[] openFourth = builder.output(result4);
              seq.append(builder.getProtocol());
              for(int i = 0; i< openFirst.length; i++) {
                res1[i] = openFirst[i];
                res2[i] = openSecond[i];
                res3[i] = openThird[i];
                res4[i] = openFourth[i];
              }
              res3[8] = openThird[8];
              res4[8] = openFourth[8];
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          boolean[] raw1 = convert(res1);
          boolean[] raw2 = convert(res2);
          boolean[] raw3 = convert(res3);
          boolean[] raw4 = convert(res4);
          Assert.assertThat(ByteArithmetic.toHex(raw1), Is.is("11"));    
          Assert.assertThat(ByteArithmetic.toHex(raw2), Is.is("12"));
          Assert.assertThat(ByteArithmetic.toHex(raw3), Is.is("00ff"));
          Assert.assertThat(ByteArithmetic.toHex(raw4), Is.is("0100"));
        }
      };
    }
    private boolean[] convert(OBool[] outputs) {
      boolean[] output = new boolean[outputs.length];
      for(int i = 0; i< outputs.length; i++) {
        output[i] = outputs[i].getValue();
      }
      return output;
    }
  }
  
  
}
