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
package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluator;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class PolynomialTests {

  public static class TestPolynomialEvaluator<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory {

    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final int[] coefficients = {1, 0, 1, 2};
        private final int x = 3;

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(BuilderFactory provider) {
              ProtocolBuilderNumeric root = ((BuilderFactoryNumeric) provider).createSequential();

              NumericBuilder numeric = root.numeric();
              List<Computation<SInt>> secretCoefficients =
                  Arrays.stream(coefficients)
                      .mapToObj(BigInteger::valueOf)
                      .map((n) -> numeric.input(n, 1))
                      .collect(Collectors.toList());

              PolynomialImpl polynomial = new PolynomialImpl(secretCoefficients);
              Computation<SInt> secretX = numeric.input(BigInteger.valueOf(x), 1);

              Computation<SInt> result = root.seq(new PolynomialEvaluator(secretX, polynomial));

              outputs.add(numeric.open(result));

              return root.build();
            }
          };
          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

          int f = 0;
          int power = 1;
          for (int coefficient : coefficients) {
            f += coefficient * power;
            power *= x;
          }
          BigInteger result = app.getOutputs()[0];
          Assert.assertTrue(result.intValue() == f);
        }
      };
    }
  }
}
