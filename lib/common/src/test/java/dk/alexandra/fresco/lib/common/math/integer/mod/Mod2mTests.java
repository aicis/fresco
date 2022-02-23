package dk.alexandra.fresco.lib.common.math.integer.mod;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class Mod2mTests {

  public static class TestMod2mBaseCase<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private Pair<List<BigInteger>, List<BigInteger>> getExpecteds(int m, int k) {
          BigInteger two = new BigInteger("2");
          List<BigInteger> inputs = new ArrayList<>();
          inputs.add(BigInteger.ONE);
          inputs.add(two.pow(m + 3));
          inputs.add(two.pow(k - 2).add(new BigInteger("3")));
          inputs.add(new BigInteger("3573894"));
          inputs.add(new BigInteger("-1"));
          inputs.add(two.pow(m).subtract(BigInteger.ONE).multiply(
              new BigInteger("-1")));
          inputs.add(two.pow(m + 4).multiply(new BigInteger("-1")));
          inputs.add(two.pow(m + 5).add(new BigInteger("23493892").multiply(
              new BigInteger("-1"))));

          List<BigInteger> outputs = new ArrayList<>();
          outputs.add(BigInteger.ONE);
          outputs.add(BigInteger.ZERO);
          outputs.add(new BigInteger("3"));
          outputs.add(new BigInteger("3573894"));
          outputs.add(two.pow(m).add(new BigInteger("-1")));
          outputs.add(BigInteger.ONE);
          outputs.add(BigInteger.ZERO);
          outputs.add(two.pow(m).subtract(new BigInteger("23493892")));
          return new Pair<List<BigInteger>, List<BigInteger>>(
              inputs, outputs);
        }

        private void runTest(int m, int k, int kappa) {
          Pair<List<BigInteger>, List<BigInteger>> expecteds = getExpecteds(
              m, k);
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            // Make input list into list of differed, known, shared integers
            List<DRes<SInt>> inputs = expecteds.getFirst().stream().map(
                input -> builder.numeric().known(
                    input)).collect(Collectors.toList());
            // Apply mod2m to each of the inputs, open the result
            List<DRes<BigInteger>> results = inputs.stream().map(
                input -> builder.numeric().open(builder.seq(
                    new Mod2m(input, m, k, kappa)))).collect(Collectors.toList());
            return () -> results.stream().map(DRes::out).collect(Collectors
                .toList());
          };
          List<BigInteger> actuals = runApplication(app);
          Assert.assertArrayEquals(expecteds.getSecond().toArray(), actuals
              .toArray());
        }

        @Override
        public void test() {
          runTest(32, 64, 40);
          runTest(64, 128, 80);
        }
      };
    }
  }

  public static class TestMod2m<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<Integer> inputs = Arrays.asList(0, -1, 127, 64);
      int shifts = 6;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {
                List<DRes<SInt>> closed = inputs.stream().map(
                    seq.numeric()::known).collect(Collectors.toList());
                return DRes.of(closed.stream().map(
                    x -> AdvancedNumeric.using(seq).mod2m(x, shifts)).collect(Collectors.toList()));
              }).seq((seq, result) -> Collections.using(seq).openList(DRes.of(result))).seq(
                  (seq, result) -> DRes
                      .of(result.stream().map(DRes::out).collect(Collectors.toList())));

          List<BigInteger> out = runApplication(testApplication);

          for (int i = 0; i < inputs.size(); i++) {
            BigInteger expected = BigInteger.valueOf(inputs.get(i))
                .mod(BigInteger.ONE.shiftLeft(shifts));
            Assert.assertEquals(expected, out.get(i));
          }
        }

      };
    }
  }

  public static class TestMod2mTrivial<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<Integer> inputs = Arrays.asList(1, 7, 31);
      int shifts = 5;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {
                List<DRes<SInt>> closed = inputs.stream().map(
                    seq.numeric()::known).collect(Collectors.toList());
                return DRes.of(closed.stream().map(
                    x -> seq.seq(new Mod2m(x, 6, 5, seq.getBasicNumericContext().getStatisticalSecurityParam()))).collect(Collectors.toList()));
              }).seq((seq, result) -> Collections.using(seq).openList(DRes.of(result))).seq(
                  (seq, result) -> DRes
                      .of(result.stream().map(DRes::out).collect(Collectors.toList())));

          List<BigInteger> out = runApplication(testApplication);

          for (int i = 0; i < inputs.size(); i++) {
            BigInteger expected = BigInteger.valueOf(inputs.get(i))
                .mod(BigInteger.ONE.shiftLeft(shifts));
            Assert.assertEquals(expected, out.get(i));
          }
        }

      };
    }
  }


}