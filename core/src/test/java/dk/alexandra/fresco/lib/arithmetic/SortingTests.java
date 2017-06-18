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

import dk.alexandra.fresco.framework.FactoryNumericProducer;
import dk.alexandra.fresco.framework.FactoryProducer;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.SortingProtocolBuilder;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;
import java.util.Random;
import org.junit.Assert;

public class SortingTests {

  public static class TestIsSorted extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            /**
             *
             */
            private static final long serialVersionUID = 7960372460887688296L;

            private BigInteger zero = BigInteger.valueOf(0);
            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger two = BigInteger.valueOf(2);
            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger four = BigInteger.valueOf(4);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory bnFactory = (BasicNumericFactory) producer;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) producer;
              NumericBitFactory numericBitFactory = (NumericBitFactory) producer;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) producer;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) producer;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();

              ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
                  80, bnFactory, localInvFactory,
                  numericBitFactory, expFromOIntFactory,
                  expFactory, (FactoryNumericProducer) factoryProducer);

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              SortingProtocolBuilder isSortedBuilder = new SortingProtocolBuilder(compFactory,
                  bnFactory);

              SInt[] unsorted = {ioBuilder.input(one, 1), ioBuilder.input(two, 1),
                  ioBuilder.input(three, 1), ioBuilder.input(five, 2), ioBuilder.input(zero, 1)};
              SInt[] sorted = {ioBuilder.input(three, 1), ioBuilder.input(four, 1),
                  ioBuilder.input(four, 2)};

              seq.append(ioBuilder.getProtocol());

              SInt isSortedResult1 = isSortedBuilder.isSorted(unsorted);
              SInt isSortedResult2 = isSortedBuilder.isSorted(sorted);
              OInt res1 = ioBuilder.output(isSortedResult1);
              OInt res2 = ioBuilder.output(isSortedResult2);

              outputs = new OInt[]{res1, res2};

              seq.append(isSortedBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[0].getValue());
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[1].getValue());
        }
      };
    }
  }

  public static class TestCompareAndSwap extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = -4348646550124325541L;


            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger two = BigInteger.valueOf(2);

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory bnFactory = (BasicNumericFactory) producer;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) producer;
              NumericBitFactory numericBitFactory = (NumericBitFactory) producer;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) producer;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) producer;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();

              ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
                  80, bnFactory, localInvFactory,
                  numericBitFactory, expFromOIntFactory,
                  expFactory, (FactoryNumericProducer) factoryProducer);

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              SortingProtocolBuilder isSortedBuilder = new SortingProtocolBuilder(compFactory,
                  bnFactory);

              SInt[] vals = {ioBuilder.input(two, 1), ioBuilder.input(one, 2)};
              seq.append(ioBuilder.getProtocol());

              isSortedBuilder.compareAndSwap(0, 1, vals);
              OInt res1 = ioBuilder.output(vals[0]);
              OInt res2 = ioBuilder.output(vals[1]);

              outputs = new OInt[]{res1, res2};

              seq.append(isSortedBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[0].getValue());
          Assert.assertEquals(BigInteger.valueOf(2), app.getOutputs()[1].getValue());
        }
      };
    }
  }


  public static class TestSort extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = -8902341205146087123L;

            private BigInteger zero = BigInteger.valueOf(0);
            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();

              BasicNumericFactory bnFactory = (BasicNumericFactory) producer;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) producer;
              NumericBitFactory numericBitFactory = (NumericBitFactory) producer;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) producer;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) producer;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();

              ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
                  80, bnFactory, localInvFactory,
                  numericBitFactory, expFromOIntFactory,
                  expFactory, (FactoryNumericProducer) factoryProducer);

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              SortingProtocolBuilder isSortedBuilder = new SortingProtocolBuilder(compFactory,
                  bnFactory);

              //two arrays w. identical data
              SInt[] unsorted = {ioBuilder.input(one, 1), ioBuilder.input(three, 2),
                  ioBuilder.input(three, 1), ioBuilder.input(five, 2), ioBuilder.input(zero, 1)};
              SInt[] toBeSorted = {ioBuilder.input(one, 1), ioBuilder.input(three, 2),
                  ioBuilder.input(three, 1), ioBuilder.input(five, 2), ioBuilder.input(zero, 1)};

              seq.append(ioBuilder.getProtocol());

              SInt isSortedResult1 = isSortedBuilder.isSorted(unsorted);
              //sorted version of same data.
              isSortedBuilder.sort(toBeSorted);
              SInt isSortedResult2 = isSortedBuilder.isSorted(toBeSorted);
              OInt res1 = ioBuilder.output(isSortedResult1);
              OInt res2 = ioBuilder.output(isSortedResult2);
              OInt res3 = ioBuilder.output(toBeSorted[0]);
              OInt res4 = ioBuilder.output(toBeSorted[1]);
              OInt res5 = ioBuilder.output(toBeSorted[2]);
              OInt res6 = ioBuilder.output(toBeSorted[3]);
              OInt res7 = ioBuilder.output(toBeSorted[4]);

              outputs = new OInt[]{res1, res2, res3, res4, res5, res6, res7};

              seq.append(isSortedBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          secureComputationEngine.shutdownSCE();
          Assert
              .assertEquals(BigInteger.ZERO, app.getOutputs()[0].getValue()); //unsorted is unsorted
          Assert.assertEquals(BigInteger.valueOf(0), app.getOutputs()[2].getValue());
          Assert.assertEquals(BigInteger.valueOf(1), app.getOutputs()[3].getValue());
          Assert.assertEquals(BigInteger.valueOf(3), app.getOutputs()[4].getValue());
          Assert.assertEquals(BigInteger.valueOf(3), app.getOutputs()[5].getValue());
          Assert.assertEquals(BigInteger.valueOf(5), app.getOutputs()[6].getValue());
          Assert
              .assertEquals(BigInteger.ONE, app.getOutputs()[1].getValue()); //tobesorted is sorted
        }
      };
    }
  }

  public static class TestBigSort extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = -8146995829636557516L;
            final int SIZE = 100;

            @Override
            public ProtocolProducer prepareApplication(
                FactoryProducer factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();

              BasicNumericFactory bnFactory = (BasicNumericFactory) producer;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) producer;
              NumericBitFactory numericBitFactory = (NumericBitFactory) producer;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) producer;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) producer;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              Random random = new Random();

              ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
                  80, bnFactory, localInvFactory,
                  numericBitFactory, expFromOIntFactory,
                  expFactory, (FactoryNumericProducer) factoryProducer);

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              SortingProtocolBuilder isSortedBuilder = new SortingProtocolBuilder(compFactory,
                  bnFactory);

              //large, random array

              SInt[] toBeSorted = new SInt[SIZE];
//							int[] users=new int[SIZE];
              BigInteger[] vals = new BigInteger[SIZE];
//							for (int i=0;i<SIZE;i++) users[i]=random.nextInt(2)+1;
              for (int i = 0; i < SIZE; i++) {
                vals[i] = BigInteger.valueOf(random.nextInt(SIZE));
              }

              for (int i = 0; i < toBeSorted.length; i++) {
                toBeSorted[i] = ioBuilder.input(vals[i], 1);
              }
              seq.append(ioBuilder.getProtocol());

              SInt isSortedResult1 = isSortedBuilder.isSorted(toBeSorted);

              //sorted version of same data.
              isSortedBuilder.sort(toBeSorted);
              SInt isSortedResult2 = isSortedBuilder.isSorted(toBeSorted);
              OInt res1 = ioBuilder.output(isSortedResult1);
              OInt res2 = ioBuilder.output(isSortedResult2);

              outputs = new OInt[]{res1, res2};

              seq.append(isSortedBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ZERO,
              app.getOutputs()[0].getValue()); //unsorted is unsorted to start
          Assert.assertEquals(BigInteger.ONE,
              app.getOutputs()[1].getValue()); //tobesorted is sorted at the end
        }
      };
    }
  }

}
