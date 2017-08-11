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

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.core.Is;
import org.junit.Assert;

public class BasicBooleanTests {

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestInput() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        
        private List<Boolean> bools = Arrays.asList(new Boolean[]{true, false});
        
        @Override
        public void test() throws Exception {
          
          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
                return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {
                  List<Computation<SBool>> closed =
                      bools.stream().map(builder.binary()::known).collect(Collectors.toList());
                  this.outputs = closed.stream().map(builder.binary()::open).collect(Collectors.toList());
                }).build();
            }
          };
       
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertThat(app.getOutputs()[0].booleanValue(), Is.is(bools.get(0)));
          Assert.assertThat(app.getOutputs()[1].booleanValue(), Is.is(bools.get(1)));              

        }
      };
    }
  }


  public static class TestXOR<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestXOR() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        
        private List<Boolean> bools = Arrays.asList(new Boolean[]
            {false, false,
             false, true, 
             true, false,
             true, true});
        
        @Override
        public void test() throws Exception {
          
          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
                return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                  List<Computation<SBool>> closed =
                      bools.stream().map(builder.binary()::known).collect(Collectors.toList());

                  List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                  results.add(builder.binary().xor(closed.get(0), closed.get(1))); //ff
                  results.add(builder.binary().xor(closed.get(2), closed.get(3))); //ft
                  results.add(builder.binary().xor(closed.get(4), closed.get(5))); //tf
                  results.add(builder.binary().xor(closed.get(6), closed.get(6))); //tt

                  this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
                }).build();
            }
          };
       
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(false, app.getOutputs()[0].booleanValue());
          Assert.assertEquals(true, app.getOutputs()[1].booleanValue());
          Assert.assertEquals(true, app.getOutputs()[2].booleanValue());
          Assert.assertEquals(false, app.getOutputs()[3].booleanValue());

        }
      };
    }
  }


  public static class TestAND<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestAND() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        
        private List<Boolean> bools = Arrays.asList(new Boolean[]
            {false, false,
             false, true, 
             true, false,
             true, true});
        
        @Override
        public void test() throws Exception {
          
          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
                return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                  List<Computation<SBool>> closed =
                      bools.stream().map(builder.binary()::known).collect(Collectors.toList());

                  List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                  results.add(builder.binary().and(closed.get(0), closed.get(1))); //ff
                  results.add(builder.binary().and(closed.get(2), closed.get(3))); //ft
                  results.add(builder.binary().and(closed.get(4), closed.get(5))); //tf
                  results.add(builder.binary().and(closed.get(6), closed.get(6))); //tt

                  this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
                }).build();
            }
          };
       
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(false, app.getOutputs()[0].booleanValue());
          Assert.assertEquals(false, app.getOutputs()[1].booleanValue());
          Assert.assertEquals(false, app.getOutputs()[2].booleanValue());
          Assert.assertEquals(true, app.getOutputs()[3].booleanValue());

        }
      };
    }
  }
  
  public static class TestNOT<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestNOT() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        
        private List<Boolean> bools = Arrays.asList(new Boolean[]{false, true});
        
        @Override
        public void test() throws Exception {
          
          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
                return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                  List<Computation<SBool>> closed =
                      bools.stream().map(builder.binary()::known).collect(Collectors.toList());

                  List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                  results.add(builder.binary().not(closed.get(0))); //f
                  results.add(builder.binary().not(closed.get(1))); //t
                  
                  this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
                }).build();
            }
          };
       
          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          
          Assert.assertEquals(true, app.getOutputs()[0].booleanValue());
          Assert.assertEquals(false, app.getOutputs()[1].booleanValue());
        }
      };
    }
  }

  public static class TestCOPY<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

  public TestCOPY() {
  }

  @Override
  public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
      TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
    return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
      
      private List<Boolean> bools = Arrays.asList(new Boolean[]{false, true});
      
      @Override
      public void test() throws Exception {
        
        TestBoolApplication app = new TestBoolApplication(){

          @Override
          public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                List<Computation<SBool>> closed =
                    bools.stream().map(builder.binary()::known).collect(Collectors.toList());

                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                results.add(builder.binary().copy(closed.get(0))); //f
                results.add(builder.binary().copy(closed.get(1))); //t
                
                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
          }
        };
     
        secureComputationEngine.runApplication(app,
            ResourcePoolCreator.createResourcePool(conf.sceConf));
        
        Assert.assertEquals(false, app.getOutputs()[0].booleanValue());
        Assert.assertEquals(true, app.getOutputs()[1].booleanValue());
      }
    };
  }
}

  

  /**
   * Tests both input, xor, not, and and output. Computes all variants of: NOT((i1 XOR i2) AND i1)
   */

  public static class TestBasicProtocols extends TestThreadFactory {

    private boolean assertAsExpected;

    public TestBasicProtocols(boolean assertAsExpected) {
      this.assertAsExpected = assertAsExpected;
    }

    @Override
    public TestThread<ResourcePool, SequentialBinaryBuilder> next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
     /*     TestBoolApplication app = new TestBoolApplication() {


            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
              SBool inp100 = builder.knownSBool(false);
              SBool inp200 = builder.knownSBool(false);
              SBool xor00 = builder.xor(inp100, inp200);
              SBool and00 = builder.and(inp100, xor00);
              SBool not00 = builder.not(and00);

              SBool inp110 = builder.knownSBool(true);
              SBool inp210 = builder.knownSBool(false);
              SBool xor10 = builder.xor(inp110, inp210);
              SBool and10 = builder.and(inp110, xor10);
              SBool not10 = builder.not(and10);

              SBool inp101 = builder.knownSBool(false);
              SBool inp201 = builder.knownSBool(true);
              SBool xor01 = builder.xor(inp101, inp201);
              SBool and01 = builder.and(inp101, xor01);
              SBool not01 = builder.not(and01);

              SBool inp111 = builder.knownSBool(true);
              SBool inp211 = builder.knownSBool(true);
              SBool xor11 = builder.xor(inp111, inp211);
              SBool and11 = builder.and(inp111, xor11);
              SBool not11 = builder.not(and11);

              // maybe remove again - test for having not before and
              SBool ainp111 = builder.knownSBool(true);
              SBool ainp211 = builder.knownSBool(true);
              SBool anot11 = builder.not(ainp211);
              SBool aand11 = builder.and(ainp111, anot11);

              this.outputs = builder.output(not00, not10, not01, not11, aand11);
              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          if (!assertAsExpected) {
            return;
          }

          Assert.assertEquals(true, app.getOutputs()[0].getValue());
          Assert.assertEquals(false, app.getOutputs()[1].getValue());
          Assert.assertEquals(true, app.getOutputs()[2].getValue());
          Assert.assertEquals(true, app.getOutputs()[3].getValue());
          Assert.assertEquals(false, app.getOutputs()[4].getValue());
   */     }
      };
    }
  }
}
