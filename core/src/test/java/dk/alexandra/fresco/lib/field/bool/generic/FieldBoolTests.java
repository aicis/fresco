/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.field.bool.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;

public class FieldBoolTests {

  public static class TestXNorFromXorAndNot<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestXNorFromXorAndNot() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                Computation<SBool> inp100 = builder.binary().known(false);
                Computation<SBool> inp200 = builder.binary().known(false);
                results.add(prov.xnor(inp100, inp200));

                Computation<SBool> inp110 = builder.binary().known(true);
                Computation<SBool> inp210 = builder.binary().known(false);
                results.add(prov.xnor(inp110, inp210));

                Computation<SBool> inp101 = builder.binary().known(false);
                Computation<SBool> inp201 = builder.binary().known(true);
                results.add(prov.xnor(inp101, inp201));

                Computation<SBool> inp111 = builder.binary().known(true);
                Computation<SBool> inp211 = builder.binary().known(true);
                results.add(prov.xnor(inp111, inp211));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(true,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[1].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[2].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].booleanValue());
        }
      };
    }
  }

  public static class TestXNorFromOpen<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestXNorFromOpen() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();

                Computation<SBool> inp100 = builder.binary().known(false);
                results.add(prov.xnor(inp100, false));

                Computation<SBool> inp110 = builder.binary().known(true);
                results.add(prov.xnor(inp110, false));

                Computation<SBool> inp101 = builder.binary().known(false);
                results.add(prov.xnor(inp101, true));

                Computation<SBool> inp111 = builder.binary().known(true);
                results.add(prov.xnor(inp111, true));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(true,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[1].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[2].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].booleanValue());
        }
      };
    }
  }

  
  public static class TestOrFromXorAnd<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOrFromXorAnd() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                Computation<SBool> inp100 = builder.binary().known(false);
                Computation<SBool> inp200 = builder.binary().known(false);
                results.add(prov.or(inp100, inp200));

                Computation<SBool> inp110 = builder.binary().known(true);
                Computation<SBool> inp210 = builder.binary().known(false);
                results.add(prov.or(inp110, inp210));

                Computation<SBool> inp101 = builder.binary().known(false);
                Computation<SBool> inp201 = builder.binary().known(true);
                results.add(prov.or(inp101, inp201));

                Computation<SBool> inp111 = builder.binary().known(true);
                Computation<SBool> inp211 = builder.binary().known(true);
                results.add(prov.or(inp111, inp211));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(false,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[1].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[2].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].booleanValue());
        }
      };
    }
  }

  public static class TestOrFromCopyConst<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOrFromCopyConst() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                Computation<SBool> inp100 = builder.binary().known(false);
                results.add(prov.or(inp100, false));

                Computation<SBool> inp110 = builder.binary().known(true);
                results.add(prov.or(inp110, false));

                Computation<SBool> inp101 = builder.binary().known(false);
                results.add(prov.or(inp101, true));

                Computation<SBool> inp111 = builder.binary().known(true);
                results.add(prov.or(inp111, true));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(false,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[1].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[2].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].booleanValue());
        }
      };
    }
  }


  public static class TestNandFromAndAndNot<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestNandFromAndAndNot() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                Computation<SBool> inp100 = builder.binary().known(false);
                Computation<SBool> inp200 = builder.binary().known(false);
                results.add(prov.nand(inp100, inp200));

                Computation<SBool> inp110 = builder.binary().known(true);
                Computation<SBool> inp210 = builder.binary().known(false);
                results.add(prov.nand(inp110, inp210));

                Computation<SBool> inp101 = builder.binary().known(false);
                Computation<SBool> inp201 = builder.binary().known(true);
                results.add(prov.nand(inp101, inp201));

                Computation<SBool> inp111 = builder.binary().known(true);
                Computation<SBool> inp211 = builder.binary().known(true);
                results.add(prov.nand(inp111, inp211));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(true,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[1].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[2].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[3].booleanValue());
        }
      };
    }
  }

  public static class TestNandFromOpen<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestNandFromOpen() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();
                Computation<SBool> inp100 = builder.binary().known(false);
                results.add(prov.nand(inp100, false));

                Computation<SBool> inp110 = builder.binary().known(true);
                results.add(prov.nand(inp110, false));

                Computation<SBool> inp101 = builder.binary().known(false);
                results.add(prov.nand(inp101, true));

                Computation<SBool> inp111 = builder.binary().known(true);
                results.add(prov.nand(inp111, true));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(true,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[1].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[2].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[3].booleanValue());
        }
      };
    }
  }

  
  public static class TestAndFromCopyConst<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestAndFromCopyConst() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();

                Computation<SBool> inp100 = builder.binary().known(false);
                results.add(prov.and(inp100, false));

                Computation<SBool> inp110 = builder.binary().known(true);
                results.add(prov.and(inp110, false));

                Computation<SBool> inp101 = builder.binary().known(false);
                results.add(prov.and(inp101, true));

                Computation<SBool> inp111 = builder.binary().known(true);
                results.add(prov.and(inp111, true));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(false,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[1].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[2].booleanValue());

          Assert.assertEquals(true,
              app.getOutputs()[3].booleanValue());
        }
      };
    }
  }

  public static class TestNotFromXor<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestNotFromXor() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          TestBoolApplication app = new TestBoolApplication(){

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilderBinary.createApplicationRoot((BuilderFactoryBinary)factoryProducer, (builder) -> {

                GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(builder);
                List<Computation<SBool>> results = new ArrayList<Computation<SBool>>();

                Computation<SBool> inp100 = builder.binary().known(false);
                results.add(prov.not(inp100));

                Computation<SBool> inp110 = builder.binary().known(true);
                results.add(prov.not(inp110));

                this.outputs = results.stream().map(builder.binary()::open).collect(Collectors.toList());
              }).build();
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(true,
              app.getOutputs()[0].booleanValue());

          Assert.assertEquals(false,
              app.getOutputs()[1].booleanValue());
        }
      };
    }
  }

}
