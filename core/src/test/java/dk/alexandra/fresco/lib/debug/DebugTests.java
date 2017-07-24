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
package dk.alexandra.fresco.lib.debug;

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
import dk.alexandra.fresco.lib.helper.builder.UtilityBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;

public class DebugTests {

  public static class TestOpenAndPrint extends TestThreadFactory {

    public TestOpenAndPrint() {
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes);        
        
        @Override
        public void test() throws Exception {
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory provider) {
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              
              UtilityBuilder util = new UtilityBuilder(provider);
              
              SBool inp1 = builder.knownSBool(true);
              SBool inp2 = builder.knownSBool(false);
              SBool[] arr = new SBool[] {inp1, inp2};
             
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              seq.append(builder.getProtocol());
              
              util.openAndPrint("test", inp1, stream);
              util.openAndPrint("multi", arr, stream);
              seq.append(util.getProtocol());
              
              SBool test = builder.and(inp1, inp2);
              seq.append(builder.getProtocol());
              util.openAndPrint("tt", test);
              seq.append(util.getProtocol());
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              NetworkCreator.createResourcePool(conf.sceConf));

          String output = bytes.toString("UTF-8");
          
          Assert.assertEquals("test\n1\nmulti\n10\n",output);
        }
      };
    }
  }

}
