package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;


/**
 * Test for the generic exponentiation pipe.
 */
public class ExponentiationPipeTests {

  public static class TestPreprocessedValues<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    static final int[] lengths = {0, 1, 2, 5, 8, 16, 23, 31, 49};

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> producer.par(par -> {
                try {
                  par.preprocessedValues().getExponentiationPipe(-1);
                  fail("Should throw exception on negative lenght");
                } catch (IllegalArgumentException e) {
                  // This should happen
                }
                List<DRes<List<DRes<SInt>>>> pipes = new ArrayList<>(lengths.length);
                for (int i = 0; i < lengths.length; i++) {
                  pipes.add(par.preprocessedValues().getExponentiationPipe(lengths[i]));
                }
                return () -> pipes;
              }).par((par, pipes) -> {
                List<DRes<BigInteger>> output = pipes.stream()
                    .map(v -> v.out())
                    .flatMap(p -> p.stream())
                    .map(e -> par.numeric().open(e))
                    .collect(Collectors.toList());
                return () -> output;
              }).seq((seq, output) -> () ->
              output.stream().map(DRes::out).collect(Collectors.toList()));
          List<BigInteger> output = runApplication(app);
          int sumLengths = IntStream.range(0, lengths.length).map(i -> lengths[i]).sum();
          sumLengths += 2 * lengths.length;
          assertEquals(sumLengths, output.size());
          BigInteger modulus =
              ((DummyArithmeticResourcePoolImpl) this.conf.getResourcePool()).getModulus();
          int offset = 0;
          for (int length : lengths) {
            BigInteger baseInv = output.get(offset++);
            BigInteger base = output.get(offset++);
            assertEquals(base.modInverse(modulus), baseInv.mod(modulus));
            for (int i = 2; i < length + 2; i++) {
              BigInteger b = output.get(offset++);
              assertEquals(b.mod(modulus), base.modPow(BigInteger.valueOf(i), modulus));
            }
          }
        }
      };
    }
  }

}
