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
 * Test for the generic exponentiation pipe
 */
public class ExponentiationPipeTests {

  public static class TestPreprocessedValues<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app 
            = producer -> producer.seq(seq -> {
            DRes<List<DRes<SInt>>> input1 = seq.preprocessedValues().getExponentiationPipe(2);
            DRes<List<DRes<SInt>>> input2 = seq.preprocessedValues().getExponentiationPipe(2);
            List<DRes<List<DRes<SInt>>>> pipes = new ArrayList<>();
            pipes.add(input1);
            pipes.add(input2);
            
            return () -> pipes;
            }).seq((seq, res) -> {
              List<DRes<BigInteger>> result = new ArrayList<>();
              for(DRes<SInt> s: res.get(0).out()){
                result.add(seq.numeric().open(s));
              }
              for(DRes<SInt> s: res.get(1).out()){
                result.add(seq.numeric().open(s));
              }
              return () -> result;
              
            }).seq(
                (seq, output) -> () -> output.stream().map(DRes::out).collect(Collectors.toList()));
             
          List<BigInteger> output = runApplication(app);
          BigInteger modulus = ((DummyArithmeticResourcePoolImpl)this.conf.getResourcePool()).getModulus();
          BigInteger base1 = output.get(1);
          BigInteger base2 = output.get(5);

          Assert.assertEquals(output.get(0), base1.modInverse(modulus));
          Assert.assertEquals(output.get(2).mod(modulus), base1.modPow(BigInteger.valueOf(2), modulus));
          Assert.assertEquals(output.get(3).mod(modulus), base1.modPow(BigInteger.valueOf(3), modulus));
          Assert.assertEquals(output.get(4), base2.modInverse(modulus));
          Assert.assertEquals(output.get(6).mod(modulus), base2.modPow(BigInteger.valueOf(2), modulus));
          Assert.assertEquals(output.get(7).mod(modulus), base2.modPow(BigInteger.valueOf(3), modulus));
        }
      };
    }
  }
}
