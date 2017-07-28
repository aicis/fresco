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
package dk.alexandra.fresco.lib.math.integer.linalg;

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
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;
import dk.alexandra.fresco.lib.lp.LPFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocolFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;
import org.junit.Assert;

public class LinAlgTests {


	public static class TestInnerProductClosed extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			
			return new TestThread() {

				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623441111137585L;
						
						private final BigInteger[] a = {new BigInteger("3"),
						    new BigInteger("5"), new BigInteger("7"), new BigInteger("9")};
						
						private final BigInteger[] b = {new BigInteger("2"),
                new BigInteger("2"), new BigInteger("2"), new BigInteger("2")};
            
						
						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							
							BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
							InnerProductFactory innerProductFactory = new InnerProductFactoryImpl(basicNumericFactory);
					
							SInt result = basicNumericFactory.getSInt();

							NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
							SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
							
							SInt[] input1 = ioBuilder.inputArray(a, 1);
							SInt[] input2 = ioBuilder.inputArray(b, 2);
							sequentialProtocolProducer.append(ioBuilder.getProtocol());

							ProtocolProducer p = innerProductFactory.getInnerProductProtocol(input1, input2, result);
							sequentialProtocolProducer.append(p);
							
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
					
					Assert.assertEquals(new BigInteger("48"), result);
				}
			};
		}
	}

	 public static class TestInnerProductClosedShort extends TestThreadFactory {

	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      
	      return new TestThread() {

	        @Override
	        public void test() throws Exception {
	          TestApplication app = new TestApplication() {

	            private static final long serialVersionUID = 701623441111137585L;
	            
	            private final BigInteger[] a = {new BigInteger("3")};
	            
	            private final BigInteger[] b = {new BigInteger("2")};
	            
	            
	            @Override
	            public ProtocolProducer prepareApplication(
	                ProtocolFactory provider) {
	              
	              BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
	              InnerProductFactory innerProductFactory = new InnerProductFactoryImpl(basicNumericFactory);
	          
	              SInt result = basicNumericFactory.getSInt();

	              NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
	              SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
	              
	              SInt[] input1 = ioBuilder.inputArray(a, 1);
	              SInt[] input2 = ioBuilder.inputArray(b, 2);
	              sequentialProtocolProducer.append(ioBuilder.getProtocol());

	              ProtocolProducer p = innerProductFactory.getInnerProductProtocol(input1, input2, result);
	              sequentialProtocolProducer.append(p);
	              
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
	          
	          Assert.assertEquals(new BigInteger("6"), result);
	        }
	      };
	    }
	  }


	 public static class TestInnerProductOpen extends TestThreadFactory {

	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      
	      return new TestThread() {

	        @Override
	        public void test() throws Exception {
	          TestApplication app = new TestApplication() {

	            private static final long serialVersionUID = 701623441111137585L;
	            
	            private final BigInteger[] a = {new BigInteger("3"),
	                new BigInteger("5"), new BigInteger("7"), new BigInteger("9")};
	            
	            private final BigInteger[] b = {new BigInteger("2"),
	                new BigInteger("2"), new BigInteger("2"), new BigInteger("2")};
	            
	            
	            @Override
	            public ProtocolProducer prepareApplication(
	                ProtocolFactory provider) {
	              
	              BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
	              InnerProductFactory innerProductFactory = new InnerProductFactoryImpl(basicNumericFactory);
	          
	              SInt result = basicNumericFactory.getSInt();

	              NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
	              SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
	              
	              SInt[] input1 = ioBuilder.inputArray(a, 1);
	              OInt[] input2 = new OInt[4];
	              for(int i = 0; i< input2.length; i++) {
	                input2[i] = basicNumericFactory.getOInt(b[i]);
	              }
	              
	              sequentialProtocolProducer.append(ioBuilder.getProtocol());

	              ProtocolProducer p = innerProductFactory.getInnerProductProtocol(input1, input2, result);
	              sequentialProtocolProducer.append(p);
	              
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
	          
	          Assert.assertEquals(new BigInteger("48"), result);
	        }
	      };
	    }
	  }

   public static class TestInnerProductOpenShort extends TestThreadFactory {

     @Override
     public TestThread next(TestThreadConfiguration conf) {
       
       return new TestThread() {

         @Override
         public void test() throws Exception {
           TestApplication app = new TestApplication() {

             private static final long serialVersionUID = 701623441111137585L;
             
             private final BigInteger[] a = {new BigInteger("3")};
             
             private final BigInteger[] b = {new BigInteger("2")};
             
             
             @Override
             public ProtocolProducer prepareApplication(
                 ProtocolFactory provider) {
               
               BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
               InnerProductFactory innerProductFactory = new InnerProductFactoryImpl(basicNumericFactory);
           
               SInt result = basicNumericFactory.getSInt();

               NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
               SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
               
               SInt[] input1 = ioBuilder.inputArray(a, 1);
               OInt[] input2 = new OInt[]{basicNumericFactory.getOInt(b[0])};
               
               
               sequentialProtocolProducer.append(ioBuilder.getProtocol());

               ProtocolProducer p = innerProductFactory.getInnerProductProtocol(input1, input2, result);
               sequentialProtocolProducer.append(p);
               
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
           
           Assert.assertEquals(new BigInteger("6"), result);
         }
       };
     }
   }

   // TODO: The tested class is no referenced anywhere and therefore not used
   public static class TestAltInnerProductClosed extends TestThreadFactory {

     @Override
     public TestThread next(TestThreadConfiguration conf) {
       
       return new TestThread() {

         @Override
         public void test() throws Exception {
           TestApplication app = new TestApplication() {

             private static final long serialVersionUID = 701623441111137585L;
             
             private final BigInteger[] a = {new BigInteger("3"),
                 new BigInteger("5"), new BigInteger("7"), new BigInteger("9")};
             
             private final BigInteger[] b = {new BigInteger("2"),
                 new BigInteger("2"), new BigInteger("2"), new BigInteger("2")};
             
             
             @Override
             public ProtocolProducer prepareApplication(
                 ProtocolFactory provider) {
               
               BasicNumericFactory basicNumericFactory = (BasicNumericFactory) provider;
               LocalInversionFactory localInvFactory = (LocalInversionFactory) provider;
               NumericBitFactory numericBitFactory = (NumericBitFactory) provider;
               ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) provider;
               PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) provider;
               RandomFieldElementFactory randFactory = (RandomFieldElementFactory) provider;
               LPFactory copyFactory = new LPFactoryImpl(80, basicNumericFactory, localInvFactory, numericBitFactory,
                   expFromOIntFactory, expFactory, randFactory);
               
               SInt result = basicNumericFactory.getSInt();

               NumericIOBuilder ioBuilder = new NumericIOBuilder(basicNumericFactory);
               SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
               
               SInt[] input1 = ioBuilder.inputArray(a, 1);
               SInt[] input2 = ioBuilder.inputArray(b, 2);
               sequentialProtocolProducer.append(ioBuilder.getProtocol());
               
               ProtocolProducer p = new AltInnerProductProtocolImpl(input1, input2, result, basicNumericFactory, copyFactory); 
               sequentialProtocolProducer.append(p);
               
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
           
           Assert.assertEquals(new BigInteger("48"), result);
         }
       };
     }
   }

   
}
