/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

/**
 * Test class for the DEASolver. Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem as inputs (i.e. the number of
 * input and output variables, the number of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.
 *
 */
public class CollectionsSortingTests {

  public static class TestKeyedCompareAndSwap<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    public TestKeyedCompareAndSwap() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          List<Boolean> rawLeftKey = Arrays.asList(ByteArithmetic.toBoolean("49"));
          List<Boolean> rawLeftValue = Arrays.asList(ByteArithmetic.toBoolean("00"));
          List<Boolean> rawRightKey = Arrays.asList(ByteArithmetic.toBoolean("ff"));
          List<Boolean> rawRightValue = Arrays.asList(ByteArithmetic.toBoolean("ee"));

          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
              builder -> {

            ProtocolBuilderBinary seqBuilder = (ProtocolBuilderBinary) builder;

            return seqBuilder.seq(seq -> {
              List<Computation<SBool>> leftKey =
                  rawLeftKey.stream().map(builder.binary()::known).collect(Collectors.toList());
              List<Computation<SBool>> rightKey =
                  rawRightKey.stream().map(builder.binary()::known).collect(Collectors.toList());
              List<Computation<SBool>> leftValue =
                  rawLeftValue.stream().map(builder.binary()::known).collect(Collectors.toList());
              List<Computation<SBool>> rightValue =
                  rawRightValue.stream().map(builder.binary()::known).collect(Collectors.toList());

              return seq.advancedBinary().keyedCompareAndSwap(new Pair<>(leftKey, leftValue),
                  new Pair<>(rightKey, rightValue));
            }).seq((seq, data) -> {
              List<Pair<List<Computation<Boolean>>, List<Computation<Boolean>>>> open =
                  new ArrayList<>();

              for (Pair<List<Computation<SBool>>, List<Computation<SBool>>> o : data) {

                List<Computation<Boolean>> first =
                    o.getFirst().stream().map(seq.binary()::open).collect(Collectors.toList());
                List<Computation<Boolean>> second =
                    o.getSecond().stream().map(seq.binary()::open).collect(Collectors.toList());

                Pair<List<Computation<Boolean>>, List<Computation<Boolean>>> pair =
                    new Pair<>(first, second);
                open.add(pair);
              }
              return () -> open;
            }).seq((seq, data) -> {
              List<Pair<List<Boolean>, List<Boolean>>> out = new ArrayList<>();
              for (Pair<List<Computation<Boolean>>, List<Computation<Boolean>>> o : data) {
                List<Boolean> first =
                    o.getFirst().stream().map(Computation::out).collect(Collectors.toList());
                List<Boolean> second =
                    o.getSecond().stream().map(Computation::out).collect(Collectors.toList());

                Pair<List<Boolean>, List<Boolean>> pair = new Pair<>(first, second);
                out.add(pair);

              }

              return () -> out;
            });
          };

          List<Pair<List<Boolean>, List<Boolean>>> result = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals("ff", ByteArithmetic.toHex(result.get(0).getFirst()));

          Assert.assertEquals("ee", ByteArithmetic.toHex(result.get(0).getSecond()));

          Assert.assertEquals("49", ByteArithmetic.toHex(result.get(1).getFirst()));

          Assert.assertEquals("00", ByteArithmetic.toHex(result.get(1).getSecond()));
        }
      };
    }
  }

  public static class TestOddEvenMerge<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    public TestOddEvenMerge() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {

          Boolean[] left11 = ByteArithmetic.toBoolean("01");
          Boolean[] left12 = ByteArithmetic.toBoolean("08");
          Boolean[] left21 = ByteArithmetic.toBoolean("03");
          Boolean[] left22 = ByteArithmetic.toBoolean("07");
          Boolean[] left31 = ByteArithmetic.toBoolean("00");
          Boolean[] left32 = ByteArithmetic.toBoolean("06");
          Boolean[] left41 = ByteArithmetic.toBoolean("02");
          Boolean[] left42 = ByteArithmetic.toBoolean("05");

          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
              new Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary>() {

            @Override
            public Computation<List<Pair<List<Boolean>, List<Boolean>>>> buildComputation(
                ProtocolBuilderBinary producer) {
              return producer.seq(seq -> {
                BinaryBuilder builder = seq.binary();
                List<Computation<SBool>> l11 =
                    Arrays.asList(left11).stream().map(builder::known).collect(Collectors.toList());
                List<Computation<SBool>> l12 =
                    Arrays.asList(left12).stream().map(builder::known).collect(Collectors.toList());
                List<Computation<SBool>> l21 =
                    Arrays.asList(left21).stream().map(builder::known).collect(Collectors.toList());
                List<Computation<SBool>> l22 =
                    Arrays.asList(left22).stream().map(builder::known).collect(Collectors.toList());
                List<Computation<SBool>> l31 =
                    Arrays.asList(left31).stream().map(builder::known).collect(Collectors.toList());
                List<Computation<SBool>> l32 =
                    Arrays.asList(left32).stream().map(builder::known).collect(Collectors.toList());

                List<Computation<SBool>> l41 =
                    Arrays.asList(left41).stream().map(builder::known).collect(Collectors.toList());
                List<Computation<SBool>> l42 =
                    Arrays.asList(left42).stream().map(builder::known).collect(Collectors.toList());

                List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>> unSorted =
                    new ArrayList<>();

                unSorted
                    .add(new Pair<List<Computation<SBool>>, List<Computation<SBool>>>(l11, l12));
                unSorted
                    .add(new Pair<List<Computation<SBool>>, List<Computation<SBool>>>(l21, l22));
                unSorted
                    .add(new Pair<List<Computation<SBool>>, List<Computation<SBool>>>(l31, l32));
                unSorted
                    .add(new Pair<List<Computation<SBool>>, List<Computation<SBool>>>(l41, l42));

                Computation<List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>>> sorted =
                    new OddEvenMergeProtocolImpl(unSorted).buildComputation(seq);
                return sorted;
              }).seq((seq, sorted) -> {
                BinaryBuilder builder = seq.binary();
                List<Pair<List<Computation<Boolean>>, List<Computation<Boolean>>>> opened =
                    new ArrayList<>();
                for (Pair<List<Computation<SBool>>, List<Computation<SBool>>> p : sorted) {
                  List<Computation<Boolean>> oKeys = new ArrayList<>();
                  for (Computation<SBool> key : p.getFirst()) {
                    oKeys.add(builder.open(key));
                  }
                  List<Computation<Boolean>> oValues = new ArrayList<>();
                  for (Computation<SBool> value : p.getSecond()) {
                    oValues.add(builder.open(value));
                  }
                  opened.add(new Pair<>(oKeys, oValues));
                }
                return () -> opened;
              }).seq((seq, opened) -> {
                return () -> opened.stream().map((p) -> {
                  List<Boolean> key =
                      p.getFirst().stream().map(Computation::out).collect(Collectors.toList());
                  List<Boolean> value =
                      p.getSecond().stream().map(Computation::out).collect(Collectors.toList());
                  return new Pair<List<Boolean>, List<Boolean>>(key, value);
                }).collect(Collectors.toList());
              });
            }

          };

          List<Pair<List<Boolean>, List<Boolean>>> results = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(Arrays.asList(left21), results.get(0).getFirst());
          Assert.assertEquals(Arrays.asList(left22), results.get(0).getSecond());
          Assert.assertEquals(Arrays.asList(left41), results.get(1).getFirst());
          Assert.assertEquals(Arrays.asList(left42), results.get(1).getSecond());
          Assert.assertEquals(Arrays.asList(left11), results.get(2).getFirst());
          Assert.assertEquals(Arrays.asList(left12), results.get(2).getSecond());
          Assert.assertEquals(Arrays.asList(left31), results.get(3).getFirst());
          Assert.assertEquals(Arrays.asList(left32), results.get(3).getSecond());
        }
      };
    }
  }
}
