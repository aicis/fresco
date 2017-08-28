package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.math.logic.LogicProtocolBuilder;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Assert;

/**
 * ]
 *
 * @author mortenvchristiansen
 */
public class LogicTests {

  private abstract static class ThreadWithFixture<ResourcePoolT extends ResourcePool>
      extends TestThread<ResourcePoolT, ProtocolBuilderNumeric> {

    protected SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> secureComputationEngine;

    @Override
    public void setUp() throws IOException {
      secureComputationEngine = SCEFactory
          .getSCEFromConfiguration(conf.sceConf.getSuite(), conf.sceConf.getEvaluator());
    }

  }

  public static class TestLogic<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration conf) {
      return new ThreadWithFixture<ResourcePoolT>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory provider) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) provider.getProtocolFactory();
              SequentialProtocolProducer seq = new SequentialProtocolProducer();

              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);

              LogicProtocolBuilder logicBuilder = new LogicProtocolBuilder(bnFactory);

              seq.append(ioBuilder.getProtocol());

              //set up logic tests
              //constants 0 and 2 (not 1)
              /*
               * TT,TF,FT and FF for
							 * and, or, xor
							 * T and F for not
							 * and get T and F 
							 * 
							 */
              final SInt S_TRUE = logicBuilder.getTrueSInt();
              final SInt S_FALSE = logicBuilder.getFalseSInt();

              SInt andTT = logicBuilder.and(S_TRUE, S_TRUE);
              SInt andTF = logicBuilder.and(S_TRUE, S_FALSE);
              SInt andFT = logicBuilder.and(S_FALSE, S_TRUE);
              SInt andFF = logicBuilder.and(S_FALSE, S_FALSE);

              SInt notT = logicBuilder.not(S_TRUE);
              SInt notF = logicBuilder.not(S_FALSE);

              SInt orTT = logicBuilder.or(S_TRUE, S_TRUE);
              SInt orTF = logicBuilder.or(S_TRUE, S_FALSE);
              SInt orFT = logicBuilder.or(S_FALSE, S_TRUE);
              SInt orFF = logicBuilder.or(S_FALSE, S_FALSE);

              SInt xorTT = logicBuilder.xor(S_TRUE, S_TRUE);
              SInt xorTF = logicBuilder.xor(S_TRUE, S_FALSE);
              SInt xorFT = logicBuilder.xor(S_FALSE, S_TRUE);
              SInt xorFF = logicBuilder.xor(S_FALSE, S_FALSE);

              Computation<BigInteger> res1 = ioBuilder.output(S_TRUE);
              Computation<BigInteger> res2 = ioBuilder.output(S_FALSE);

              Computation<BigInteger> res3 = ioBuilder.output(andTT);
              Computation<BigInteger> res4 = ioBuilder.output(andTF);
              Computation<BigInteger> res5 = ioBuilder.output(andFT);
              Computation<BigInteger> res6 = ioBuilder.output(andFF);

              Computation<BigInteger> res7 = ioBuilder.output(orTT);
              Computation<BigInteger> res8 = ioBuilder.output(orTF);
              Computation<BigInteger> res9 = ioBuilder.output(orFT);
              Computation<BigInteger> res10 = ioBuilder.output(orFF);

              Computation<BigInteger> res11 = ioBuilder.output(xorTT);
              Computation<BigInteger> res12 = ioBuilder.output(xorTF);
              Computation<BigInteger> res13 = ioBuilder.output(xorFT);
              Computation<BigInteger> res14 = ioBuilder.output(xorFF);

              Computation<BigInteger> res15 = ioBuilder.output(notT);
              Computation<BigInteger> res16 = ioBuilder.output(notF);

              outputs = Arrays.asList(res1, res2, res3, res4, res5, res6, res7, res8, res9, res10,
                  res11, res12, res13, res14, res15, res16);

              seq.append(logicBuilder.getProtocol());
              seq.append(ioBuilder.getProtocol());

              return seq;
            }
          };
          ResourcePoolT resourcePool =
              ResourcePoolCreator.createResourcePool(conf.sceConf);
          secureComputationEngine.runApplication(app, resourcePool);
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[0]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[1]);
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[2]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[3]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[4]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[5]);
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[6]);
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[7]);
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[8]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[9]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[10]);
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[11]);
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[12]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[13]);
          Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[14]);
          Assert.assertEquals(BigInteger.ONE, app.getOutputs()[15]);
    /*			 */
          /*

					*/
        }
      };
    }
  }
}
