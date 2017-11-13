package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.AdvancedBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
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
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> {

            List<DRes<Pair<SBool, SBool>>> data = new ArrayList<>();

            return producer.seq(seq -> {
              AdvancedBinary prov = seq.advancedBinary();
              DRes<SBool> inp0 = seq.binary().known(false);
              DRes<SBool> inp1 = seq.binary().known(true);
              data.add(prov.oneBitHalfAdder(inp0, inp0));
              data.add(prov.oneBitHalfAdder(inp0, inp1));
              data.add(prov.oneBitHalfAdder(inp1, inp0));
              data.add(prov.oneBitHalfAdder(inp1, inp1));
              return () -> data;
            }).seq((seq, dat) -> {
              List<DRes<Boolean>> out = new ArrayList<>();
              for (DRes<Pair<SBool, SBool>> o : dat) {
                out.add(seq.binary().open(o.out().getFirst()));
                out.add(seq.binary().open(o.out().getSecond()));
              }
              return () -> out.stream().map(DRes::out).collect(Collectors.toList());
            });
          };

          List<Boolean> outputs = runApplication(app);
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
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> {

            List<DRes<Pair<SBool, SBool>>> data = new ArrayList<>();

            return producer.seq(seq -> {
              AdvancedBinary prov = seq.advancedBinary();
              DRes<SBool> inp0 = seq.binary().known(false);
              DRes<SBool> inp1 = seq.binary().known(true);
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
              List<DRes<Boolean>> out = new ArrayList<>();
              for (DRes<Pair<SBool, SBool>> o : dat) {
                out.add(seq.binary().open(o.out().getFirst()));
                out.add(seq.binary().open(o.out().getSecond()));
              }
              return () -> out.stream().map(DRes::out).collect(Collectors.toList());
            });
          };

          List<Boolean> outputs = runApplication(app);
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
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        List<Boolean> rawFirst = Arrays.asList(ByteAndBitConverter.toBoolean("ff"));
        List<Boolean> rawSecond = Arrays.asList(ByteAndBitConverter.toBoolean("01"));

        final String expected = "0101"; // First carry is set to true

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            AdvancedBinary prov = seq.advancedBinary();
            DRes<SBool> carry = seq.binary().known(true);

            List<DRes<SBool>> first =
                rawFirst.stream().map(seq.binary()::known).collect(Collectors.toList());
            List<DRes<SBool>> second =
                rawSecond.stream().map(seq.binary()::known).collect(Collectors.toList());

            DRes<List<DRes<SBool>>> adder = prov.fullAdder(first, second, carry);

            return adder;
          }).seq((seq, dat) -> {
            List<DRes<Boolean>> out = new ArrayList<>();
            for (DRes<SBool> o : dat) {
              out.add(seq.binary().open(o));
            }
            return () -> out.stream().map(DRes::out).collect(Collectors.toList());
          });

          List<Boolean> outputs = runApplication(app);
          if (doAsserts) {
            Assert.assertThat(ByteAndBitConverter.toHex(outputs), Is.is(expected));
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
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        List<Boolean> rawLarge = Arrays.asList(ByteAndBitConverter.toBoolean("ff"));

        final String expected = "0100";

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            AdvancedBinary prov = seq.advancedBinary();
            DRes<SBool> increment = seq.binary().known(true);

            List<DRes<SBool>> large =
                rawLarge.stream().map(seq.binary()::known).collect(Collectors.toList());

            DRes<List<DRes<SBool>>> adder = prov.bitIncrement(large, increment);

            return adder;
          }).seq((seq, dat) -> {
            List<DRes<Boolean>> out = new ArrayList<>();
            for (DRes<SBool> o : dat) {
              out.add(seq.binary().open(o));
            }
            return () -> out.stream().map(DRes::out).collect(Collectors.toList());
          });

          List<Boolean> outputs = runApplication(app);

          Assert.assertThat(ByteAndBitConverter.toHex(outputs), Is.is(expected));
          Assert.assertThat(outputs.size(), Is.is(rawLarge.size() + 1));
        }
      };
    }
  }
}
