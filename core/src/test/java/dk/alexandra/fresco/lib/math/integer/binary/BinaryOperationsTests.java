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
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder.RightShiftResult;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 *
 * Can be reused by a test case for any protocol suite that implements the basic
 * field protocol factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in
 * mpc.lib or something.
 */
public class BinaryOperationsTests {

  /**
   * Test binary right shift of a shared secret.
   */
  public static class TestRightShift extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {

      return new TestThread() {
        private final BigInteger input = BigInteger.valueOf(12332157);
        private final int shifts = 3;

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, List<BigInteger>>, SequentialNumericBuilder> app =
              (SequentialNumericBuilder builder) -> {
                AdvancedNumericBuilder rightShift = builder.advancedNumeric();
                Computation<SInt> encryptedInput = builder.numeric().known(input);
                Computation<RightShiftResult> shiftedRight = rightShift
                    .rightShiftWithRemainder(encryptedInput, shifts);
                NumericBuilder NumericBuilder = builder.numeric();
                Computation<BigInteger> openResult = NumericBuilder
                    .open(() -> shiftedRight.out().getResult());
                Computation<List<Computation<BigInteger>>> openRemainders = builder
                    .createSequentialSub((innerBuilder) -> {
                      NumericBuilder innerOpenBuilder = innerBuilder.numeric();
                      List<Computation<BigInteger>> opened = shiftedRight.out()
                          .getRemainder()
                          .stream()
                          .map(innerOpenBuilder::open)
                          .collect(Collectors.toList());
                      return () -> opened;
                    });
                return () -> new Pair<>(
                    openResult.out(),
                    openRemainders.out().stream()
                        .map(Computation::out)
                        .collect(Collectors.toList()));
              };
          Pair<BigInteger, List<BigInteger>> output =
              (Pair<BigInteger, List<BigInteger>>) secureComputationEngine.runApplication(
                  app,
                  ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger result = output.getFirst();
          List<BigInteger> remainders = output.getSecond();

          Assert.assertEquals(result, input.shiftRight(3));
          BigInteger lastRound = input;
          for (BigInteger remainder : remainders) {
            Assert.assertEquals(lastRound.mod(BigInteger.valueOf(2)), remainder);
            lastRound = lastRound.shiftRight(1);
          }
        }
      };
    }
  }


  /**
   * Test binary right shift of a shared secret.
   */
  public static class TestBitLength extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {

      return new TestThread() {
        private final BigInteger input = BigInteger.valueOf(5);
        private Computation<BigInteger> openResult;

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory producer) {
              return ProtocolBuilderNumeric
                  .createApplicationRoot((BuilderFactoryNumeric) producer, (builder) -> {
                    Computation<SInt> sharedInput = builder.numeric().known(input);
                    AdvancedNumericBuilder bitLengthBuilder = builder
                        .advancedNumeric();
                    Computation<SInt> bitLength = bitLengthBuilder
                        .bitLength(sharedInput, input.bitLength() * 2);
                    openResult = builder.numeric().open(bitLength);
                  }).build();
            }
          };
          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger result = openResult.out();

          Assert.assertEquals(BigInteger.valueOf(input.bitLength()), result);
        }
      };
    }
  }
}
