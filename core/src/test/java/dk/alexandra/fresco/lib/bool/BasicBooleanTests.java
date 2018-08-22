package dk.alexandra.fresco.lib.bool;

import static org.junit.Assert.assertFalse;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class BasicBooleanTests {

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

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
            DRes<SBool> in = seq.binary().input(true, 1);
            DRes<Boolean> open = seq.binary().open(in);
            return open;
          }).seq((seq, out) -> {
            return () -> out;
          });

          boolean output = runApplication(app);

          if (doAsserts) {
            Assert.assertEquals(true, output);
          }
        }
      };
    }
  }

  public static class TestInputDifferentSender<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    private boolean doAsserts;

    public TestInputDifferentSender(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          Application<Boolean, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            DRes<SBool> in = seq.binary().input(true, 2);
            DRes<Boolean> open = seq.binary().open(in);
            return open;
          }).seq((seq, out) -> {
            return () -> out;
          });

          boolean output = runApplication(app);

          if (doAsserts) {
            Assert.assertEquals(true, output);
          }
        }
      };
    }
  }


  public static class TestXOR<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

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
            Binary builder = seq.binary();
            DRes<SBool> falseBool = builder.known(false);
            DRes<SBool> trueBool = builder.known(true);
            List<DRes<Boolean>> xors = new ArrayList<>();
            xors.add(builder.open(builder.xor(falseBool, falseBool)));
            xors.add(builder.open(builder.xor(trueBool, falseBool)));
            xors.add(builder.open(builder.xor(falseBool, trueBool)));
            xors.add(builder.open(builder.xor(trueBool, trueBool)));
            return () -> xors;
          }).seq((seq, list) -> {
            return () -> list.stream().map(DRes::out).collect(Collectors.toList());
          });

          List<Boolean> outs = runApplication(app);

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
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

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
            Binary builder = seq.binary();
            DRes<SBool> falseBool = builder.known(false);
            DRes<SBool> trueBool = builder.known(true);
            List<DRes<Boolean>> list = new ArrayList<>();
            list.add(builder.open(builder.and(falseBool, falseBool)));
            list.add(builder.open(builder.and(trueBool, falseBool)));
            list.add(builder.open(builder.and(falseBool, trueBool)));
            list.add(builder.open(builder.and(trueBool, trueBool)));
            return () -> list;
          }).seq((seq, list) -> {
            return () -> list.stream().map(DRes::out).collect(Collectors.toList());
          });

          List<Boolean> outs = runApplication(app);

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

  public static class TestMultipleAnds<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    private final boolean doAsserts;
    private final int amount;

    /**
     * Constructs a test doing a given number of ANDs.
     *
     * <p>
     * Note: this is mainly meant to stress test the protocol suite with a larger number ANDs. All
     * ANDs will be with the arguments <code>false</code> and <code>true</code> and thus should give
     * a result of <code>false</code>.
     * </p>
     *
     * @param doAsserts whether or not to run the asserts
     * @param amount the amount of ANDs to compute
     */
    public TestMultipleAnds(boolean doAsserts, int amount) {
      this.doAsserts = doAsserts;
      this.amount = amount;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app = producer -> producer.par(par -> {
            Binary builder = par.binary();
            DRes<SBool> falseBool = builder.known(false);
            DRes<SBool> trueBool = builder.known(true);
            return () -> new Pair<>(falseBool, trueBool);
          }).par((par, boolPair) -> {
            Binary builder = par.binary();
            DRes<SBool> falseBool = boolPair.getFirst();
            DRes<SBool> trueBool = boolPair.getSecond();
            List<DRes<SBool>> list = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
              list.add(builder.and(falseBool, trueBool));
            }
            return () -> list;
          }).par((par, list) -> {
            List<DRes<Boolean>> openList = new ArrayList<>(list.size());
            for (DRes<SBool> s : list) {
              openList.add(par.binary().open(s));
            }
            return () -> openList;
          }).par((par, list) -> {
            return () -> list.stream().map(DRes::out).collect(Collectors.toList());
          });

          List<Boolean> outs = runApplication(app);
          if (doAsserts) {
            for (Boolean b : outs) {
              assertFalse(b);
            }
          }
        }
      };
    }
  }

  public static class TestNOT<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

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
            Binary builder = seq.binary();
            DRes<SBool> falseBool = builder.known(false);
            DRes<SBool> trueBool = builder.known(true);
            List<DRes<Boolean>> list = new ArrayList<>();
            list.add(builder.open(builder.not(falseBool)));
            list.add(builder.open(builder.not(trueBool)));
            return () -> list;
          }).seq((seq, list) -> {
            return () -> list.stream().map(DRes::out).collect(Collectors.toList());
          });

          List<Boolean> outs = runApplication(app);

          if (doAsserts) {
            Assert.assertEquals(true, outs.get(0));
            Assert.assertEquals(false, outs.get(1));
          }
        }
      };
    }
  }

  public static class TestRandomBit<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    private boolean doAsserts;

    public TestRandomBit(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          Application<Boolean, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            DRes<SBool> in = seq.binary().randomBit();
            DRes<Boolean> open = seq.binary().open(in);
            return open;
          }).seq((seq, out) -> {
            return () -> out;
          });

          boolean output = runApplication(app);

          if (doAsserts) {
            Assert.assertEquals(true, output);
          }
        }
      };
    }
  }

  /**
   * Tests both input, xor, not, and and output. Computes all variants of: NOT((i1 XOR i2) AND i1)
   */
  public static class TestBasicProtocols<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

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
            Binary builder = seq.binary();
            DRes<SBool> falseBool = builder.known(false);
            DRes<SBool> trueBool = builder.known(true);
            List<DRes<Boolean>> list = new ArrayList<>();
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
            return () -> list.stream().map(DRes::out).collect(Collectors.toList());
          });

          List<Boolean> outs = runApplication(app);

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
