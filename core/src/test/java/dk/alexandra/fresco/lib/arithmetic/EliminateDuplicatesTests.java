package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.list.FindDuplicatesHelper;
import dk.alexandra.fresco.lib.list.SIntListofTuples;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class EliminateDuplicatesTests {

  public static class TestFindDuplicatesOne<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    private BigInteger zero = BigInteger.valueOf(0);
    private BigInteger one = BigInteger.valueOf(1);
    private BigInteger two = BigInteger.valueOf(2);
    private BigInteger three = BigInteger.valueOf(3);
    private BigInteger four = BigInteger.valueOf(4);
    private BigInteger five = BigInteger.valueOf(5);

    private List<List<BigInteger>> values =
        Arrays.asList(
            Arrays.asList(zero, five),
            Arrays.asList(one, zero),
            Arrays.asList(two, three),
            Arrays.asList(three, one),
            Arrays.asList(four, three)
        );

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, SequentialNumericBuilder> app =
              builder -> {
                NumericBuilder input = builder.numeric();
                Computation<SInt> zero = input.known(BigInteger.ZERO);

                SIntListofTuples list1 = new SIntListofTuples(2);
                SIntListofTuples list2 = new SIntListofTuples(2);

                list1.add(
                    values.get(0).stream().map(input::known).collect(Collectors.toList()),
                    zero
                );
                list1.add(
                    values.get(1).stream().map(input::known).collect(Collectors.toList()),
                    zero
                );
                list1.add(
                    values.get(2).stream().map(input::known).collect(Collectors.toList()),
                    zero
                );

                list2.add(
                    values.get(2).stream().map(input::known).collect(Collectors.toList()),
                    zero
                );
                list2.add(
                    values.get(3).stream().map(input::known).collect(Collectors.toList()),
                    zero
                );
                list2.add(
                    values.get(4).stream().map(input::known).collect(Collectors.toList()),
                    zero
                );

                return builder.par(par -> {
                      new FindDuplicatesHelper().findDuplicates(par, list1, list2);
                      return () -> list1;
                    }
                ).par((list, par) -> {
                  NumericBuilder numeric = par.numeric();
                  List<Computation<BigInteger>> openDuplicates = Arrays.asList(
                      numeric.open(list.getDuplicate(0)),
                      numeric.open(list.getDuplicate(1)),
                      numeric.open(list.getDuplicate(2))
                  );
                  return () -> openDuplicates.stream().map(Computation::out)
                      .collect(Collectors.toList());
                });
              };

          List<BigInteger> outputs = secureComputationEngine
              .runApplication(app,
                  SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                      conf.sceConf.getSuite()));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ZERO, outputs.get(0));
          Assert.assertEquals(BigInteger.ZERO, outputs.get(1));
          Assert.assertEquals(BigInteger.ONE, outputs.get(2));
        }
      };
    }
  }
}
