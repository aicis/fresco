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
package dk.alexandra.fresco.lib.math.bool.log;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.SBool;

import org.hamcrest.core.Is;
import org.junit.Assert;

public class LogTests {

  public static class TestLogNice extends TestThreadFactory {

    public TestLogNice() {
    }

    Boolean[] rawInput = ByteArithmetic.toBoolean("ff");
    
    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
    /*      
          Boolean[] rawInput = ByteArithmetic.toBoolean("ff");
          
          TestBoolApplication app = new TestBoolApplication() {
            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();

              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SBool[] in1 = builder.knownSBool(rawInput);
              
              SBool[] result = prov.getSBools(4);
              builder.addProtocolProducer(new LogProtocolImpl(in1, result, prov));
              
              this.outputs = builder.output(result);
              return builder.getProtocol();
            }
          };
          
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          boolean[] raw = convert(app.getOutputs());
          
          Assert.assertThat(ByteArithmetic.toHex(raw), Is.is("08"));*/
        }

        
      };
    }
    /*private boolean[] convert(OBool[] outputs) {
      boolean[] output = new boolean[outputs.length];
      for(int i = 0; i< outputs.length; i++) {
        output[i] = outputs[i].getValue();
      }
      return output;
    }*/
  }

  
  public static class TestLogBadLength extends TestThreadFactory {

    public TestLogBadLength() {
    }

    Boolean[] rawInput = ByteArithmetic.toBoolean("ff");
    
    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
       /*   
          boolean[] rawInput = ByteArithmetic.toBoolean("ff");
          
          
          TestBoolApplication app = new TestBoolApplication() {
            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();

              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              SBool[] in1 = builder.knownSBool(rawInput);
              
              SBool[] result = prov.getSBools(6);
              builder.addProtocolProducer(new LogProtocolImpl(in1, result, prov));
              
              this.outputs = builder.output(result);
              return builder.getProtocol();
            }
          };

          try{
            secureComputationEngine.runApplication(app,
                ResourcePoolCreator.createResourcePool(conf.sceConf));
          }catch(RuntimeException e) {
          }*/
        }
      };
    }
   
  }

}
