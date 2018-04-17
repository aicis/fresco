package dk.alexandra.fresco.lib.math.integer.mod;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class Mod2mTests {

  public static class TestMod2mBaseCase<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<SInt, ProtocolBuilderNumeric> app = builder -> {
            // TODO implement
            DRes<SInt> value = builder.numeric().known(BigInteger.ONE);
            int m = 64;
            return builder.seq(new Mod2m(value, m));
          };
          DRes<SInt> result = runApplication(app);
        }
      };
    }
  }

}
