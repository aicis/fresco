/**
 * 
 */
package dk.alexandra.fresco.lib.arithmetic;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.Assert;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.logic.LogicProtocolBuilder;

/**
 * @author mortenvchristiansen
 *
 */
public class LogicTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf,
					conf.protocolSuiteConf);
		}

	}

	public static class TestLogic extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 1297709949887927667L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							BasicNumericFactory bnFactory = (BasicNumericFactory) provider;
							SequentialProtocolProducer seq = new SequentialProtocolProducer();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);

							LogicProtocolBuilder logicBuilder= new LogicProtocolBuilder(bnFactory);
							
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
							final SInt S_TRUE=logicBuilder.getTrueSInt();
							final SInt S_FALSE=logicBuilder.getFalseSInt();
							
							SInt andTT=logicBuilder.and(S_TRUE, S_TRUE);
							SInt andTF=logicBuilder.and(S_TRUE, S_FALSE);
							SInt andFT=logicBuilder.and(S_FALSE, S_TRUE);
							SInt andFF=logicBuilder.and(S_FALSE, S_FALSE);
						
							SInt notT=logicBuilder.not(S_TRUE);
							SInt notF=logicBuilder.not(S_FALSE);
		
							SInt orTT=logicBuilder.or(S_TRUE, S_TRUE);
							SInt orTF=logicBuilder.or(S_TRUE, S_FALSE);
							SInt orFT=logicBuilder.or(S_FALSE, S_TRUE);
							SInt orFF=logicBuilder.or(S_FALSE, S_FALSE);
							
							
							SInt xorTT=logicBuilder.xor(S_TRUE, S_TRUE);
							SInt xorTF=logicBuilder.xor(S_TRUE, S_FALSE);
							SInt xorFT=logicBuilder.xor(S_FALSE, S_TRUE);
							SInt xorFF=logicBuilder.xor(S_FALSE, S_FALSE);
							
							OInt res1 = ioBuilder.output(S_TRUE);
							OInt res2 = ioBuilder.output(S_FALSE);
							
							OInt res3 = ioBuilder.output(andTT);
							OInt res4 = ioBuilder.output(andTF);
							OInt res5 = ioBuilder.output(andFT);
							OInt res6 = ioBuilder.output(andFF);
								
							OInt res7 = ioBuilder.output(orTT);
							OInt res8 = ioBuilder.output(orTF);
							OInt res9 = ioBuilder.output(orFT);
							OInt res10 = ioBuilder.output(orFF);
						
							OInt res11 = ioBuilder.output(xorTT);
							OInt res12 = ioBuilder.output(xorTF);
							OInt res13 = ioBuilder.output(xorFT);
							OInt res14 = ioBuilder.output(xorFF);
							
							OInt res15 = ioBuilder.output(notT);
							OInt res16 = ioBuilder.output(notF);
							

							outputs = new OInt[] {res1,res2,res3,res4,res5,res6, res7, res8,res9,res10, res11, res12,res13,res14, res15, res16};
							
							seq.append(logicBuilder.getProtocol());
							seq.append(ioBuilder.getProtocol());

							return seq;
						}
					};
					sce.runApplication(app);
					sce.shutdownSCE();
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[0].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[1].getValue());
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[2].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[3].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[4].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[5].getValue());
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[6].getValue());
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[7].getValue());
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[8].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[9].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[10].getValue());
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[11].getValue());
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[12].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[13].getValue());
					Assert.assertEquals(BigInteger.ZERO, app.getOutputs()[14].getValue());
					Assert.assertEquals(BigInteger.ONE, app.getOutputs()[15].getValue());
		/*			 */
					/*
					
					*/
				}
			};	
		}
	}
}
