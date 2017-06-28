package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplicationBigInteger;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import java.math.BigInteger;
import org.junit.Assert;

/**
 * Tests which ensures that the SecureComputationEngine's parallel and sequential evaluations of
 * application works.
 *
 * @author Kasper Damgaard
 */
public class ParallelAndSequenceTests {

  public static class TestSequentialEvaluation extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplicationSum sumApp = new ParallelAndSequenceTests().new TestApplicationSum();
          TestApplicationMult multApp = new ParallelAndSequenceTests().new TestApplicationMult();

          secureComputationEngine
              .runApplication(sumApp, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          secureComputationEngine.runApplication(multApp,
              SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          BigInteger sum = sumApp.getResult();
          BigInteger mult = multApp.getResult();
          Assert
              .assertEquals(BigInteger.valueOf(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9), sum);
          Assert
              .assertEquals(BigInteger.valueOf(1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9), mult);
        }
      };
    }
  }

  public static class TestParallelEvaluation extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplicationSum sumApp = new ParallelAndSequenceTests().new TestApplicationSum();
          TestApplicationMult multApp = new ParallelAndSequenceTests().new TestApplicationMult();

          secureComputationEngine
              .runApplication(sumApp, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          secureComputationEngine.runApplication(multApp,
              SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          BigInteger sum = sumApp.getResult();
          BigInteger mult = multApp.getResult();
          Assert
              .assertEquals(BigInteger.valueOf(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9), sum);
          Assert
              .assertEquals(BigInteger.valueOf(1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9), mult);
        }
      };
    }
  }

  private class TestApplicationSum extends TestApplicationBigInteger {

    @Override
    public ProtocolProducer prepareApplication(BuilderFactory producer) {
      BuilderFactoryNumeric factoryNumeric = (BuilderFactoryNumeric) producer;
      OmniBuilder builder = new OmniBuilder(factoryNumeric);
      SInt[] terms = builder.getNumericIOBuilder()
          .inputArray(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, 1);
      SInt sum = builder.getNumericProtocolBuilder().sum(terms);
      output = builder.getNumericIOBuilder().output(sum);
      return builder.getProtocol();
    }

  }

  private class TestApplicationMult extends TestApplicationBigInteger {

    @Override
    public ProtocolProducer prepareApplication(BuilderFactory producer) {
      BuilderFactoryNumeric factoryNumeric = (BuilderFactoryNumeric) producer;
      OmniBuilder builder = new OmniBuilder(factoryNumeric);
      SInt[] terms = builder.getNumericIOBuilder()
          .inputArray(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, 1);
      SInt mult = builder.getNumericProtocolBuilder().mult(terms);
      output = builder.getNumericIOBuilder().output(mult);
      return builder.getProtocol();
    }

  }
}
