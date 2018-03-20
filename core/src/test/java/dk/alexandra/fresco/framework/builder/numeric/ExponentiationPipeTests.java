package dk.alexandra.fresco.framework.builder.numeric;

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
import org.junit.Assert;


/**
 * Test for the generic exponentiation pipe.
 */
public class ExponentiationPipeTests {

  public static class TestPreprocessedValues<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    static final int length1 = 30;
    static final int length2 = 200;

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                DRes<List<DRes<SInt>>> input1 = seq.preprocessedValues()
                    .getExponentiationPipe(length1);
                DRes<List<DRes<SInt>>> input2 = seq.preprocessedValues()
                    .getExponentiationPipe(length2);
                List<DRes<List<DRes<SInt>>>> pipes = new ArrayList<>();
                pipes.add(input1);
                pipes.add(input2);
                return () -> pipes;
              }).par((par, res) -> {
                List<DRes<BigInteger>> result = new ArrayList<>();
                for (DRes<SInt> s : res.get(0).out()) {
                  result.add(par.numeric().open(s));
                }
                for (DRes<SInt> s : res.get(1).out()) {
                  result.add(par.numeric().open(s));
                }
                return () -> result;
              }).seq((seq,
                  output) -> () -> output.stream().map(DRes::out).collect(Collectors.toList()));

          List<BigInteger> output = runApplication(app);
          Assert.assertEquals(length1 + length2 + 4, output.size());
          BigInteger modulus =
              ((DummyArithmeticResourcePoolImpl) this.conf.getResourcePool()).getModulus();
          BigInteger base1 = output.get(1);
          BigInteger base2 = output.get(length1 + 3);
          Assert.assertEquals(output.get(0), base1.modInverse(modulus));
          Assert.assertEquals(output.get(length1 + 2), base2.modInverse(modulus));
          for (int i = 0; i < length1 + 1; i++) {
            Assert.assertEquals(output.get(i + 1).mod(modulus),
                base1.modPow(BigInteger.valueOf(i + 1), modulus));
          }
          for (int i = 0; i < length2 + 1; i++) {
            Assert.assertEquals(output.get(length1 + 3 + i).mod(modulus),
                base2.modPow(BigInteger.valueOf(i + 1), modulus));
          }
        }
      };
    }
  }


}
