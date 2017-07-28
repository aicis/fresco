/*******************************************************************************
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
package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;
import dk.alexandra.fresco.lib.lp.LPFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocolFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;
import org.junit.Assert;

public class ExponentiationTests {

	/**
	 * Test binary right shift of a shared secret.
	 */
	public static class TestExponentiation extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {
				private final BigInteger input = BigInteger.valueOf(12332157);
				private final int exp = 12;

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
							NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) provider;
							RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
							LocalInversionFactory localInversionFactory = (LocalInversionFactory) provider;
							RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
							IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory, rightShiftFactory);
							ExponentiationFactory exponentiationFactory = new ExponentiationFactoryImpl(basicNumericFactory, integerToBitsFactory);

							SInt result = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt input1 = ioBuilder.input(input, 1);
							SInt input2 = ioBuilder.input(exp, 2);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							ExponentiationProtocol exponentiationProtocol = exponentiationFactory.getExponentiationCircuit(input1, input2, 5, result);
							sequentialProtocolProducer.append(exponentiationProtocol);
							
							OInt output1 = ioBuilder.output(result);
							
							sequentialProtocolProducer.append(ioBuilder.getProtocol());
							
							ProtocolProducer gp = sequentialProtocolProducer;
							
							outputs = new OInt[] {output1};
							
							return gp;
						}
					};
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          BigInteger result = app.getOutputs()[0].getValue();
					
					Assert.assertEquals(input.pow(exp), result);
				}
			};
		}
	}

	 public static class TestExponentiationOInt extends TestThreadFactory {

	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      
	      return new TestThread() {
	        private final BigInteger input = BigInteger.valueOf(12332157);
	        private final int exp = 12;

	        @Override
	        public void test() throws Exception {
	          TestApplication app = new TestApplication() {

	            private static final long serialVersionUID = 701623441111137585L;
	            
	            @Override
	            public ProtocolProducer prepareApplication(
	                ProtocolFactory provider) {
	              
	              BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
	              NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) provider;
	              RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
	              LocalInversionFactory localInversionFactory = (LocalInversionFactory) provider;
	              RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
	              IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory, rightShiftFactory);
	              ExponentiationFactory exponentiationFactory = new ExponentiationFactoryImpl(basicNumericFactory, integerToBitsFactory);

	              SInt result = basicNumericFactory.getSInt();

	              NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
	              SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
	              
	              OInt input1 = basicNumericFactory.getOInt(input);
	              SInt input2 = ioBuilder.input(exp, 2);
	              sequentialProtocolProducer.append(ioBuilder.getProtocol());

	              ExponentiationProtocol exponentiationProtocol = exponentiationFactory.getExponentiationCircuit(input1, input2, 5, result);
	              sequentialProtocolProducer.append(exponentiationProtocol);
	              
	              OInt output1 = ioBuilder.output(result);
	              
	              sequentialProtocolProducer.append(ioBuilder.getProtocol());
	              
	              ProtocolProducer gp = sequentialProtocolProducer;
	              
	              outputs = new OInt[] {output1};
	              
	              return gp;
	            }
	          };
	          secureComputationEngine
	              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
	          BigInteger result = app.getOutputs()[0].getValue();
	          
	          Assert.assertEquals(input.pow(exp), result);
	        }
	      };
	    }
	  }

	 
   public static class TestExponentiationPipe extends TestThreadFactory {

     @Override
     public TestThread next(TestThreadConfiguration conf) {
       
       return new TestThread() {
         private final BigInteger input = BigInteger.valueOf(12332157);
         private final int exp = 12;

         @Override
         public void test() throws Exception {
           TestApplication app = new TestApplication() {

             private static final long serialVersionUID = 701623441111137585L;
             
             @Override
             public ProtocolProducer prepareApplication(
                 ProtocolFactory provider) {
               
               BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
               NumericBitFactory numericBitFactory = (NumericBitFactory) provider;
               RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, numericBitFactory);
               LocalInversionFactory localInversionFactory = (LocalInversionFactory) provider;
               RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
               
               ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) provider;
               PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) provider;
               RandomFieldElementFactory randFactory = (RandomFieldElementFactory) provider;
               LPFactory lpFactory = new LPFactoryImpl(80, basicNumericFactory,
                   localInversionFactory, numericBitFactory,
                   expFromOIntFactory, expFactory, randFactory);

               ExponentiationPipeFactory exponentiationFactory = 
                   new ExponentiationPipeFactoryImpl(basicNumericFactory, lpFactory, lpFactory);

               
               NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
               SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
               
               SInt[] result = new SInt[10];
               for(int i = 0; i< result.length; i++) {
                 result[i] = basicNumericFactory.getSInt();
               }

               SInt x = ioBuilder.input(input, 2);
               sequentialProtocolProducer.append(ioBuilder.getProtocol());

               
               ExponentiationPipeProtocol exponentiationPipeProtocol = 
                   exponentiationFactory.getExponentiationProtocol(x, result);
               sequentialProtocolProducer.append(exponentiationPipeProtocol);
               
               OInt[] output1 = ioBuilder.outputArray(result);
               
               sequentialProtocolProducer.append(ioBuilder.getProtocol());
               
               ProtocolProducer gp = sequentialProtocolProducer;
               
               outputs = output1;
               
               return gp;
             }
           };
           secureComputationEngine
               .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));

           for(int i = 1; i< app.getOutputs().length; i++) {
             BigInteger tmp  = app.getOutputs()[i].getValue();
             Assert.assertEquals(input.pow(i), tmp);
           }
         }
       };
     }
   }
}
