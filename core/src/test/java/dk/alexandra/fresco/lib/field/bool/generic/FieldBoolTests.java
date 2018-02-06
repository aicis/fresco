package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.AdvancedBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class FieldBoolTests {

  public static class TestXNorFromXorAndNot<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestXNorFromXorAndNot() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                AdvancedBinary prov = builder.advancedBinary();
                List<DRes<Boolean>> results = new ArrayList<>();
                DRes<SBool> inp100 = builder.binary().known(false);
                DRes<SBool> inp200 = builder.binary().known(false);
                results.add(builder.binary().open(prov.xnor(inp100, inp200)));

                DRes<SBool> inp110 = builder.binary().known(true);
                DRes<SBool> inp210 = builder.binary().known(false);
                results.add(builder.binary().open(prov.xnor(inp110, inp210)));

                DRes<SBool> inp101 = builder.binary().known(false);
                DRes<SBool> inp201 = builder.binary().known(true);
                results.add(builder.binary().open(prov.xnor(inp101, inp201)));

                DRes<SBool> inp111 = builder.binary().known(true);
                DRes<SBool> inp211 = builder.binary().known(true);
                results.add(builder.binary().open(prov.xnor(inp111, inp211)));
                return () -> results;
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(true, res.get(0));
          Assert.assertEquals(false, res.get(1));
          Assert.assertEquals(false, res.get(2));
          Assert.assertEquals(true, res.get(3));
        }
      };
    }
  }

  public static class TestXNorFromOpen<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestXNorFromOpen() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                AdvancedBinary prov = builder.advancedBinary();
                List<DRes<Boolean>> results = new ArrayList<>();

                DRes<SBool> inp100 = builder.binary().known(false);
                results.add(builder.binary().open(prov.xnor(inp100, false)));

                DRes<SBool> inp110 = builder.binary().known(true);
                results.add(builder.binary().open(prov.xnor(inp110, false)));

                DRes<SBool> inp101 = builder.binary().known(false);
                results.add(builder.binary().open(prov.xnor(inp101, true)));

                DRes<SBool> inp111 = builder.binary().known(true);
                results.add(builder.binary().open(prov.xnor(inp111, true)));

                return () -> results;
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(true, res.get(0));
          Assert.assertEquals(false, res.get(1));
          Assert.assertEquals(false, res.get(2));
          Assert.assertEquals(true, res.get(3));
        }
      };
    }
  }


  public static class TestOrFromXorAnd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOrFromXorAnd() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                AdvancedBinary prov = builder.advancedBinary();
                List<DRes<Boolean>> results = new ArrayList<>();
                DRes<SBool> inp100 = builder.binary().known(false);
                DRes<SBool> inp200 = builder.binary().known(false);
                results.add(builder.binary().open(prov.or(inp100, inp200)));

                DRes<SBool> inp110 = builder.binary().known(true);
                DRes<SBool> inp210 = builder.binary().known(false);
                results.add(builder.binary().open(prov.or(inp110, inp210)));

                DRes<SBool> inp101 = builder.binary().known(false);
                DRes<SBool> inp201 = builder.binary().known(true);
                results.add(builder.binary().open(prov.or(inp101, inp201)));

                DRes<SBool> inp111 = builder.binary().known(true);
                DRes<SBool> inp211 = builder.binary().known(true);
                results.add(builder.binary().open(prov.or(inp111, inp211)));
                return () -> results;
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(false, res.get(0));
          Assert.assertEquals(true, res.get(1));
          Assert.assertEquals(true, res.get(2));
          Assert.assertEquals(true, res.get(3));
        }
      };
    }
  }

  public static class TestOrFromCopyConst<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOrFromCopyConst() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                AdvancedBinary prov = builder.advancedBinary();
                List<DRes<Boolean>> results = new ArrayList<>();
                DRes<SBool> inp100 = builder.binary().known(false);
                results.add(builder.binary().open(prov.or(inp100, false)));

                DRes<SBool> inp110 = builder.binary().known(true);
                results.add(builder.binary().open(prov.or(inp110, false)));

                DRes<SBool> inp101 = builder.binary().known(false);
                results.add(builder.binary().open(prov.or(inp101, true)));

                DRes<SBool> inp111 = builder.binary().known(true);
                results.add(builder.binary().open(prov.or(inp111, true)));

                return () -> results;
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(false, res.get(0));
          Assert.assertEquals(true, res.get(1));
          Assert.assertEquals(true, res.get(2));
          Assert.assertEquals(true, res.get(3));
        }
      };
    }
  }

  public static class TestOpen<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                DRes<SBool> inp1 = builder.binary().input(false, 1);
                DRes<SBool> inp2 = builder.binary().input(true, 1);
                DRes<Boolean> open1 = builder.binary().open(inp1);
                DRes<Boolean> open2 = builder.binary().open(inp2);
                DRes<Boolean> open12 = builder.binary().open(inp1, 2);
                DRes<Boolean> open21 = builder.binary().open(inp2, 1);
                return () -> Arrays.asList(open1, open2, open12, open21);
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(false, res.get(0));
          Assert.assertEquals(true, res.get(1));
          Assert.assertEquals(conf.getMyId() == 2 ? false : null, res.get(2));
          Assert.assertEquals(conf.getMyId() == 1 ? true : null, res.get(3));
        }
      };
    }
  }


  public static class TestNandFromAndAndNot<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestNandFromAndAndNot() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                AdvancedBinary prov = builder.advancedBinary();
                List<DRes<Boolean>> results = new ArrayList<>();
                DRes<SBool> inp100 = builder.binary().known(false);
                DRes<SBool> inp200 = builder.binary().known(false);
                results.add(builder.binary().open(prov.nand(inp100, inp200)));

                DRes<SBool> inp110 = builder.binary().known(true);
                DRes<SBool> inp210 = builder.binary().known(false);
                results.add(builder.binary().open(prov.nand(inp110, inp210)));

                DRes<SBool> inp101 = builder.binary().known(false);
                DRes<SBool> inp201 = builder.binary().known(true);
                results.add(builder.binary().open(prov.nand(inp101, inp201)));

                DRes<SBool> inp111 = builder.binary().known(true);
                DRes<SBool> inp211 = builder.binary().known(true);
                results.add(builder.binary().open(prov.nand(inp111, inp211)));

                return () -> results;
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(true, res.get(0));
          Assert.assertEquals(true, res.get(1));
          Assert.assertEquals(true, res.get(2));
          Assert.assertEquals(false, res.get(3));
        }
      };
    }
  }

  public static class TestNandFromOpen<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestNandFromOpen() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                AdvancedBinary prov = builder.advancedBinary();

                List<DRes<Boolean>> results = new ArrayList<>();
                DRes<SBool> inp100 = builder.binary().known(false);
                results.add(builder.binary().open(prov.nand(inp100, false)));

                DRes<SBool> inp110 = builder.binary().known(true);
                results.add(builder.binary().open(prov.nand(inp110, false)));

                DRes<SBool> inp101 = builder.binary().known(false);
                results.add(builder.binary().open(prov.nand(inp101, true)));

                DRes<SBool> inp111 = builder.binary().known(true);
                results.add(builder.binary().open(prov.nand(inp111, true)));

                return () -> results;
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(true, res.get(0));
          Assert.assertEquals(true, res.get(1));
          Assert.assertEquals(true, res.get(2));
          Assert.assertEquals(false, res.get(3));
        }
      };
    }
  }


  public static class TestAndFromCopyConst<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestAndFromCopyConst() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          Application<List<Boolean>, ProtocolBuilderBinary> app =
              producer -> producer.seq(builder -> {
                AdvancedBinary prov = builder.advancedBinary();
                List<DRes<Boolean>> results = new ArrayList<>();

                DRes<SBool> inp100 = builder.binary().known(false);
                results.add(builder.binary().open(prov.and(inp100, false)));

                DRes<SBool> inp110 = builder.binary().known(true);
                results.add(builder.binary().open(prov.and(inp110, false)));

                DRes<SBool> inp101 = builder.binary().known(false);
                results.add(builder.binary().open(prov.and(inp101, true)));

                DRes<SBool> inp111 = builder.binary().known(true);
                results.add(builder.binary().open(prov.and(inp111, true)));

                return () -> results;
              }).seq(
                  (seq, results) -> () -> results.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<Boolean> res = runApplication(app);

          Assert.assertEquals(false, res.get(0));
          Assert.assertEquals(false, res.get(1));
          Assert.assertEquals(false, res.get(2));
          Assert.assertEquals(true, res.get(3));
        }
      };
    }
  }
}
