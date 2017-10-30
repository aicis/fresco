package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.LinearLookUp;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Assert;

public class SearchingTests {

  public static class TestIsSorted<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          final int PAIRS = 10;
          final int MAXVALUE = 20000;
          final int NOTFOUND = -1;
          int[] values = new int[PAIRS];
          Application<Pair<ArrayList<DRes<SInt>>, ArrayList<DRes<SInt>>>, ProtocolBuilderNumeric> app =
              producer -> {
            ArrayList<DRes<SInt>> sKeys = new ArrayList<>(PAIRS);
            ArrayList<DRes<SInt>> sValues = new ArrayList<>(PAIRS);

            Numeric numeric = producer.numeric();
            Random rand = new Random(0);
            for (int i = 0; i < PAIRS; i++) {
              values[i] = rand.nextInt(MAXVALUE);
              DRes<SInt> sInt = numeric.known(BigInteger.valueOf(i));
              sKeys.add(sInt);
              DRes<SInt> valueSInt = numeric.known(BigInteger.valueOf(values[i]));
              sValues.add(valueSInt);
            }
            return () -> new Pair<>(sKeys, sValues);
          };
          Pair<ArrayList<DRes<SInt>>, ArrayList<DRes<SInt>>> inputs = runApplication(app);
          ArrayList<DRes<SInt>> sKeys = inputs.getFirst();
          ArrayList<DRes<SInt>> sValues = inputs.getSecond();
          for (int i = 0; i < PAIRS; i++) {
            final int counter = i;

            Application<BigInteger, ProtocolBuilderNumeric> app1 = producer -> 
              producer.seq((seq) -> seq.numeric().known(BigInteger.valueOf(NOTFOUND)))
                    .seq((seq, notFound) -> seq
                        .seq(new LinearLookUp(sKeys.get(counter), sKeys, sValues, notFound)))
                .seq((seq, out) -> seq.numeric().open(out));
            BigInteger bigInteger = runApplication(app1);

            Assert.assertEquals("Checking value index " + i, values[i], bigInteger.intValue());
          }
        }
      };
    }
  }
}
