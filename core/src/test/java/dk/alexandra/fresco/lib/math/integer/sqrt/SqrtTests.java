/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
 */
package dk.alexandra.fresco.lib.math.integer.sqrt;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.InputBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

public class SqrtTests {

  public static class TestSquareRoot extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {

      return new TestThread() {

        private final BigInteger[] x = new BigInteger[]{
            BigInteger.valueOf(1234),
            BigInteger.valueOf(12345),
            BigInteger.valueOf(123456),
            BigInteger.valueOf(1234567),
            BigInteger.valueOf(12345678),
            BigInteger.valueOf(123456789)
        };
        private final int n = x.length;

        List<Computation<OInt>> results = new ArrayList<>(n);

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    InputBuilder sIntFactory = builder.createInputBuilder();

                    results = new ArrayList<>(n);

                    for (BigInteger input : x) {
                      Computation<SInt> actualInput = sIntFactory.known(input);
                      Computation<SInt> result = builder.createAdvancedNumericBuilder()
                          .sqrt(actualInput, input.bitLength());
                      Computation<OInt> openResult = builder.createOpenBuilder().open(result);
                      results.add(openResult);
                    }
                  }).build();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(n, results.size());

          for (int i = 0; i < results.size(); i++) {
            Computation<OInt> result = results.get(i);
            BigInteger actual = result.out().getValue();
            BigInteger expected = BigInteger.valueOf((long) Math.sqrt(x[i].intValue()));

            BigInteger difference = expected.subtract(actual).abs();

            int precision = expected.bitLength() - difference.bitLength();

            boolean shouldBeCorrect = precision >= expected.bitLength();
            boolean isCorrect = expected.equals(actual);

            Assert.assertFalse(shouldBeCorrect && !isCorrect);

            System.out.println(
                "sqrt(" + x[i] + ") = " + actual + ", expected " + expected + ".");
            Assert.assertTrue(isCorrect);
          }
        }
      };
    }
  }
}
