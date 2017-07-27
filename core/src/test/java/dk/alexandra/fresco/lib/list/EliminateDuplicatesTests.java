package dk.alexandra.fresco.lib.list;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.list.FindDuplicatesProtocolBuilder;
import dk.alexandra.fresco.lib.list.SIntListofTuples;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Assert;

public class EliminateDuplicatesTests {

  private abstract static class ThreadWithFixture extends TestThread {

    protected SecureComputationEngine secureComputationEngine;

    @Override
    public void setUp() throws IOException {
      secureComputationEngine = SCEFactory
          .getSCEFromConfiguration(conf.sceConf, conf.sceConf.getSuite());
    }
  }

  public static class TestFindDuplicatesOne extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new ThreadWithFixture() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            private static final long serialVersionUID = 161228667004418796L;

            private BigInteger zero = BigInteger.valueOf(0);
            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger two = BigInteger.valueOf(2);
            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger four = BigInteger.valueOf(4);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory provider) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) provider;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) provider;
              NumericBitFactory numericBitFactory = (NumericBitFactory) provider;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) provider;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) provider;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();

              ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
                  80, bnFactory, localInvFactory,
                  numericBitFactory, expFromOIntFactory,
                  expFactory);

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              FindDuplicatesProtocolBuilder findDuplicatesBuilder = new FindDuplicatesProtocolBuilder(
                  compFactory, bnFactory);

              SInt[][] data = {{ioBuilder.input(zero, 1), ioBuilder.input(five, 1)},
                  {ioBuilder.input(one, 1), ioBuilder.input(zero, 1)},
                  {ioBuilder.input(two, 1), ioBuilder.input(two, 1)},
                  {ioBuilder.input(three, 1), ioBuilder.input(one, 1)},
                  {ioBuilder.input(four, 1), ioBuilder.input(three, 1)}};
              SIntListofTuples list1 = new SIntListofTuples(2);
              SIntListofTuples list2 = new SIntListofTuples(2);

              list1.add(data[0], ioBuilder.input(zero, 1));
              list1.add(data[1], ioBuilder.input(zero, 1));
              list1.add(data[2], ioBuilder.input(zero, 1));

              list2.add(data[2], ioBuilder.input(zero, 2));
              list2.add(data[3], ioBuilder.input(zero, 2));
              list2.add(data[4], ioBuilder.input(zero, 2));

              seq.append(ioBuilder.getProtocol());

              findDuplicatesBuilder.findDuplicates(list1, list2);

              outputs = new OInt[]{ioBuilder.output(list1.getDuplicate(0)),
                  ioBuilder.output(list1.getDuplicate(1)), ioBuilder.output(list1.getDuplicate(2))};

              seq.append(findDuplicatesBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[0].getValue());
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[1].getValue());
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[2].getValue());
        }
      };
    }
  }


  public static class TestFindDuplicatesTwo extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new ThreadWithFixture() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            private static final long serialVersionUID = 161228667004418796L;

            private BigInteger zero = BigInteger.valueOf(0);
            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger two = BigInteger.valueOf(2);
            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger four = BigInteger.valueOf(4);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory provider) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) provider;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) provider;
              NumericBitFactory numericBitFactory = (NumericBitFactory) provider;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) provider;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) provider;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();

              ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
                  80, bnFactory, localInvFactory,
                  numericBitFactory, expFromOIntFactory,
                  expFactory);

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              FindDuplicatesProtocolBuilder findDuplicatesBuilder = new FindDuplicatesProtocolBuilder(
                  compFactory, bnFactory);

              SInt[][] data = {{ioBuilder.input(zero, 1), ioBuilder.input(five, 1)},
                  {ioBuilder.input(one, 1), ioBuilder.input(zero, 1)},
                  {ioBuilder.input(two, 1), ioBuilder.input(two, 1)},
                  {ioBuilder.input(three, 1), ioBuilder.input(one, 1)},
                  {ioBuilder.input(four, 1), ioBuilder.input(three, 1)}};
              SIntListofTuples list1 = new SIntListofTuples(2);
              SIntListofTuples list2 = new SIntListofTuples(2);

              list1.add(data[0], ioBuilder.input(zero, 1));
              list1.add(data[1], ioBuilder.input(zero, 1));
              list1.add(data[2], ioBuilder.input(zero, 1));

              list2.add(data[2], ioBuilder.input(zero, 2));
              list2.add(data[3], ioBuilder.input(zero, 2));
              list2.add(data[4], ioBuilder.input(zero, 2));

              seq.append(ioBuilder.getProtocol());

              findDuplicatesBuilder.findDuplicates(list2, list1);

              outputs = new OInt[]{ioBuilder.output(list2.getDuplicate(0)),
                  ioBuilder.output(list2.getDuplicate(1)), ioBuilder.output(list2.getDuplicate(2))};

              seq.append(findDuplicatesBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[0].getValue());
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[1].getValue());
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[2].getValue());
        }
      };
    }
  }

  /*
   * Demonstrates elimination from list 2
   */
  public static class TestVerticalJoin extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new ThreadWithFixture() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {


            private static final long serialVersionUID = 161228667004418796L;

            private BigInteger zero = BigInteger.valueOf(0);
            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger two = BigInteger.valueOf(2);
            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger four = BigInteger.valueOf(4);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory provider) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) provider;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) provider;
              NumericBitFactory numericBitFactory = (NumericBitFactory) provider;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) provider;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) provider;
              SequentialProtocolProducer seq = new SequentialProtocolProducer();

              ComparisonProtocolFactoryImpl compFactory = new ComparisonProtocolFactoryImpl(
                  80, bnFactory, localInvFactory,
                  numericBitFactory, expFromOIntFactory,
                  expFactory);

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              FindDuplicatesProtocolBuilder findDuplicatesBuilder = new FindDuplicatesProtocolBuilder(
                  compFactory, bnFactory);

              SInt[][] data = {{ioBuilder.input(zero, 1), ioBuilder.input(five, 1)},
                  {ioBuilder.input(one, 1), ioBuilder.input(zero, 1)},
                  {ioBuilder.input(two, 1), ioBuilder.input(two, 1)},
                  {ioBuilder.input(three, 1), ioBuilder.input(one, 1)},
                  {ioBuilder.input(four, 1), ioBuilder.input(three, 1)}};
              SIntListofTuples list1 = new SIntListofTuples(2);
              SIntListofTuples list2 = new SIntListofTuples(2);

              list1.add(data[0], ioBuilder.input(zero, 1));
              list1.add(data[1], ioBuilder.input(zero, 1));
              list1.add(data[2], ioBuilder.input(zero, 1));

              list2.add(data[2], ioBuilder.input(zero, 2));
              list2.add(data[3], ioBuilder.input(zero, 2));
              list2.add(data[4], ioBuilder.input(zero, 2));

              seq.append(ioBuilder.getProtocol());

              findDuplicatesBuilder.findDuplicates(list2, list1);

              outputs = new OInt[]{ioBuilder.output(list2.getDuplicate(0)),
                  ioBuilder.output(list2.getDuplicate(1)), ioBuilder.output(list2.getDuplicate(2))};

              seq.append(findDuplicatesBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[0].getValue());
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[1].getValue());
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[2].getValue());
        }
      };
    }
  }
}
