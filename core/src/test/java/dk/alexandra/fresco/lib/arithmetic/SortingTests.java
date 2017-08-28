/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 */
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.SortingHelperUtility;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;

public class SortingTests {

  public static class TestIsSorted<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = new Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric>() {

            private BigInteger zero = BigInteger.valueOf(0);
            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger two = BigInteger.valueOf(2);
            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger four = BigInteger.valueOf(4);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public Computation<Pair<BigInteger, BigInteger>> prepareApplication(
                ProtocolBuilderNumeric builder) {
              Computation<SInt> zero = builder.numeric().known(this.zero);
              Computation<SInt> one = builder.numeric().known(this.one);
              Computation<SInt> two = builder.numeric().known(this.two);
              Computation<SInt> three = builder.numeric().known(this.three);
              Computation<SInt> four = builder.numeric().known(this.four);
              Computation<SInt> five = builder.numeric().known(this.five);

              List<Computation<SInt>> unsorted = Arrays.asList(one, two, three, five, zero);
              List<Computation<SInt>> sorted = Arrays.asList(three, four, four);

              Computation<SInt> firstResult = new SortingHelperUtility()
                  .isSorted(builder, unsorted);
              Computation<SInt> secondResult = new SortingHelperUtility().isSorted(builder, sorted);

              Computation<BigInteger> firstOpen = builder.numeric().open(firstResult);
              Computation<BigInteger> secondOpen = builder.numeric().open(secondResult);

              return () -> new Pair<>(firstOpen.out(), secondOpen.out());
            }
          };

          Pair<BigInteger, BigInteger> outputs = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ZERO, outputs.getFirst());
          Assert.assertEquals(BigInteger.ONE, outputs.getSecond());
        }
      };
    }
  }

  public static class TestCompareAndSwap<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = new Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric>() {


            private BigInteger one = BigInteger.valueOf(1);
            private BigInteger two = BigInteger.valueOf(2);

            @Override
            public Computation<Pair<BigInteger, BigInteger>> prepareApplication(
                ProtocolBuilderNumeric builder) {
              Computation<SInt> one = builder.numeric().known(this.one);
              Computation<SInt> two = builder.numeric().known(this.two);

              List<Computation<SInt>> initialList = Arrays.asList(two, one);

              return builder.seq(seq -> {
                    new SortingHelperUtility().compareAndSwap(seq, 0, 1, initialList);
                    return () -> initialList;
                  }
              ).seq((seq, list) -> {
                    Computation<BigInteger> firstOpen = seq.numeric().open(list.get(0));
                    Computation<BigInteger> secondOpen = seq.numeric().open(list.get(1));

                    return () -> new Pair<>(firstOpen.out(), secondOpen.out());
                  }
              );
            }
          };

          Pair<BigInteger, BigInteger> outputs = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(BigInteger.ONE, outputs.getFirst());
          Assert.assertEquals(BigInteger.valueOf(2), outputs.getSecond());
        }
      };
    }
  }

  public static class TestSort<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> values;
    private final List<BigInteger> sorted;

    public TestSort(List<BigInteger> values) {
      this.values = values;
      this.sorted = new ArrayList<>(values);
      this.sorted.sort(BigInteger::compareTo);
    }

    public TestSort() {
      this(Arrays.asList(
          BigInteger.valueOf(1),
          BigInteger.valueOf(3),
          BigInteger.valueOf(3),
          BigInteger.valueOf(5),
          BigInteger.ZERO));
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              builder -> {
                NumericBuilder input = builder.numeric();
                List<Computation<SInt>> unsorted = values.stream().map(input::known)
                    .collect(Collectors.toList());

                return builder.seq(seq -> {
                  new SortingHelperUtility().sort(seq, unsorted);
                  return () -> unsorted;
                }).par((par, list) -> {
                  NumericBuilder numeric = par.numeric();
                  List<Computation<BigInteger>> openList = list.stream().map(numeric::open)
                      .collect(Collectors.toList());
                  return () -> openList.stream().map(Computation::out).collect(Collectors.toList());
                });
              };

          List<BigInteger> outputs = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          secureComputationEngine.shutdownSCE();
          Assert.assertEquals(sorted, outputs);
        }
      };
    }
  }

  public static class TestBigSort<ResourcePoolT extends ResourcePool> extends
      TestSort<ResourcePoolT> {

    private static final Random random = new Random();

    public TestBigSort() {
      super(IntStream.range(0, 100)
          .mapToObj((i) -> new BigInteger(10, random))
          .collect(Collectors.toList()));
    }
  }

}
