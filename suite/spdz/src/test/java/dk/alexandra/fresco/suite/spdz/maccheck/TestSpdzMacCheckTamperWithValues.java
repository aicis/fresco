package dk.alexandra.fresco.suite.spdz.maccheck;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.AbstractSpdzTest;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import org.junit.Test;

public class TestSpdzMacCheckTamperWithValues extends AbstractSpdzTest {

  @Test
  public void testModifyShare() {
    int noOfParties = 2;
    for (int cheatingPartyId = 1; cheatingPartyId <= noOfParties; cheatingPartyId++) {
      runTest(new TestModifyShare<>(cheatingPartyId), EvaluationStrategy.SEQUENTIAL_BATCHED,
          PreprocessingStrategy.DUMMY, noOfParties);
    }
  }

  @Test
  public void testModifyShareThree() {
    int noOfParties = 3;
    for (int cheatingPartyId = 1; cheatingPartyId <= noOfParties; cheatingPartyId++) {
      runTest(new TestModifyShare<>(cheatingPartyId), EvaluationStrategy.SEQUENTIAL_BATCHED,
          PreprocessingStrategy.DUMMY, noOfParties);
    }
  }

  private static class TestModifyShare<ResourcePoolT extends SpdzResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final int cheatingPartyId;

    TestModifyShare(int cheatingPartyId) {
      this.cheatingPartyId = cheatingPartyId;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> input = numeric.input(BigInteger.ONE, 1);
            return producer.seq(seq -> {
              SInt value = input.out();
              if (seq.getBasicNumericContext().getMyId() == cheatingPartyId) {
                value = ((SpdzSInt) value).multiply(BigInteger.valueOf(2));
              }
              final SInt finalSInt = value;
              return seq.numeric().open(() -> finalSInt);
            });
          };
          try {
            runApplication(app);
          } catch (Exception e) {
            assertTrue(e.getCause() instanceof MaliciousException);
          }
        }
      };
    }
  }

}
