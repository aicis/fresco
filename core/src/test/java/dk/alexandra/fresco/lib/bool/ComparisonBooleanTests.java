/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.bool;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.value.SBool;
<<<<<<< HEAD
import dk.alexandra.fresco.lib.compare.bool.ParBinaryGreaterThanProtocolImpl;
import dk.alexandra.fresco.lib.compare.bool.eq.BinaryEqualityProtocolImpl;
import dk.alexandra.fresco.lib.field.bool.generic.AbstractBinaryFactory;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;

import org.apache.commons.math3.genetics.BinaryChromosome;
=======
>>>>>>> origin/feature/binary-builder
import org.junit.Assert;

public class ComparisonBooleanTests {

  /**
   * Tests if the number 01010 > 01110 - then it reverses that.
   *
   * @author Kasper Damgaard
   */
  public static class TestGreaterThan extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
<<<<<<< HEAD
          boolean[] comp1 = new boolean[]{false, true, false, true, false};
          boolean[] comp2 = new boolean[]{false, true, true, true, false};

          TestBoolApplication app = new TestBoolApplication() {
            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();

              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);

              SBool[] in1 = builder.knownSBool(comp1);
              SBool[] in2 = builder.knownSBool(comp2);


              SBool compRes1 = builder.greaterThan(in1, in2);
              SBool compRes2 = builder.greaterThan(in2, in1);

              this.outputs = new OBool[]{builder.output(compRes1), builder.output(compRes2)};
              return builder.getProtocol();
=======
          boolean[] comp1 = new boolean[] {false, true, false, true, false};
          boolean[] comp2 = new boolean[] {false, true, true, true, false};

          Application<Boolean[], SequentialBinaryBuilder> app =
              new Application<Boolean[], SequentialBinaryBuilder>() {

            @Override
            public Computation<Boolean[]> prepareApplication(SequentialBinaryBuilder producer) {
              return producer.seq(seq -> {
                Computation<SBool[]> in1 = seq.binary().known(comp1);
                Computation<SBool[]> in2 = seq.binary().known(comp2);
                Computation<SBool> res1 = seq.comparison().greaterThan(in1.out(), in2.out());
                Computation<SBool> res2 = seq.comparison().greaterThan(in2.out(), in1.out());
                Computation<Boolean> open1 = seq.binary().open(res1);
                Computation<Boolean> open2 = seq.binary().open(res2);
                return () -> new Boolean[] {open1.out(), open2.out()};
              });
>>>>>>> origin/feature/binary-builder
            }
          };

          boolean[] res = (boolean[]) secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(false, res[0]);
          Assert.assertEquals(true, res[1]);
        }
      };
    }
  }
	
	 public static class TestGreaterThanUnequalLength extends TestThreadFactory {
	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      return new TestThread() {
	        @Override
	        public void test() throws Exception {
	          boolean[] comp1 = new boolean[] {false, true, false, true, false};
	          boolean[] comp2 = new boolean[] {false, true, true};
	          
	          TestBoolApplication app = new TestBoolApplication() {

	            private static final long serialVersionUID = 4338818809103728010L;

	            @Override
	            public ProtocolProducer prepareApplication(
	                BuilderFactory factory) {
	              ProtocolFactory provider = factory.getProtocolFactory();
	              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
	              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
	              
	              SBool[] in1 = builder.knownSBool(comp1);
	              SBool[] in2 = builder.knownSBool(comp2);
	              
	              SBool compRes1 = builder.greaterThan(in1, in2);
	              SBool compRes2 = builder.greaterThan(in2, in1);

	              this.outputs = new OBool[]{builder.output(compRes1), builder.output(compRes2)};
	              return builder.getProtocol();
	            }
	          };

	          secureComputationEngine
	              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

	        }
	      };
	    }
	  }

	 
	 //TODO The tested class is commented as experimental and is not referenced in 
	 // any builders
	  public static class TestGreaterThanPar extends TestThreadFactory {
	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      return new TestThread() {
	        @Override
	        public void test() throws Exception {
	          boolean[] comp1 = new boolean[] {false, true, false, true, false};
	          boolean[] comp2 = new boolean[] {false, true, true, true, false};
	          
	          TestBoolApplication app = new TestBoolApplication() {

	            private static final long serialVersionUID = 4338818809103728010L;

	            @Override
	            public ProtocolProducer prepareApplication(
	               BuilderFactory factory) {
	              ProtocolFactory provider = factory.getProtocolFactory();
	              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
	              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
	              
	              SBool[] in1 = builder.knownSBool(comp1);
	              SBool[] in2 = builder.knownSBool(comp2);
	              
	              SBool compRes1 = prov.getSBool();
	              SBool compRes2 = prov.getSBool();
                
	              ParBinaryGreaterThanProtocolImpl parBinaryGreaterThanProtocolImpl = new
	                  ParBinaryGreaterThanProtocolImpl(in1, in2, compRes1, prov);
	              
	              ParBinaryGreaterThanProtocolImpl parBinaryGreaterThanProtocolImpl2 = new
                    ParBinaryGreaterThanProtocolImpl(in2, in1, compRes2, prov);
                SequentialProtocolProducer sseq = new SequentialProtocolProducer();
                sseq.append(parBinaryGreaterThanProtocolImpl);
                sseq.append(parBinaryGreaterThanProtocolImpl2);
                
                
                this.outputs = new OBool[]{builder.output(compRes1), builder.output(compRes2)};
                sseq.append(builder.getProtocol());
	              return sseq;
	            }
	          };

	          secureComputationEngine
	              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

	            Assert.assertEquals(false,
	                app.getOutputs()[0].getValue());
	            Assert.assertEquals(true,
	                app.getOutputs()[1].getValue());
	        }
	      };
	    }
	  }

	  
	  public static class TestBinaryEqual extends TestThreadFactory {
	    @Override
	    public TestThread next(TestThreadConfiguration conf) {
	      return new TestThread() {
	        @Override
	        public void test() throws Exception {
	          boolean[] comp1 = new boolean[] {false, true, false, true, false};
	          boolean[] comp2 = new boolean[] {false, true, true, true, false};
	          boolean[] comp3 = new boolean[] {false, true, true, true, false};
            
	          
	          TestBoolApplication app = new TestBoolApplication() {

	            private static final long serialVersionUID = 4338818809103728010L;

	            @Override
	            public ProtocolProducer prepareApplication(
	               BuilderFactory factory) {
	              ProtocolFactory provider = factory.getProtocolFactory();
	              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
	              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
	              
	              SBool[] in1 = builder.knownSBool(comp1);
	              SBool[] in2 = builder.knownSBool(comp2);
	              SBool[] in3 = builder.knownSBool(comp3);
                
	              SBool compRes1 = builder.equality(in1, in2);
	              SBool compRes2 = builder.equality(in2, in3);

	              this.outputs = new OBool[]{builder.output(compRes1), builder.output(compRes2)};
	              return builder.getProtocol();
	            }
	          };

	          secureComputationEngine
	              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

	            Assert.assertEquals(false,
	                app.getOutputs()[0].getValue());
	            Assert.assertEquals(true,
	                app.getOutputs()[1].getValue());
	        }
	      };
	    }
	  }

    public static class TestBinaryEqualBasicProtocol extends TestThreadFactory {
      @Override
      public TestThread next(TestThreadConfiguration conf) {
        return new TestThread() {
          @Override
          public void test() throws Exception {
            boolean[] comp1 = new boolean[] {false, true, false, true, false};
            boolean[] comp2 = new boolean[] {false, true, true, true, false};
            boolean[] comp3 = new boolean[] {false, true, true, true, false};
            
            
            TestBoolApplication app = new TestBoolApplication() {

              private static final long serialVersionUID = 4338818809103728010L;

              @Override
              public ProtocolProducer prepareApplication(
                  BuilderFactory factory) {
                ProtocolFactory provider = factory.getProtocolFactory();
                AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
                BasicLogicBuilder builder = new BasicLogicBuilder(prov);
                
                SBool[] in1 = builder.knownSBool(comp1);
                SBool[] in2 = builder.knownSBool(comp2);
                SBool[] in3 = builder.knownSBool(comp3);
                
                SBool compRes1 = builder.equality(in1, in2);
                SBool compRes2 = builder.equality(in2, in3);

                
                BinaryEqualityProtocolImpl eq1 = new
                    BinaryEqualityProtocolImpl(in1, in2, compRes1, prov);
                
                BinaryEqualityProtocolImpl eq2 = new
                    BinaryEqualityProtocolImpl(in2, in3, compRes1, prov);
                
                SequentialProtocolProducer sseq = new SequentialProtocolProducer();
                sseq.append(eq1);
                sseq.append(eq2);
                
                this.outputs = new OBool[]{builder.output(compRes1), builder.output(compRes2)};
                sseq.append(builder.getProtocol());
                return sseq;
              }
            };

            secureComputationEngine
                .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

              Assert.assertEquals(false,
                  app.getOutputs()[0].getValue());
              Assert.assertEquals(true,
                  app.getOutputs()[1].getValue());
          }
        };
      }
    }
}
