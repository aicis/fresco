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
 *******************************************************************************/
package dk.alexandra.fresco.lib.math.integer.min;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;
import dk.alexandra.fresco.lib.lp.LPFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;

import org.hamcrest.core.Is;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 * 
 * Can be reused by a test case for any protocol suite that implements the basic
 * field protocol factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in
 * mpc.lib or something.
 *
 */
public class MinTests {

	public static class TestMinimumProtocol extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final int[] data1 = {200, 144, 99, 211, 930,543,520,532,497,450,432};
								
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {

							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
							NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
							ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)factory;
							PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)factory;
							LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, basicNumericFactory, localInversionFactory,
                  preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory,
                  (RandomFieldElementFactory)factory);

              
							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt[] input = ioBuilder.inputArray(data1, 1);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							SInt[] result = basicNumericFactory.getSIntArray(11);
              SInt inputM = basicNumericFactory.getSInt();
              
							sequentialProtocolProducer.append(lpFactory.getMinimumProtocol(input, inputM, result));
														
							OInt[] output = ioBuilder.outputArray(result);
							OInt o = ioBuilder.output(inputM);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							outputs = new OInt[output.length+1];
							for(int i = 0; i< output.length; i++) {
							  outputs[i] = output[i];
							}
							outputs[output.length] = o;

							return sequentialProtocolProducer;
						}
					};
					secureComputationEngine
							.runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
					
					Assert.assertThat(app.getOutputs()[2].getValue(), Is.is(BigInteger.ONE));
					Assert.assertThat(app.getOutputs()[11].getValue(), Is.is(new BigInteger("99")));
				}
			};
		}
	}

	
  public static class TestMinimumFraction extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      
      return new TestThread() {
        private final int[] data1 = {20, 14, 9, 21, 93, 54, 52, 53, 49, 45, 43};
        private final int[] data2 = {140, 120, 90, 191, 123, 4, 122, 153, 149, 145, 143};
                
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 701623441111137585L;
            
            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory factory) {

              BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
              NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)factory;
              PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)factory;
              LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, basicNumericFactory, localInversionFactory,
                  preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory,
                  (RandomFieldElementFactory)factory);

              
              NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
              SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
              
              SInt[] inputN = ioBuilder.inputArray(data1, 1);
              SInt[] inputD = ioBuilder.inputArray(data2, 1);
              sequentialProtocolProducer.append(ioBuilder.getProtocol());

              SInt[] result = basicNumericFactory.getSIntArray(11);

              SInt inputNM = basicNumericFactory.getSInt();
              SInt inputDM = basicNumericFactory.getSInt();              
              
              sequentialProtocolProducer.append(lpFactory.getMinimumFractionProtocol(inputN, inputD, inputNM, inputDM, result));
                            
              OInt[] output = ioBuilder.outputArray(result);
              OInt nm = ioBuilder.output(inputNM);
              OInt dm = ioBuilder.output(inputDM);

              sequentialProtocolProducer.append(ioBuilder.getProtocol());
              
              outputs = new OInt[output.length+2];
              for(int i = 0; i< output.length; i++) {
                outputs[i] = output[i];
              }
              outputs[output.length] = nm;
              outputs[output.length+1] = dm;

              return sequentialProtocolProducer;
            }
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          
          Assert.assertThat(app.getOutputs()[2].getValue(), Is.is(BigInteger.ONE));
          
          Assert.assertThat(app.getOutputs()[11].getValue(), Is.is(new BigInteger("9")));
          Assert.assertThat(app.getOutputs()[12].getValue(), Is.is(new BigInteger("90")));
          
        }
      };
    }
  }

  public static class TestMinInfFrac extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      
      return new TestThread() {
        private final int[] data1 = {20, 14, 9, 21, 93, 54, 52, 53, 49, 45, 43};
        private final int[] data2 = {140, 120, 90, 191, 123, 4, 122, 153, 149, 145, 143};
        private final int[] data3 = {0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0};
                
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 701623441111137585L;
            
            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory factory) {

              BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
              NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)factory;
              PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)factory;
              LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, basicNumericFactory, localInversionFactory,
                  preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory,
                  (RandomFieldElementFactory)factory);

              
              NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
              SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
              
              SInt[] inputN = ioBuilder.inputArray(data1, 1);
              SInt[] inputD = ioBuilder.inputArray(data2, 1);
              SInt[] inputI = ioBuilder.inputArray(data3, 1);
              sequentialProtocolProducer.append(ioBuilder.getProtocol());

              SInt[] result = basicNumericFactory.getSIntArray(11);

              SInt inputNM = basicNumericFactory.getSInt();
              SInt inputDM = basicNumericFactory.getSInt();
              SInt inputInf = basicNumericFactory.getSInt();
              
              sequentialProtocolProducer.append(
                  lpFactory.getMinInfFracProtocol(inputN, inputD, inputI, 
                      inputNM, inputDM, inputInf, result));
                            
              OInt[] output = ioBuilder.outputArray(result);
              OInt nm = ioBuilder.output(inputNM);
              OInt dm = ioBuilder.output(inputDM);
              OInt infm = ioBuilder.output(inputInf);

              sequentialProtocolProducer.append(ioBuilder.getProtocol());
              
              outputs = new OInt[output.length+3];
              for(int i = 0; i< output.length; i++) {
                outputs[i] = output[i];
              }
              outputs[output.length] = nm;
              outputs[output.length+1] = dm;
              outputs[output.length+2] = infm;

              return sequentialProtocolProducer;
            }
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          
          Assert.assertThat(app.getOutputs()[2].getValue(), Is.is(BigInteger.ONE));
          
          Assert.assertThat(app.getOutputs()[11].getValue(), Is.is(new BigInteger("9")));
          Assert.assertThat(app.getOutputs()[12].getValue(), Is.is(new BigInteger("90")));
          Assert.assertThat(app.getOutputs()[13].getValue(), Is.is(new BigInteger("0")));
          
        }
      };
    }
  }
  
  public static class TestMinInfFracTrivial extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      
      return new TestThread() {
        private final int[] data1 = {20};
        private final int[] data2 = {140};
        private final int[] data3 = {0};
                
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 701623441111137585L;
            
            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory factory) {

              BasicNumericFactory basicNumericFactory = (BasicNumericFactory) factory;
              NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)factory;
              PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)factory;
              LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, basicNumericFactory, localInversionFactory,
                  preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory,
                  (RandomFieldElementFactory)factory);

              
              NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
              SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
              
              SInt[] inputN = ioBuilder.inputArray(data1, 1);
              SInt[] inputD = ioBuilder.inputArray(data2, 1);
              SInt[] inputI = ioBuilder.inputArray(data3, 1);
              sequentialProtocolProducer.append(ioBuilder.getProtocol());

              SInt[] result = basicNumericFactory.getSIntArray(1);

              SInt inputNM = basicNumericFactory.getSInt();
              SInt inputDM = basicNumericFactory.getSInt();
              SInt inputInf = basicNumericFactory.getSInt();
              
              sequentialProtocolProducer.append(
                  lpFactory.getMinInfFracProtocol(inputN, inputD, inputI, 
                      inputNM, inputDM, inputInf, result));
                            
              OInt[] output = ioBuilder.outputArray(result);
              OInt nm = ioBuilder.output(inputNM);
              OInt dm = ioBuilder.output(inputDM);
              OInt infm = ioBuilder.output(inputInf);

              sequentialProtocolProducer.append(ioBuilder.getProtocol());
              
              outputs = new OInt[output.length+3];
              for(int i = 0; i< output.length; i++) {
                outputs[i] = output[i];
              }
              outputs[output.length] = nm;
              outputs[output.length+1] = dm;
              outputs[output.length+2] = infm;

              return sequentialProtocolProducer;
            }
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          
          Assert.assertThat(app.getOutputs()[0].getValue(), Is.is(BigInteger.ONE));
          
          Assert.assertThat(app.getOutputs()[1].getValue(), Is.is(new BigInteger("20")));
          Assert.assertThat(app.getOutputs()[2].getValue(), Is.is(new BigInteger("140")));
          Assert.assertThat(app.getOutputs()[3].getValue(), Is.is(new BigInteger("0")));
          
        }
      };
    }
  }

}
