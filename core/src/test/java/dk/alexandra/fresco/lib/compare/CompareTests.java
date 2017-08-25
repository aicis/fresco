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
package dk.alexandra.fresco.lib.compare;


/**
 * Test class for the DEASolver.
 * Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem
 * as inputs (i.e. the number of input and output variables, the number
 * of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.    
 *
 */
public class CompareTests {
	/* TODO 
	 public static class TestCompareAndSwap extends TestThreadFactory {
	    
	    public TestCompareAndSwap() {
	    }

	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      return new TestThread() {
	        @Override
	        public void test() throws Exception {
	          
	          boolean[] left = ByteArithmetic.toBoolean("ee");
	          boolean[] right = ByteArithmetic.toBoolean("00");
	          
	          OBool[][] result = new OBool[4][16];
	          
	          TestApplication app = new TestApplication() {

	            private static final long serialVersionUID = 4338818809103728010L;

	            @Override
	            public ProtocolProducer prepareApplication(
	                BuilderFactory factoryProducer) {
	              ProtocolFactory producer = factoryProducer.getProtocolFactory();
	               AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
	                BasicLogicBuilder builder = new BasicLogicBuilder(prov);
	                SequentialProtocolProducer sseq = new SequentialProtocolProducer();
	               
	                
	                SBool[] sLeft = builder.knownSBool(left);
                  SBool[] sRight = builder.knownSBool(right);
                  
                  SBool[] sLeft2 = builder.knownSBool(left);
                  SBool[] sRight2 = builder.knownSBool(right);
                  
                  
	                //TODO not found in builder
	                CompareAndSwapProtocol swap = prov.getCompareAndSwapProtocol(sLeft, sRight);
	                
	              //TODO not found in builder
                  CompareAndSwapProtocol swapReverseOrder = prov.getCompareAndSwapProtocol(sRight2, sLeft2);
                  
	                
	                sseq.append(builder.getProtocol());
	         
	                sseq.append(swap);
	                sseq.append(swapReverseOrder);
	                
	                result[0] = builder.output(sLeft);
	                result[1] = builder.output(sRight);
	                result[2] = builder.output(sLeft2);
                  result[3] = builder.output(sRight2);
	                sseq.append(builder.getProtocol());
	              
	              return sseq;
	            }
	          };
	          secureComputationEngine
	              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

	          Assert.assertArrayEquals(left, convertOBoolToBool(result[0]));
	          Assert.assertArrayEquals(right, convertOBoolToBool(result[1]));
	          Assert.assertArrayEquals(left, convertOBoolToBool(result[3]));
            Assert.assertArrayEquals(right, convertOBoolToBool(result[2]));  
	        }
	      };
	    }
	  }

	 
   private static int valueOfBools(boolean[] boolInput) {
     boolean[] bool = boolInput;
     ArrayUtils.reverse(bool);
     int res = 0;
     int count = 2;
     if(bool[0]) {
       res = 1;
     }
     for(int i= 1;i < bool.length; i++) {
       if(bool[i]) {
         res += count;
       }
       count = count*2;
     }
   return res;
   }
   
   private static boolean[] randomBoolArray(int size, Random rand) {
     boolean[] output = new boolean[size];
     for(int i = 0; i< size; i++) {
       output[i] = rand.nextBoolean();
     }
     return output;
   }
   
  private static boolean[] convertOBoolToBool(OBool[] input) {
    boolean[] output = new boolean[input.length];
    for(int i = 0; i< input.length; i++) {
      output[i] = input[i].getValue();
    }
    return output;
  } */
}
