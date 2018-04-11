package dk.alexandra.fresco.lib.collections.batch;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class TestBatchArithmetic {

  public static class TestBatchMultiply<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      int repetitions = 20000;
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                List<BigInteger> inputsLeft = new ArrayList<>(repetitions);
                List<BigInteger> inputsRight = new ArrayList<>(repetitions);
                for (int i = 0; i < repetitions; i++) {
                  inputsLeft.add(BigInteger.valueOf(i));
                  inputsRight.add(BigInteger.valueOf(i + 1));
                }
                DRes<List<DRes<SInt>>> left;
                DRes<List<DRes<SInt>>> right;
                if (seq.getBasicNumericContext().getMyId() == 1) {
                  left = seq.collections().closeList(inputsLeft, 1);
                  right = seq.collections().closeList(inputsRight, 1);
                } else {
                  left = seq.collections().closeList(repetitions, 1);
                  right = seq.collections().closeList(repetitions, 1);
                }
                return seq.collections().openList(seq.collections().batchMultiply(left, right));
              });
          List<BigInteger> expected = new ArrayList<>(repetitions);
          NumericResourcePool resourcePool = conf.getResourcePool();
          for (int i = 0; i < repetitions; i++) {
            BigInteger prod = BigInteger.valueOf(i).multiply(BigInteger.valueOf(i + 1));
            expected.add(resourcePool.convertRepresentation(prod));
          }
          List<BigInteger> actual = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          Assert.assertEquals(expected, actual);
        }
      };
    }

  }


}
