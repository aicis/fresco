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

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import org.junit.Assert;

public class ComparisonBooleanTests {

  /**
   * Tests if the number 01010 > 01110 - then it reverses that.
   *
   * @author Kasper Damgaard
   */
  public static class TestGreaterThan extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          boolean[] comp1 = new boolean[]{false, true, false, true, false};
          boolean[] comp2 = new boolean[]{false, true, true, true, false};

          TestBoolApplication app = new TestBoolApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();

              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);

              SBool[] in1 = builder.knownSBool(comp1);
              SBool[] in2 = builder.knownSBool(comp2);

              SBool compRes1 = builder.greaterThan(in1, in2);
              SBool compRes2 = builder.greaterThan(in2, in1);

              this.outputs = new OBool[]{builder.output(compRes1), builder.output(compRes2)};
              return builder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(false,
              app.getOutputs()[0].getValue());
          Assert.assertEquals(true,
              app.getOutputs()[1].getValue());
        }
      };
    }
  }
}
