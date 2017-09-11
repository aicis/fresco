/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
 */
package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class LinAlgTests {


  public static class TestInnerProductClosed<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory {

    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 = Arrays.asList(200, 144, 99, 211);
        private final List<Integer> data2 = Arrays.asList(87, 14, 11, 21);
        private final BigInteger expected = new BigInteger("24936");

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              builder -> {
                Numeric sIntFactory = builder.numeric();

                List<DRes<SInt>> input1 = data1.stream().map(BigInteger::valueOf)
                    .map(sIntFactory::known).collect(Collectors.toList());
                // LinkedList<Computation<SInt>> bleh = new LinkedList(input1);
                System.out.println(input1);
                List<DRes<SInt>> input2 = data2.stream().map(BigInteger::valueOf)
                    .map(sIntFactory::known).collect(Collectors.toList());
                DRes<SInt> min =
                    builder.seq(new InnerProduct(input1, input2));

                return builder.numeric().open(min);
              };

          BigInteger result = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(expected, result);
        }
      };
    }
  }

  public static class TestInnerProductOpen<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory {

    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 = Arrays.asList(200, 144, 99, 211);
        private final List<BigInteger> data2 = Arrays.asList(BigInteger.valueOf(87),
            BigInteger.valueOf(14), BigInteger.valueOf(11), BigInteger.valueOf(21));
        private final BigInteger expected = new BigInteger("24936");

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              builder -> {
                Numeric sIntFactory = builder.numeric();

                List<DRes<SInt>> input1 = data1.stream().map(BigInteger::valueOf)
                    .map(sIntFactory::known).collect(Collectors.toList());
                DRes<SInt> min =
                    builder.seq(new InnerProductOpen(data2, input1));

                return builder.numeric().open(min);
              };

          BigInteger result = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(expected, result);
        }
      };
    }
  }
}
