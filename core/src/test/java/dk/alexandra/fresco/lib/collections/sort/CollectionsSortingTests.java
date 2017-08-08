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
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.math.Util;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.hamcrest.core.Is;
import org.junit.Assert;

/**
 * Test class for the DEASolver.
 * Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem
 * as inputs (i.e. the number of input and output variables, the number
 * of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.    
 *
 */
public class CollectionsSortingTests {
	
	 public static class TestOddEvenMerge extends TestThreadFactory {
	    
	    public TestOddEvenMerge() {
	    }

	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      return new TestThread() {
	        @Override
	        public void test() throws Exception {
	          
	          boolean[] left11 = ByteArithmetic.toBoolean("ff");
	          boolean[] left12 = ByteArithmetic.toBoolean("ee");
	          boolean[] left21 = ByteArithmetic.toBoolean("bb");
	          boolean[] left22 = ByteArithmetic.toBoolean("ba");
	          boolean[] left31 = ByteArithmetic.toBoolean("ab");
            boolean[] left32 = ByteArithmetic.toBoolean("aa");
	          boolean[] right11 = ByteArithmetic.toBoolean("49");         
	          boolean[] right12 = ByteArithmetic.toBoolean("00");
	          
	          OBool[][] results = new OBool[16][];
	          
	          TestApplication app = new TestApplication() {

	            private static final long serialVersionUID = 4338818809103728010L;

	            @Override
	            public ProtocolProducer prepareApplication(
	                BuilderFactory factoryProducer) {
	              ProtocolFactory producer = factoryProducer.getProtocolFactory();
	               AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
	                BasicLogicBuilder builder = new BasicLogicBuilder(prov);
	                SequentialProtocolProducer sseq = new SequentialProtocolProducer();
	                SBool[] l11 = builder.knownSBool(left11);
	                SBool[] l12 = builder.knownSBool(left12);
	                SBool[] l21 = builder.knownSBool(left21);
	                SBool[] l22 = builder.knownSBool(left22);
	                SBool[] l31 = builder.knownSBool(left31);
                  SBool[] l32 = builder.knownSBool(left32);
	                
	                SBool[] r11 = builder.knownSBool(right11);
	                SBool[] r12 = builder.knownSBool(right12);

	                SBool[] s11 = prov.getSBools(8);
	                SBool[] s12 = prov.getSBools(8);
	                SBool[] s21 = prov.getSBools(8);
	                SBool[] s22 = prov.getSBools(8);
	                SBool[] s31 = prov.getSBools(8);
	                SBool[] s32 = prov.getSBools(8);
	                SBool[] s41 = prov.getSBools(8);
                  SBool[] s42 = prov.getSBools(8);

                  SBool[] sr11 = prov.getSBools(8);
                  SBool[] sr12 = prov.getSBools(8);
                  SBool[] sr21 = prov.getSBools(8);
                  SBool[] sr22 = prov.getSBools(8);
                  SBool[] sr31 = prov.getSBools(8);
                  SBool[] sr32 = prov.getSBools(8);
                  SBool[] sr41 = prov.getSBools(8);
                  SBool[] sr42 = prov.getSBools(8);
                  
	              sseq.append(builder.getProtocol());
	              
	              List<Pair<SBool[], SBool[]>> left = new ArrayList<Pair<SBool[], SBool[]>>();
	              List<Pair<SBool[], SBool[]>> right = new ArrayList<Pair<SBool[], SBool[]>>();
	              List<Pair<SBool[], SBool[]>> sorted = new ArrayList<Pair<SBool[], SBool[]>>();
	              List<Pair<SBool[], SBool[]>> sorted2 = new ArrayList<Pair<SBool[], SBool[]>>();

	              left.add(new Pair<SBool[], SBool[]>(l11, l12));
	              left.add(new Pair<SBool[], SBool[]>(l21, l22));
	              left.add(new Pair<SBool[], SBool[]>(l31, l32));
	              right.add(new Pair<SBool[], SBool[]>(r11, r12));
	              sorted.add(new Pair<SBool[], SBool[]>(s11, s12));
	              sorted.add(new Pair<SBool[], SBool[]>(s21, s22));
	              sorted.add(new Pair<SBool[], SBool[]>(s31, s32));
	              sorted.add(new Pair<SBool[], SBool[]>(s41, s42));
                sorted2.add(new Pair<SBool[], SBool[]>(sr11, sr12));
                sorted2.add(new Pair<SBool[], SBool[]>(sr21, sr22));
                sorted2.add(new Pair<SBool[], SBool[]>(sr31, sr32));
                sorted2.add(new Pair<SBool[], SBool[]>(sr41, sr42));
	              
	              OddEvenMergeProtocolImpl mergeProtocol = 
	                  new OddEvenMergeProtocolImpl(left, right, sorted, prov);
	             
	              OddEvenMergeProtocolImpl mergeProtocolReversed = 
                    new OddEvenMergeProtocolImpl(right, left, sorted2, prov);
               
	              sseq.append(mergeProtocol);
	              sseq.append(mergeProtocolReversed);
	              
	              results[0] = builder.output(sorted.get(0).getFirst());
	              results[1] = builder.output(sorted.get(0).getSecond());
	              results[2] = builder.output(sorted.get(1).getFirst());
	              results[3] = builder.output(sorted.get(1).getSecond());
	              results[4] = builder.output(sorted.get(2).getFirst());
	              results[5] = builder.output(sorted.get(2).getSecond());
	              results[6] = builder.output(sorted.get(3).getFirst());
                results[7] = builder.output(sorted.get(3).getSecond());
                results[0+8] = builder.output(sorted2.get(0).getFirst());
                results[1+8] = builder.output(sorted2.get(0).getSecond());
                results[2+8] = builder.output(sorted2.get(1).getFirst());
                results[3+8] = builder.output(sorted2.get(1).getSecond());
                results[4+8] = builder.output(sorted2.get(2).getFirst());
                results[5+8] = builder.output(sorted2.get(2).getSecond());
                results[6+8] = builder.output(sorted2.get(3).getFirst());
                results[7+8] = builder.output(sorted2.get(3).getSecond());
	              sseq.append(builder.getProtocol());
	              
	              return sseq;
	            }
	          };
	          secureComputationEngine
	              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

	          Assert.assertArrayEquals(left11, convertOBoolToBool(results[0]));
	          Assert.assertArrayEquals(left12, convertOBoolToBool(results[1]));
	          Assert.assertArrayEquals(left21, convertOBoolToBool(results[2]));
	          Assert.assertArrayEquals(left22, convertOBoolToBool(results[3]));
	          Assert.assertArrayEquals(left31, convertOBoolToBool(results[4]));
	          Assert.assertArrayEquals(left32, convertOBoolToBool(results[5]));
	          Assert.assertArrayEquals(right11, convertOBoolToBool(results[6]));
	          Assert.assertArrayEquals(right12, convertOBoolToBool(results[7]));

            Assert.assertArrayEquals(left11, convertOBoolToBool(results[0+8]));
	          Assert.assertArrayEquals(left12, convertOBoolToBool(results[1+8]));
	          Assert.assertArrayEquals(left21, convertOBoolToBool(results[2+8]));
	          Assert.assertArrayEquals(left22, convertOBoolToBool(results[3+8]));
	          Assert.assertArrayEquals(left31, convertOBoolToBool(results[4+8]));
	          Assert.assertArrayEquals(left32, convertOBoolToBool(results[5+8]));
	          Assert.assertArrayEquals(right11, convertOBoolToBool(results[6+8]));
	          Assert.assertArrayEquals(right12, convertOBoolToBool(results[7+8]));
	          
	        }
	      };
	    }
	  }

	 
   public static class TestOddEvenMergeRec extends TestThreadFactory {
     
     public TestOddEvenMergeRec() {
     }

     @Override
     public TestThread next(TestThreadConfiguration conf) {
       return new TestThread() {
         @Override
         public void test() throws Exception {
           
           boolean[] left11 = ByteArithmetic.toBoolean("ff");
           boolean[] left12 = ByteArithmetic.toBoolean("ee");
           boolean[] left21 = ByteArithmetic.toBoolean("bb");
           boolean[] left22 = ByteArithmetic.toBoolean("ba");
           boolean[] left31 = ByteArithmetic.toBoolean("ab");
           boolean[] left32 = ByteArithmetic.toBoolean("aa");
           boolean[] right11 = ByteArithmetic.toBoolean("49");         
           boolean[] right12 = ByteArithmetic.toBoolean("00");
           
           OBool[][] results = new OBool[16][];
           
           TestApplication app = new TestApplication() {

             private static final long serialVersionUID = 4338818809103728010L;

             @Override
             public ProtocolProducer prepareApplication(
                 BuilderFactory factoryProducer) {
               ProtocolFactory producer = factoryProducer.getProtocolFactory();
                AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
                 BasicLogicBuilder builder = new BasicLogicBuilder(prov);
                 SequentialProtocolProducer sseq = new SequentialProtocolProducer();
                 SBool[] l11 = builder.knownSBool(left11);
                 SBool[] l12 = builder.knownSBool(left12);
                 SBool[] l21 = builder.knownSBool(left21);
                 SBool[] l22 = builder.knownSBool(left22);
                 SBool[] l31 = builder.knownSBool(left31);
                 SBool[] l32 = builder.knownSBool(left32);
                 
                 SBool[] r11 = builder.knownSBool(right11);
                 SBool[] r12 = builder.knownSBool(right12);

                 SBool[] s11 = prov.getSBools(8);
                 SBool[] s12 = prov.getSBools(8);
                 SBool[] s21 = prov.getSBools(8);
                 SBool[] s22 = prov.getSBools(8);
                 SBool[] s31 = prov.getSBools(8);
                 SBool[] s32 = prov.getSBools(8);
                 SBool[] s41 = prov.getSBools(8);
                 SBool[] s42 = prov.getSBools(8);

                 SBool[] sr11 = prov.getSBools(8);
                 SBool[] sr12 = prov.getSBools(8);
                 SBool[] sr21 = prov.getSBools(8);
                 SBool[] sr22 = prov.getSBools(8);
                 SBool[] sr31 = prov.getSBools(8);
                 SBool[] sr32 = prov.getSBools(8);
                 SBool[] sr41 = prov.getSBools(8);
                 SBool[] sr42 = prov.getSBools(8);
                 
               sseq.append(builder.getProtocol());
               
               List<Pair<SBool[], SBool[]>> left = new ArrayList<Pair<SBool[], SBool[]>>();
               List<Pair<SBool[], SBool[]>> right = new ArrayList<Pair<SBool[], SBool[]>>();
               List<Pair<SBool[], SBool[]>> sorted = new ArrayList<Pair<SBool[], SBool[]>>();
               List<Pair<SBool[], SBool[]>> sorted2 = new ArrayList<Pair<SBool[], SBool[]>>();

               left.add(new Pair<SBool[], SBool[]>(l11, l12));
               left.add(new Pair<SBool[], SBool[]>(l21, l22));
               left.add(new Pair<SBool[], SBool[]>(l31, l32));
               right.add(new Pair<SBool[], SBool[]>(r11, r12));
               sorted.add(new Pair<SBool[], SBool[]>(s11, s12));
               sorted.add(new Pair<SBool[], SBool[]>(s21, s22));
               sorted.add(new Pair<SBool[], SBool[]>(s31, s32));
               sorted.add(new Pair<SBool[], SBool[]>(s41, s42));
               sorted2.add(new Pair<SBool[], SBool[]>(sr11, sr12));
               sorted2.add(new Pair<SBool[], SBool[]>(sr21, sr22));
               sorted2.add(new Pair<SBool[], SBool[]>(sr31, sr32));
               sorted2.add(new Pair<SBool[], SBool[]>(sr41, sr42));
               
               
               
               OddEvenMergeProtocol mergeProtocol = 
                   prov.getOddEvenMergeProtocol(left, right, sorted);
               
               OddEvenMergeProtocol mergeProtocolReversed = 
                   prov.getOddEvenMergeProtocol(left, right, sorted2); 
              
               sseq.append(mergeProtocol);
               sseq.append(mergeProtocolReversed);
               
               results[0] = builder.output(sorted.get(0).getFirst());
               results[1] = builder.output(sorted.get(0).getSecond());
               results[2] = builder.output(sorted.get(1).getFirst());
               results[3] = builder.output(sorted.get(1).getSecond());
               results[4] = builder.output(sorted.get(2).getFirst());
               results[5] = builder.output(sorted.get(2).getSecond());
               results[6] = builder.output(sorted.get(3).getFirst());
               results[7] = builder.output(sorted.get(3).getSecond());
               results[0+8] = builder.output(sorted2.get(0).getFirst());
               results[1+8] = builder.output(sorted2.get(0).getSecond());
               results[2+8] = builder.output(sorted2.get(1).getFirst());
               results[3+8] = builder.output(sorted2.get(1).getSecond());
               results[4+8] = builder.output(sorted2.get(2).getFirst());
               results[5+8] = builder.output(sorted2.get(2).getSecond());
               results[6+8] = builder.output(sorted2.get(3).getFirst());
               results[7+8] = builder.output(sorted2.get(3).getSecond());
               sseq.append(builder.getProtocol());
               
               return sseq;
             }
           };
           secureComputationEngine
               .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

           Assert.assertArrayEquals(left11, convertOBoolToBool(results[0]));
           Assert.assertArrayEquals(left12, convertOBoolToBool(results[1]));
           Assert.assertArrayEquals(left21, convertOBoolToBool(results[2]));
           Assert.assertArrayEquals(left22, convertOBoolToBool(results[3]));
           Assert.assertArrayEquals(left31, convertOBoolToBool(results[4]));
           Assert.assertArrayEquals(left32, convertOBoolToBool(results[5]));
           Assert.assertArrayEquals(right11, convertOBoolToBool(results[6]));
           Assert.assertArrayEquals(right12, convertOBoolToBool(results[7]));

           Assert.assertArrayEquals(left11, convertOBoolToBool(results[0+8]));
           Assert.assertArrayEquals(left12, convertOBoolToBool(results[1+8]));
           Assert.assertArrayEquals(left21, convertOBoolToBool(results[2+8]));
           Assert.assertArrayEquals(left22, convertOBoolToBool(results[3+8]));
           Assert.assertArrayEquals(left31, convertOBoolToBool(results[4+8]));
           Assert.assertArrayEquals(left32, convertOBoolToBool(results[5+8]));
           Assert.assertArrayEquals(right11, convertOBoolToBool(results[6+8]));
           Assert.assertArrayEquals(right12, convertOBoolToBool(results[7+8]));
           
         }
       };
     }
   }

   public static class TestOddEvenMergeRecLarge extends TestThreadFactory {
     
     public TestOddEvenMergeRecLarge() {
     }

     @Override
     public TestThread next(TestThreadConfiguration conf) {
       return new TestThread() {
         @Override
         public void test() throws Exception {
           
           boolean[][][] left =  new boolean[40][2][];
           boolean[][][] right = new boolean[60][2][];
           
           
           OBool[][] results = new OBool[200][];
           
           TestApplication app = new TestApplication() {

             private static final long serialVersionUID = 4338818809103728010L;

             @Override
             public ProtocolProducer prepareApplication(
                 BuilderFactory factoryProducer) {
               ProtocolFactory producer = factoryProducer.getProtocolFactory();
                AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
                 BasicLogicBuilder builder = new BasicLogicBuilder(prov);
                 SequentialProtocolProducer sseq = new SequentialProtocolProducer();
                 SBool[][][] leftSecret = new SBool[40][2][];
                 SBool[][][] rightSecret = new SBool[60][2][];
               

                 List<Pair<SBool[], SBool[]>> leftList = new ArrayList<Pair<SBool[], SBool[]>>();
                 List<Pair<SBool[], SBool[]>> rightList = new ArrayList<Pair<SBool[], SBool[]>>();
                 List<Pair<SBool[], SBool[]>> sorted = new ArrayList<Pair<SBool[], SBool[]>>();

                 Random random = new Random(2);
                 for(int i = 0; i< left.length; i++) {
                   left[i][0] = randomBoolArray(8, random);
                   left[i][1] = randomBoolArray(8, random);
                   leftSecret[i][0] = builder.knownSBool(left[i][0]);
                   leftSecret[i][1] = builder.knownSBool(left[i][1]);
                   leftList.add(new Pair(leftSecret[i][0], leftSecret[i][1]));
                   sorted.add(new Pair(prov.getSBools(8), prov.getSBools(8)));
                 }
                 for(int i = 0; i< right.length; i++) {
                   right[i][0] = randomBoolArray(8, random);
                   right[i][1] = randomBoolArray(8, random);
                   rightSecret[i][0] = builder.knownSBool(right[i][0]);
                   rightSecret[i][1] = builder.knownSBool(right[i][1]);
                   rightList.add(new Pair(rightSecret[i][0], rightSecret[i][1]));
                   sorted.add(new Pair(prov.getSBools(8), prov.getSBools(8)));
                 }
                 

               sseq.append(builder.getProtocol());
               
               OddEvenMergeProtocol mergeProtocol = 
                   prov.getOddEvenMergeProtocol(rightList, leftList, sorted);
               
 
               
               sseq.append(mergeProtocol);
               for(int i = 0; i< left.length; i++) {
                 results[i*2] = builder.output(sorted.get(i).getFirst());
                 results[(i*2)+1] = builder.output(sorted.get(i).getSecond());
               }
               for(int i = 0; i< right.length; i++) {
                 results[(i*2)+(left.length*2)] = builder.output(sorted.get(i+left.length).getFirst());
                 results[(i*2)+1+(left.length*2)] = builder.output(sorted.get(i+left.length).getSecond());
               }
               sseq.append(builder.getProtocol());
               
               return sseq;
             }
           };
           secureComputationEngine
               .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
           
           int prev = valueOfBools(convertOBoolToBool(results[0]));
           for(int i = 1; i < results.length; i++) {
             int current = valueOfBools(convertOBoolToBool(results[i]));
             // TODO Find out what OddEvenMergeRec is supposed to return and verify it is correct!
             //Assert.assertTrue(current >= prev);
             prev = current;
           }
                      
         }
       };
     }
   }

   
   public static class TestKeyedCompareAndSwap extends TestThreadFactory {
     
     public TestKeyedCompareAndSwap() {
     }

     @Override
     public TestThread next(TestThreadConfiguration conf) {
       return new TestThread() {
         @Override
         public void test() throws Exception {
           
           boolean[] leftKey = ByteArithmetic.toBoolean("49");         
           boolean[] leftValue = ByteArithmetic.toBoolean("00");
           boolean[] rightKey = ByteArithmetic.toBoolean("ff");
           boolean[] rightValue = ByteArithmetic.toBoolean("ee");
           
           OBool[][] results = new OBool[4][];
           
           TestApplication app = new TestApplication() {

             private static final long serialVersionUID = 4338818809103728010L;

             @Override
             public ProtocolProducer prepareApplication(
                 BuilderFactory factoryProducer) {
               ProtocolFactory producer = factoryProducer.getProtocolFactory();
                AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
                 BasicLogicBuilder builder = new BasicLogicBuilder(prov);
                 SequentialProtocolProducer sseq = new SequentialProtocolProducer();
                 SBool[] lk = builder.knownSBool(leftKey);
                 SBool[] lv = builder.knownSBool(leftValue);
                 
                 SBool[] rk = builder.knownSBool(rightKey);
                 SBool[] rv = builder.knownSBool(rightValue);

                                  
                 sseq.append(builder.getProtocol());
               
               KeyedCompareAndSwapProtocolImpl keyedCompAndSwap = 
                   new KeyedCompareAndSwapProtocolImpl(lk, lv, rk, rv, prov);
               
               sseq.append(keyedCompAndSwap);
               
               results[0] = builder.output(lk);
               results[1] = builder.output(lv);
               results[2] = builder.output(rk);
               results[3] = builder.output(rv);
               sseq.append(builder.getProtocol());
               
               return sseq;
             }
           };
           secureComputationEngine
               .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

           Assert.assertArrayEquals(rightKey, convertOBoolToBool(results[0]));
           Assert.assertArrayEquals(rightValue, convertOBoolToBool(results[1]));
           Assert.assertArrayEquals(leftKey, convertOBoolToBool(results[2]));
           Assert.assertArrayEquals(leftValue, convertOBoolToBool(results[3]));
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
  }
}
