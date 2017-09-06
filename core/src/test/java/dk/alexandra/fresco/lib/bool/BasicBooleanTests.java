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
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class BasicBooleanTests {

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    private boolean doAsserts;

    public TestInput(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          Application<Boolean, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            Computation<SBool> in = seq.binary().input(true, 1);
            Computation<Boolean> open = seq.binary().open(in);
            return open;
          }).seq((seq, out) -> {
            return () -> out;
          });

          boolean output = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          if (doAsserts) {
            Assert.assertEquals(true, output);
          }
        }
      };
    }
  }

  public static class TestXOR<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    private boolean doAsserts;

    public TestXOR(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            BinaryBuilder builder = seq.binary();
            Computation<SBool> falseBool = builder.known(false);
            Computation<SBool> trueBool = builder.known(true);
            List<Computation<Boolean>> xors = new ArrayList<>();
            xors.add(builder.open(builder.xor(falseBool, falseBool)));
            xors.add(builder.open(builder.xor(trueBool, falseBool)));
            xors.add(builder.open(builder.xor(falseBool, trueBool)));
            xors.add(builder.open(builder.xor(trueBool, trueBool)));
            return () -> xors;
          }).seq((seq, list) -> {
            return () -> list.stream().map(Computation::out).collect(Collectors.toList());
          });

          List<Boolean> outs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          if (doAsserts) {
            Assert.assertEquals(false, outs.get(0));
            Assert.assertEquals(true, outs.get(1));
            Assert.assertEquals(true, outs.get(2));
            Assert.assertEquals(false, outs.get(3));
          }
        }
      };
    }
  }

  public static class TestAND<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    private boolean doAsserts;

    public TestAND(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            BinaryBuilder builder = seq.binary();
            Computation<SBool> falseBool = builder.known(false);
            Computation<SBool> trueBool = builder.known(true);
            List<Computation<Boolean>> list = new ArrayList<>();
            list.add(builder.open(builder.and(falseBool, falseBool)));
            list.add(builder.open(builder.and(trueBool, falseBool)));
            list.add(builder.open(builder.and(falseBool, trueBool)));
            list.add(builder.open(builder.and(trueBool, trueBool)));
            return () -> list;
          }).seq((seq, list) -> {
            return () -> list.stream().map(Computation::out).collect(Collectors.toList());
          });

          List<Boolean> outs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          if (doAsserts) {
            Assert.assertEquals(false, outs.get(0));
            Assert.assertEquals(false, outs.get(1));
            Assert.assertEquals(false, outs.get(2));
            Assert.assertEquals(true, outs.get(3));
          }
        }
      };
    }
  }

  public static class TestNOT<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    private boolean doAsserts;

    public TestNOT(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            BinaryBuilder builder = seq.binary();
            Computation<SBool> falseBool = builder.known(false);
            Computation<SBool> trueBool = builder.known(true);
            List<Computation<Boolean>> list = new ArrayList<>();
            list.add(builder.open(builder.not(falseBool)));
            list.add(builder.open(builder.not(trueBool)));
            return () -> list;
          }).seq((seq, list) -> {
            return () -> list.stream().map(Computation::out).collect(Collectors.toList());
          });

          List<Boolean> outs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          if (doAsserts) {
            Assert.assertEquals(true, outs.get(0));
            Assert.assertEquals(false, outs.get(1));
          }
        }
      };
    }
  }

  /**
   * Tests both input, xor, not, and and output. Computes all variants of: NOT((i1 XOR i2) AND i1)
   */
  public static class TestBasicProtocols<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    private boolean doAsserts;

    public TestBasicProtocols(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            BinaryBuilder builder = seq.binary();
            Computation<SBool> falseBool = builder.known(false);
            Computation<SBool> trueBool = builder.known(true);
            List<Computation<Boolean>> list = new ArrayList<>();
            list.add(builder
                .open(builder.not(builder.and(builder.xor(falseBool, falseBool), falseBool))));
            list.add(builder
                .open(builder.not(builder.and(builder.xor(falseBool, trueBool), falseBool))));
            list.add(
                builder.open(builder.not(builder.and(builder.xor(trueBool, falseBool), trueBool))));
            list.add(
                builder.open(builder.not(builder.and(builder.xor(trueBool, trueBool), trueBool))));
            return () -> list;
          }).seq((seq, list) -> {
            return () -> list.stream().map(Computation::out).collect(Collectors.toList());
          });

          List<Boolean> outs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          if (doAsserts) {
            Assert.assertEquals(true, outs.get(0));
            Assert.assertEquals(true, outs.get(1));
            Assert.assertEquals(false, outs.get(2));
            Assert.assertEquals(true, outs.get(3));
          }
        }
      };
    }
  }
}
