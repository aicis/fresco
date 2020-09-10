package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * Test class for the DEASolver. Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem as inputs (i.e. the number of
 * input and output variables, the number of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.
 *
 */
public class NumericSortingTests {

  public static class TestOddEvenMergeSort<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOddEvenMergeSort() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {

//          BigInteger left1 = new BigInteger("1");
//          BigInteger left2 = new BigInteger("8");
//          BigInteger left3 = new BigInteger("7");
//          BigInteger left4 = new BigInteger("3");
//          BigInteger left5 = new BigInteger("0");
//          BigInteger left6 = new BigInteger("6");
//          BigInteger left7 = new BigInteger("5");
//          BigInteger left8 = new BigInteger("2");
//
//
//          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
//              new Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary>() {
//
//                @Override
//                public DRes<List<Pair<List<Boolean>, List<Boolean>>>> buildComputation(
//                    ProtocolBuilderBinary producer) {
//                  return producer.seq(seq -> {
//                    Binary builder = seq.binary();
//                    List<DRes<SBool>> l1 =
//                        Arrays.asList(left1).stream().map(builder::known).collect(Collectors.toList());
//                    List<DRes<SBool>> l2 =
//                        Arrays.asList(left2).stream().map(builder::known).collect(Collectors.toList());
//                    List<DRes<SBool>> l3 =
//                        Arrays.asList(left3).stream().map(builder::known).collect(Collectors.toList());
//                    List<DRes<SBool>> l4 =
//                        Arrays.asList(left4).stream().map(builder::known).collect(Collectors.toList());
//                    List<DRes<SBool>> l5 =
//                        Arrays.asList(left5).stream().map(builder::known).collect(Collectors.toList());
//                    List<DRes<SBool>> l6 =
//                        Arrays.asList(left6).stream().map(builder::known).collect(Collectors.toList());
//                    List<DRes<SBool>> l7 =
//                        Arrays.asList(left7).stream().map(builder::known).collect(Collectors.toList());
//                    List<DRes<SBool>> l8 =
//                        Arrays.asList(left8).stream().map(builder::known).collect(Collectors.toList());
//
//                    // Empty data payloas
//                    List<DRes<SBool>> emptyData =
//                        Arrays.asList(false).stream().map(builder::known).collect(Collectors.toList());
//
//                    List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unSorted = new ArrayList<>();
//
//                    unSorted.add(new Pair<>(l1, emptyData));
//                    unSorted.add(new Pair<>(l2, emptyData));
//                    unSorted.add(new Pair<>(l3, emptyData));
//                    unSorted.add(new Pair<>(l4, emptyData));
//                    unSorted.add(new Pair<>(l5, emptyData));
//                    unSorted.add(new Pair<>(l6, emptyData));
//                    unSorted.add(new Pair<>(l7, emptyData));
//                    unSorted.add(new Pair<>(l8, emptyData));
//
//                    DRes<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>> sorted =
//                        new OddEvenMerge(unSorted).buildComputation(seq);
//                    return sorted;
//                  }).seq((seq, sorted) -> {
//                    Binary builder = seq.binary();
//                    List<Pair<List<DRes<Boolean>>, List<DRes<Boolean>>>> opened = new ArrayList<>();
//                    for (Pair<List<DRes<SBool>>, List<DRes<SBool>>> p : sorted) {
//                      List<DRes<Boolean>> oKeys = new ArrayList<>();
//                      for (DRes<SBool> key : p.getFirst()) {
//                        oKeys.add(builder.open(key));
//                      }
//                      List<DRes<Boolean>> oValues = new ArrayList<>();
//                      for (DRes<SBool> value : p.getSecond()) {
//                        oValues.add(builder.open(value));
//                      }
//                      opened.add(new Pair<>(oKeys, oValues));
//                    }
//                    return () -> opened;
//                  }).seq((seq, opened) -> {
//                    return () -> opened.stream().map((p) -> {
//                      List<Boolean> key =
//                          p.getFirst().stream().map(DRes::out).collect(Collectors.toList());
//                      List<Boolean> value =
//                          p.getSecond().stream().map(DRes::out).collect(Collectors.toList());
//                      return new Pair<>(key, value);
//                    }).collect(Collectors.toList());
//                  });
//                }
//
//              };
//
//          List<Pair<List<Boolean>, List<Boolean>>> results = runApplication(app);
//
//          // The payload for all is simply the value false
//          Assert.assertEquals(Arrays.asList(left2), results.get(0).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(0).getSecond());
//          Assert.assertEquals(Arrays.asList(left3), results.get(1).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(1).getSecond());
//          Assert.assertEquals(Arrays.asList(left6), results.get(2).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(2).getSecond());
//          Assert.assertEquals(Arrays.asList(left7), results.get(3).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(3).getSecond());
//          Assert.assertEquals(Arrays.asList(left4), results.get(4).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(4).getSecond());
//          Assert.assertEquals(Arrays.asList(left8), results.get(5).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(5).getSecond());
//          Assert.assertEquals(Arrays.asList(left1), results.get(6).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(6).getSecond());
//          Assert.assertEquals(Arrays.asList(left5), results.get(7).getFirst());
//          Assert.assertEquals(Arrays.asList(false), results.get(7).getSecond());
        }
      };
    }
  }
}
