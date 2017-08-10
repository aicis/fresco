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
package dk.alexandra.fresco.lib.math.bool.mult;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.generic.AbstractBinaryFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFrac;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.core.Is;
import org.junit.Assert;

public class MultTests {

  
  public static class TestBinaryMult<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestBinaryMult() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          List<Boolean> rawFirst = Arrays.asList(ByteArithmetic.toBoolean("11ff"));
          List<Boolean> rawSecond = Arrays.asList(ByteArithmetic.toBoolean("22"));
          
          TestBoolApplication app = new TestBoolApplication() {
//
            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory fac = (AbstractBinaryFactory) producer;
              BasicLogicBuilder ioBuilder = new BasicLogicBuilder(fac);
              
              //SBool[] first = ioBuilder.knownSBool(rawFirst);              
              //SBool[] second = ioBuilder.knownSBool(rawSecond);
              

              SequentialBinaryBuilder seq = ProtocolBuilderBinary
                  .createApplicationRoot(fac);
              
              List<Computation<SBool>> first  = rawFirst.stream().map(ioBuilder::knownSBool).collect(Collectors.toList());

              seq.seq(new MinInfFrac(ns, ds, infs)).seq((infOutput, seq2) -> {
                NumericBuilder innerNumeric = seq2.numeric();
                List<Computation<BigInteger>> collect =
                    infOutput.cs.stream().map(innerNumeric::open).collect(Collectors.toList());
                return () -> collect;
              }).seq((outputList, ignored) -> {
                outputs = outputList;
                return () -> null;
              });
              return seq.build();
            }
          };

              
  /*            
              Computation<Boolean> output = ioBuilder.output(fac.getBinaryMultProtocol(lefts, rights, outs)(first, second));

              this.outputs.add(output);
              return ioBuilder.getProtocol();
            }
          };
*/
          
          
          
          secureComputationEngine.runApplication(app, ResourcePoolCreator
              .createResourcePool(conf.sceConf));

          //Assert.assertEquals(BigInteger.valueOf(10), app.getOutputs()[0]);
          Assert.assertThat(app.getOutputs()[app.getOutputs().length-1], Is.is(false));    
          Assert.assertThat(ByteArithmetic.toHex(app.getOutputs()), Is.is("0263de"));
        }
      };
    }
  }
    
  private Boolean[] convert(List<Boolean> outputs) {
    return outputs.toArray(new Boolean[1]);
  }
}
