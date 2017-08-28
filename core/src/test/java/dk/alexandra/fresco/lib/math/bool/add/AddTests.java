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
package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilderAdvanced;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Assert;

public class AddTests {

  public static class TestOnebitHalfAdder<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOnebitHalfAdder() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app =
              new Application<List<Boolean>, ProtocolBuilderBinary>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(ProtocolBuilderBinary producer) {

              List<Computation<Pair<SBool, SBool>>> data =
                  new ArrayList<Computation<Pair<SBool, SBool>>>();

              ProtocolBuilderBinary builder = (ProtocolBuilderBinary) producer;

              return builder.seq(seq -> {
                BinaryBuilderAdvanced prov = seq.advancedBinary();
                Computation<SBool> inp0 = seq.binary().known(false);
                Computation<SBool> inp1 = seq.binary().known(true);
                data.add(prov.oneBitHalfAdder(inp0, inp0));
                data.add(prov.oneBitHalfAdder(inp0, inp1));
                data.add(prov.oneBitHalfAdder(inp1, inp0));
                data.add(prov.oneBitHalfAdder(inp1, inp1));
                return () -> data;
              }).seq((seq, dat) -> {
                System.out.println("got: " + dat.get(0));
                List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                for (Computation<Pair<SBool, SBool>> o : dat) {
                  System.out.println("adding " + o.out().getFirst());
                  out.add(seq.binary().open(o.out().getFirst()));
                  out.add(seq.binary().open(o.out().getSecond()));
                }
                System.out.println("so our out has : " + out.get(0));
                // out = list<DummyBooleanOpenProtocol>
                return () -> out.stream().map(Computation::out).collect(Collectors.toList());
              });
            }
          };


          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(outputs.get(0), Is.is(false));
          Assert.assertThat(outputs.get(1), Is.is(false));
          Assert.assertThat(outputs.get(2), Is.is(true));
          Assert.assertThat(outputs.get(3), Is.is(false));
          Assert.assertThat(outputs.get(4), Is.is(true));
          Assert.assertThat(outputs.get(5), Is.is(false));
          Assert.assertThat(outputs.get(6), Is.is(false));
          Assert.assertThat(outputs.get(7), Is.is(true));
        }
      };
    }
  }

  public static class TestOnebitFullAdder<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOnebitFullAdder() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app =
              new Application<List<Boolean>, ProtocolBuilderBinary>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(ProtocolBuilderBinary producer) {

              List<Computation<Pair<SBool, SBool>>> data =
                  new ArrayList<Computation<Pair<SBool, SBool>>>();

              ProtocolBuilderBinary builder = (ProtocolBuilderBinary) producer;

              return builder.seq(seq -> {
                BinaryBuilderAdvanced prov = seq.advancedBinary();
                Computation<SBool> inp0 = seq.binary().known(false);
                Computation<SBool> inp1 = seq.binary().known(true);
                data.add(prov.oneBitFullAdder(inp0, inp0, inp0));
                data.add(prov.oneBitFullAdder(inp0, inp0, inp1));
                data.add(prov.oneBitFullAdder(inp0, inp1, inp0));
                data.add(prov.oneBitFullAdder(inp0, inp1, inp1));
                data.add(prov.oneBitFullAdder(inp1, inp0, inp0));
                data.add(prov.oneBitFullAdder(inp1, inp0, inp1));
                data.add(prov.oneBitFullAdder(inp1, inp1, inp0));
                data.add(prov.oneBitFullAdder(inp1, inp1, inp1));
                return () -> data;
              }).seq((seq, dat) -> {
                List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                for (Computation<Pair<SBool, SBool>> o : dat) {
                  out.add(seq.binary().open(o.out().getFirst()));
                  out.add(seq.binary().open(o.out().getSecond()));
                }
                return () -> out.stream().map(Computation::out).collect(Collectors.toList());
              });
            }
          };

          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(outputs.get(0), Is.is(false)); // 000
          Assert.assertThat(outputs.get(1), Is.is(false)); // 000
          Assert.assertThat(outputs.get(2), Is.is(true)); // 001
          Assert.assertThat(outputs.get(3), Is.is(false)); // 001
          Assert.assertThat(outputs.get(4), Is.is(true)); // 010
          Assert.assertThat(outputs.get(5), Is.is(false)); // 010
          Assert.assertThat(outputs.get(6), Is.is(false)); // 011
          Assert.assertThat(outputs.get(7), Is.is(true)); // 011
          Assert.assertThat(outputs.get(8), Is.is(true)); // 100
          Assert.assertThat(outputs.get(9), Is.is(false)); // 100
          Assert.assertThat(outputs.get(10), Is.is(false)); // 101
          Assert.assertThat(outputs.get(11), Is.is(true)); // 101
          Assert.assertThat(outputs.get(12), Is.is(false)); // 110
          Assert.assertThat(outputs.get(13), Is.is(true)); // 110
          Assert.assertThat(outputs.get(14), Is.is(true)); // 111
          Assert.assertThat(outputs.get(15), Is.is(true)); // 111
        }
      };
    }
  }

  public static class TestFullAdder<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    private boolean doAsserts = false;

    public TestFullAdder() {}

    public TestFullAdder(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        List<Boolean> rawFirst = Arrays.asList(ByteArithmetic.toBoolean("ff"));
        List<Boolean> rawSecond = Arrays.asList(ByteArithmetic.toBoolean("01"));

        final String expected = "0101"; // First carry is set to true

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app =
              new Application<List<Boolean>, ProtocolBuilderBinary>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(ProtocolBuilderBinary producer) {
              ProtocolBuilderBinary builder = (ProtocolBuilderBinary) producer;

              return builder.seq(seq -> {
                BinaryBuilderAdvanced prov = seq.advancedBinary();
                Computation<SBool> carry = seq.binary().known(true);

                List<Computation<SBool>> first =
                    rawFirst.stream().map(seq.binary()::known).collect(Collectors.toList());
                List<Computation<SBool>> second =
                    rawSecond.stream().map(seq.binary()::known).collect(Collectors.toList());

                Computation<List<Computation<SBool>>> adder = prov.fullAdder(first, second, carry);

                return () -> adder.out();
              }).seq((seq, dat) -> {
                List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                for (Computation<SBool> o : dat) {
                  out.add(seq.binary().open(o));
                }
                return () -> out.stream().map(Computation::out).collect(Collectors.toList());
              });
            }
          };

          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          if (doAsserts) {
            Assert.assertThat(ByteArithmetic.toHex(outputs), Is.is(expected));
            Assert.assertThat(outputs.size(), Is.is(rawFirst.size() + 1));
          }
        }
      };
    }
  }

  public static class TestBitIncrement<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestBitIncrement() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        List<Boolean> rawLarge = Arrays.asList(ByteArithmetic.toBoolean("ff"));

        final String expected = "0100";

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app =
              new Application<List<Boolean>, ProtocolBuilderBinary>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(ProtocolBuilderBinary producer) {

              ProtocolBuilderBinary builder = (ProtocolBuilderBinary) producer;

              return builder.seq(seq -> {
                BinaryBuilderAdvanced prov = seq.advancedBinary();
                Computation<SBool> increment = seq.binary().known(true);

                List<Computation<SBool>> large =
                    rawLarge.stream().map(seq.binary()::known).collect(Collectors.toList());

                Computation<List<Computation<SBool>>> adder = prov.bitIncrement(large, increment);

                return () -> adder.out();
              }).seq((seq, dat) -> {
                List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                for (Computation<SBool> o : dat) {
                  out.add(seq.binary().open(o));
                }
                return () -> out.stream().map(Computation::out).collect(Collectors.toList());
              });
            }
          };

          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertThat(ByteArithmetic.toHex(outputs), Is.is(expected));
          Assert.assertThat(outputs.size(), Is.is(rawLarge.size() + 1));
        }
      };
    }
  }
}
