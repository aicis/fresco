/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.math.bool.mult;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.DefaultBinaryBuilderAdvanced;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Assert;

public class MultTests {

  public static class TestBinaryMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestBinaryMult() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        List<Boolean> rawFirst = Arrays.asList(ByteArithmetic.toBoolean("11ff"));
        List<Boolean> rawSecond = Arrays.asList(ByteArithmetic.toBoolean("22"));

        final String expected = "0263de";

        @Override
        public void test() throws Exception {
        
            Application<List<Boolean>, ProtocolBuilderBinary> app =
                new Application<List<Boolean>, ProtocolBuilderBinary>() {
                
                  @Override
                  public Computation<List<Boolean>> prepareApplication(
                      ProtocolBuilderBinary producer) {

                    SequentialBinaryBuilder builder = (SequentialBinaryBuilder)producer;
                    
                    return builder.seq( seq -> {
                      DefaultBinaryBuilderAdvanced prov = new DefaultBinaryBuilderAdvanced(seq);
                      List<Computation<SBool>> first = rawFirst.stream().map(seq.binary()::known)
                          .collect(Collectors.toList());
                      List<Computation<SBool>> second = rawSecond.stream().map(seq.binary()::known)
                          .collect(Collectors.toList());
                      
                      Computation<List<Computation<SBool>>> multiplication = prov.binaryMult(first, second); 
                      
                      return () -> multiplication.out();
                      }
                    ).seq( (dat, seq) -> {
                       List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                       for(Computation<SBool> o : dat) {
                         out.add(seq.binary().open(o));
                          }
                          return () -> out.stream().map(Computation::out).collect(Collectors.toList());
                          }
                        );
                      }
              };
          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
 
          Assert.assertThat(ByteArithmetic.toHex(
              outputs),
              Is.is(expected));
          
          Assert.assertThat(outputs.size(), Is.is(rawFirst.size()+rawSecond.size()));

        }
      };
    }
  }
}
