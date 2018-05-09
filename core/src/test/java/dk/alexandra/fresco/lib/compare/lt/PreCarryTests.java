package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;

public class PreCarryTests {

  public static class TestPreCarryBits<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric numeric = builder.numeric();
            DRes<SInt> p1 = numeric.known(BigInteger.ONE);
            DRes<SInt> g1 = numeric.known(BigInteger.ZERO);
            DRes<SInt> p2 = numeric.known(BigInteger.ONE);
            DRes<SInt> g2 = numeric.known(BigInteger.ONE);
            SIntPair pairOne = new SIntPair(p1, g1);
            SIntPair pairTwo = new SIntPair(p2, g2);
            List<SIntPair> pairs = Arrays.asList(
                pairOne,
                pairTwo
            );
            DRes<SInt> carried = builder.seq(new PreCarryBits(() -> pairs));
            return builder.numeric().open(carried);
          };
          BigInteger actual = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, actual);
        }
      };
    }
  }

}
