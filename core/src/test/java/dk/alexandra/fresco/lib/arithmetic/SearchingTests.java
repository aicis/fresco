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
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.LinearLookUp;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Assert;

public class SearchingTests {

  public static class TestIsSorted<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          ResourcePoolT resourcePool = ResourcePoolCreator.createResourcePool(conf.sceConf);
          final int PAIRS = 10;
          final int MAXVALUE = 20000;
          final int NOTFOUND = -1;
          int[] keys = new int[PAIRS];
          int[] values = new int[PAIRS];
          ArrayList<Computation<SInt>> sKeys = new ArrayList<>(PAIRS);
          ArrayList<Computation<SInt>> sValues = new ArrayList<>(PAIRS);
          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory bnf = (BasicNumericFactory) producer;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              Random rand = new Random(0);
              for (int i = 0; i < PAIRS; i++) {
                keys[i] = i;
                values[i] = rand.nextInt(MAXVALUE);
                SInt sInt = bnf.getSInt(i);
                sKeys.add(() -> sInt);
                SInt valueSInt = bnf.getSInt(values[i]);
                sValues.add(() -> valueSInt);
              }
              return seq;
            }
          };
          secureComputationEngine.runApplication(app, resourcePool);
          for (int i = 0; i < PAIRS; i++) {
            final int counter = i;
            TestApplication app1 = new TestApplication() {
              @Override
              public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
                LinearLookUp linearLookUp = new LinearLookUp(
                    sKeys.get(counter), sKeys, sValues, NOTFOUND);
                ProtocolBuilderNumeric applicationRoot = ((BuilderFactoryNumeric) factoryProducer)
                    .createSequential();
                applicationRoot.seq(linearLookUp)
                    .seq((out, seq) -> {
                      this.outputs.add(seq.numeric().open(() -> out));
                      return () -> out;
                    });
                return applicationRoot.build();
              }
            };

            secureComputationEngine.runApplication(app1, resourcePool);

            Assert.assertEquals("Checking value index " + i,
                values[i], app1.outputs.get(0).out().intValue());
          }
        }
      };
    }
  }
}
