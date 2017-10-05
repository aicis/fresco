package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;

public class ComparisonTests {

  /**
   * Compares the two numbers 3 and 5 and checks that 3 < 5. Also checks that 5 is not < 3
   *
   * @author Kasper Damgaard
   */
  public static class TestCompareLT<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app =
              new Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric>() {
                private BigInteger three = BigInteger.valueOf(3);
                private BigInteger five = BigInteger.valueOf(5);

                @Override
                public DRes<Pair<BigInteger, BigInteger>> buildComputation(
                    ProtocolBuilderNumeric builder) {
                  Numeric input = builder.numeric();
                  DRes<SInt> x = input.known(three);
                  DRes<SInt> y = input.known(five);
                  Comparison comparison = builder.comparison();
                  DRes<SInt> compResult1 = comparison.compareLEQ(x, y);
                  DRes<SInt> compResult2 = comparison.compareLEQ(y, x);
                  Numeric open = builder.numeric();
                  DRes<BigInteger> res1;
                  DRes<BigInteger> res2;
                  res1 = open.open(compResult1);
                  res2 = open.open(compResult2);
                  return () -> new Pair<>(res1.out(), res2.out());
                }
              };
          Pair<BigInteger, BigInteger> output = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(BigInteger.ONE, output.getFirst());
          Assert.assertEquals(BigInteger.ZERO, output.getSecond());
        }
      };
    }
  }

  /**
   * Compares the two numbers 3 and 5 and checks that 3 == 3. Also checks that 3 != 5
   *
   * @author Kasper Damgaard
   */
  public static class TestCompareEQ<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app =
              new Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric>() {

                private BigInteger three = BigInteger.valueOf(3);
                private BigInteger five = BigInteger.valueOf(5);

                @Override
                public DRes<Pair<BigInteger, BigInteger>> buildComputation(
                    ProtocolBuilderNumeric builder) {
                  Numeric input = builder.numeric();
                  DRes<SInt> x = input.known(three);
                  DRes<SInt> y = input.known(five);
                  Comparison comparison = builder.comparison();
                  DRes<SInt> compResult1 = comparison.equals(x, x);
                  DRes<SInt> compResult2 = comparison.equals(x, y);
                  Numeric open = builder.numeric();
                  DRes<BigInteger> res1 = open.open(compResult1);
                  DRes<BigInteger> res2 = open.open(compResult2);
                  return () -> new Pair<>(res1.out(), res2.out());
                }
              };
          Pair<BigInteger, BigInteger> output = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(BigInteger.ONE, output.getFirst());
          Assert.assertEquals(BigInteger.ZERO, output.getSecond());
        }
      };
    }
  }
}
