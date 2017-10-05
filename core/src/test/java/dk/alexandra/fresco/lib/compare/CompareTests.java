package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class CompareTests {

  public static class CompareAndSwapTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public CompareAndSwapTest() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          List<Boolean> rawLeft = Arrays.asList(ByteArithmetic.toBoolean("ee"));
          List<Boolean> rawRight = Arrays.asList(ByteArithmetic.toBoolean("00"));


          Application<List<List<Boolean>>, ProtocolBuilderBinary> app =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> left =
                    rawLeft.stream().map(seq.binary()::known).collect(Collectors.toList());
                List<DRes<SBool>> right =
                    rawRight.stream().map(seq.binary()::known).collect(Collectors.toList());

                DRes<List<List<DRes<SBool>>>> compared =
                    new CompareAndSwap(left, right).buildComputation(seq);
                return compared;
              }).seq((seq, opened) -> {
                List<List<DRes<Boolean>>> result =
                    new ArrayList<>();
                for (List<DRes<SBool>> entry : opened) {
                  result.add(entry.stream().map(DRes::out).map(seq.binary()::open)
                      .collect(Collectors.toList()));
                }

                return () -> result;
              }).seq((seq, opened) -> {
                List<List<Boolean>> result = new ArrayList<>();
                for (List<DRes<Boolean>> entry : opened) {
                  result.add(entry.stream().map(DRes::out).collect(Collectors.toList()));
                }

                return () -> result;
              });

          List<List<Boolean>> res = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals("00", ByteArithmetic.toHex(res.get(0)));
          Assert.assertEquals("ee", ByteArithmetic.toHex(res.get(1)));
        }
      };
    }
  }
}
