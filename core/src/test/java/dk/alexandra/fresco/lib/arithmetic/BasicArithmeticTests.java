package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.core.Is;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 *
 * Can be reused by a test case for any protocol suite that implements the basic field protocol
 * factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in mpc.lib or something.
 */
public class BasicArithmeticTests {

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger value = BigInteger.valueOf(10);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> input = numeric.input(value, 1);
            return numeric.open(input);
          };
          BigInteger output = runApplication(app);

          Assert.assertEquals(value, output);
        }
      };
    }
  }

  public static class TestInputFromAll<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<Pair<Integer, List<DRes<BigInteger>>>, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            int noOfParties = producer.getBasicNumericContext().getNoOfParties();
            List<DRes<SInt>> inputs = new ArrayList<>(noOfParties);
            for (int i = 1; i <= noOfParties; i++) {
              inputs.add(numeric.input(BigInteger.valueOf(i), i));
            }
            DRes<List<DRes<BigInteger>>> opened = producer.collections().openList(() -> inputs);
            return () -> new Pair<>(noOfParties, opened.out());
          };
          Pair<Integer, List<DRes<BigInteger>>> output = runApplication(app);
          int noOfParties = output.getFirst();
          List<DRes<BigInteger>> inputs = output.getSecond();
          Assert.assertEquals(noOfParties, inputs.size());
          for (int i = 0; i < noOfParties; i++) {
            Assert.assertEquals(i + 1, inputs.get(i).out().intValue());
          }
        }
      };
    }
  }

  public static class TestOutputToSingleParty<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger value = BigInteger.valueOf(10);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          for (int partyId = 1; partyId <= conf.getResourcePool().getNoOfParties(); partyId++) {
            final int finalPartyId = partyId;
            Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
              Numeric numeric = producer.numeric();
              DRes<SInt> input = numeric.known(value);
              return numeric.open(input, finalPartyId);
            };
            BigInteger output = runApplication(app);
            if (conf.getMyId() == finalPartyId) {
              Assert.assertEquals(BigInteger.valueOf(10), output);
            } else {
              Assert.assertNull(output);
            }
          }
        }
      };
    }
  }

  public static class TestAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger leftValue = BigInteger.valueOf(10);
      BigInteger rightValue = BigInteger.valueOf(4);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> left = numeric.input(leftValue, 1);
            DRes<SInt> right = numeric.input(rightValue, 1);
            DRes<SInt> result = numeric.add(left, right);
            return numeric.open(result);
          };
          BigInteger output = runApplication(app);
          Assert.assertEquals(leftValue.add(rightValue), output);
        }
      };
    }
  }

  public static class TestAddWithOverflow<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          BigInteger modulus = conf.getResourcePool().getModulus();
          BigInteger leftValue = modulus.subtract(BigInteger.ONE);
          BigInteger rightValue = BigInteger.valueOf(4);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> left = numeric.input(leftValue, 1);
            DRes<SInt> right = numeric.input(rightValue, 1);
            DRes<SInt> result = numeric.add(left, right);
            return numeric.open(result);
          };
          ResourcePoolT resourcePool = conf.getResourcePool();
          BigInteger output = runApplication(app);
          Assert
              .assertEquals(resourcePool.convertRepresentation(leftValue.add(rightValue)), output);
        }
      };
    }

  }

  public static class TestMultiply<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger leftValue = BigInteger.valueOf(10);
      BigInteger rightValue = BigInteger.valueOf(4);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> left = numeric.input(leftValue, 1);
            DRes<SInt> right = numeric.input(rightValue, 1);
            DRes<SInt> result = numeric.mult(left, right);
            return numeric.open(result);
          };
          BigInteger output = runApplication(app);
          Assert.assertEquals(leftValue.multiply(rightValue), output);
        }
      };
    }
  }

  public static class TestMultiplyByZero<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger leftValue = BigInteger.valueOf(10);
      BigInteger rightValue = BigInteger.valueOf(0);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> left = numeric.input(leftValue, 1);
            DRes<SInt> right = numeric.input(rightValue, 1);
            DRes<SInt> result = numeric.mult(left, right);
            return numeric.open(result);
          };
          BigInteger output = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, output);
        }
      };
    }
  }

  public static class TestMultiplyWithOverflow<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          BigInteger modulus = conf.getResourcePool().getModulus();
          BigInteger leftValue = modulus.subtract(BigInteger.ONE);
          BigInteger rightValue = BigInteger.valueOf(2);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> left = numeric.input(leftValue, 1);
            DRes<SInt> right = numeric.input(rightValue, 1);
            DRes<SInt> result = numeric.mult(left, right);
            return numeric.open(result);
          };
          ResourcePoolT resourcePool = conf.getResourcePool();
          BigInteger output = runApplication(app);
          Assert.assertEquals(resourcePool.convertRepresentation(leftValue.multiply(rightValue)),
              output);
        }
      };
    }

  }

  public static class TestSubtract<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger left = BigInteger.valueOf(10);
      BigInteger right = BigInteger.valueOf(4);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> leftClosed = numeric.input(left, 1);
            DRes<SInt> rightClosed = numeric.input(right, 1);
            DRes<SInt> result = numeric.sub(leftClosed, rightClosed);
            return numeric.open(result);
          };
          ResourcePoolT resourcePool = conf.getResourcePool();
          BigInteger expected = resourcePool
              .convertRepresentation(left.subtract(right).mod(resourcePool.getModulus()));
          BigInteger actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestSubtractNegative<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger left = BigInteger.valueOf(4);
      BigInteger right = BigInteger.valueOf(10);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> leftClosed = numeric.input(left, 1);
            DRes<SInt> rightClosed = numeric.input(right, 1);
            DRes<SInt> result = numeric.sub(leftClosed, rightClosed);
            return numeric.open(result);
          };
          ResourcePoolT resourcePool = conf.getResourcePool();
          BigInteger expected = resourcePool
              .convertRepresentation(left.subtract(right).mod(resourcePool.getModulus()));
          BigInteger actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestSubtractPublic<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger left = BigInteger.valueOf(4);
      BigInteger right = BigInteger.valueOf(10);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> leftClosed = numeric.input(left, 1);
            DRes<SInt> result = numeric.sub(leftClosed, right);
            return numeric.open(result);
          };
          ResourcePoolT resourcePool = conf.getResourcePool();
          BigInteger expected = resourcePool
              .convertRepresentation(left.subtract(right).mod(resourcePool.getModulus()));
          BigInteger actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestSubtractFromPublic<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger left = BigInteger.valueOf(4);
      BigInteger right = BigInteger.valueOf(10);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> rightClosed = numeric.input(right, 1);
            DRes<SInt> result = numeric.sub(left, rightClosed);
            return numeric.open(result);
          };
          ResourcePoolT resourcePool = conf.getResourcePool();
          BigInteger expected = resourcePool
              .convertRepresentation(left.subtract(right).mod(resourcePool.getModulus()));
          BigInteger actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestAddPublicValue<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger value = BigInteger.valueOf(10);
      BigInteger add = BigInteger.valueOf(4);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();

            DRes<SInt> input = numeric.input(value, 1);
            DRes<SInt> result = numeric.add(add, input);
            return numeric.open(result);
          };
          BigInteger output = runApplication(app);

          Assert.assertEquals(value.add(add), output);
        }
      };
    }
  }

  public static class TestMultiplyByPublicValue<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger value = BigInteger.valueOf(10);
      BigInteger constant = BigInteger.valueOf(4);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> input = numeric.input(value, 1);
            DRes<SInt> result = numeric.mult(constant, input);
            return numeric.open(result);
          };
          BigInteger output = runApplication(app);

          Assert.assertEquals(value.multiply(constant), output);
        }
      };
    }
  }

  public static class TestRandomBit<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            Collections collections = producer.collections();
            int numBits = 10;
            List<DRes<SInt>> bits = new ArrayList<>(numBits);
            for (int i = 0; i < numBits; i++) {
              bits.add(numeric.randomBit());
            }
            DRes<List<DRes<BigInteger>>> opened = collections.openList(() -> bits);
            return () -> opened.out().stream().map(DRes::out)
                .collect(Collectors.toList());
          };
          List<BigInteger> bits = runApplication(app);
          for (BigInteger bit : bits) {
            Assert.assertTrue("Expected bit but was " + bit,
                bit.equals(BigInteger.ZERO) || bit.equals(BigInteger.ONE));
          }
        }
      };
    }
  }

  public static class TestRandomElement<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            Collections collections = producer.collections();
            int numElements = 10;
            List<DRes<SInt>> elements = new ArrayList<>(numElements);
            for (int i = 0; i < numElements; i++) {
              elements.add(numeric.randomElement());
            }
            DRes<List<DRes<BigInteger>>> opened = collections.openList(() -> elements);
            return () -> opened.out().stream().map(DRes::out)
                .collect(Collectors.toList());
          };
          List<BigInteger> elements = runApplication(app);
          assertAllDifferent(elements);
          Assert.assertEquals(10, elements.size());
        }
      };
    }
  }

  public static class TestOpenWithConversion<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            BigInteger modulus = producer.getBasicNumericContext().getModulus();
            BigInteger input = modulus.divide(BigInteger.valueOf(2)).add(BigInteger.ONE);
            Numeric numeric = producer.numeric();
            DRes<SInt> closed = numeric.input(input, 1);
            DRes<BigInteger> opened = numeric.open(closed);
            BigInteger expected = input.subtract(modulus);
            return () -> new Pair<>(opened.out(), expected);
          };
          Pair<BigInteger, BigInteger> actualAndExpected = runApplication(app);
          Assert.assertEquals(actualAndExpected.getSecond(), actualAndExpected.getFirst());
        }
      };
    }
  }

  public static class TestKnownSInt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigInteger> openInputs =
          Stream.of(200, 300, 1, 2).map(BigInteger::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> producer.par(par -> {
                Numeric numeric = par.numeric();
                List<DRes<SInt>> result =
                    openInputs.stream().map(numeric::known).collect(Collectors.toList());
                return () -> result;
              }).par((par, closed) -> {
                Numeric numeric = par.numeric();
                List<DRes<BigInteger>> result =
                    closed.stream().map(numeric::open).collect(Collectors.toList());
                return () -> result.stream().map(DRes::out).collect(Collectors.toList());
              });
          List<BigInteger> output = runApplication(app);

          Assert.assertEquals(openInputs, output);
        }
      };
    }
  }


  public static class TestSumAndMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigInteger> openInputs =
          Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
              .map(BigInteger::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.par(par -> {
            Numeric numeric = par.numeric();
            List<DRes<SInt>> result =
                openInputs.stream().map(numeric::known).collect(Collectors.toList());
            return () -> result;
          }).seq((seq, closed) -> {
            AdvancedNumeric advancedNumeric = seq.advancedNumeric();
            DRes<SInt> sum = advancedNumeric.sum(closed);
            DRes<SInt> mult = seq.numeric().mult(sum, sum);
            return seq.numeric().open(mult);
          });
          BigInteger output = runApplication(app);

          int sum = 0;
          for (BigInteger openInput : openInputs) {
            sum += openInput.intValue();
          }
          sum = sum * sum;
          Assert.assertEquals(sum, output.intValue());
        }
      };
    }
  }

  public static class TestSimpleMultAndAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger first = BigInteger.valueOf(10);
      BigInteger second = BigInteger.valueOf(5);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();

            DRes<SInt> firstClosed = numeric.known(first);
            DRes<SInt> secondClosed = numeric.known(second);

            DRes<SInt> add = numeric.add(firstClosed, secondClosed);
            DRes<SInt> mult = numeric.mult(firstClosed, add);

            return numeric.open(mult);
          };
          BigInteger output = runApplication(app);

          Assert.assertEquals(first.add(second).multiply(first), output);
        }
      };
    }
  }

  /**
   * Test a large amount (defined by the REPS constant) multiplication protocols in order to
   * stress-test the protocol suite.
   */
  public static class TestLotsMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger first = BigInteger.valueOf(10);
      BigInteger second = BigInteger.valueOf(5);
      final int repetitions = 20000;
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> producer.par(par -> {
                Numeric numeric = par.numeric();
                DRes<SInt> firstClosed = numeric.known(first);
                DRes<SInt> secondClosed = numeric.known(second);
                return Pair.lazy(firstClosed, secondClosed);
              }).par((par, pair) -> {
                DRes<SInt> firstClosed = pair.getFirst();
                DRes<SInt> secondClosed = pair.getSecond();
                Numeric numeric = par.numeric();
                ArrayList<DRes<SInt>> computations = new ArrayList<>();
                for (int i = 0; i < repetitions; i++) {
                  computations.add(numeric.mult(firstClosed, secondClosed));
                }
                return () -> computations;
              }).par((par, computations) -> {
                Numeric numeric = par.numeric();
                List<DRes<BigInteger>> opened =
                    computations.stream().map(numeric::open).collect(Collectors.toList());
                return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
              });
          List<BigInteger> output = runApplication(app);

          BigInteger multiply = first.multiply(second);
          Assert.assertThat(output.size(), Is.is(repetitions));
          for (BigInteger result : output) {
            Assert.assertEquals(multiply, result);
          }
        }
      };
    }
  }

  /**
   * Test a computation of doing a many multiplications and additions alternating between the two.
   * This should ensure batches with both types of protocols.
   */
  public static class TestAlternatingMultAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigInteger first = BigInteger.valueOf(10);
      BigInteger second = BigInteger.valueOf(5);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          final int numberOfComputations = 1000;
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> producer.par(par -> {
                Numeric numeric = par.numeric();
                DRes<SInt> firstClosed = numeric.known(first);
                DRes<SInt> secondClosed = numeric.known(second);
                return Pair.lazy(firstClosed, secondClosed);
              }).par((par, pair) -> {
                DRes<SInt> firstClosed = pair.getFirst();
                DRes<SInt> secondClosed = pair.getSecond();
                Numeric numeric1 = par.numeric();
                ArrayList<DRes<SInt>> computations = new ArrayList<>();
                for (int i = 0; i < numberOfComputations; i++) {
                  if (i % 2 == 0) {
                    computations.add(numeric1.mult(firstClosed, secondClosed));
                  } else {
                    computations.add(numeric1.add(firstClosed, secondClosed));
                  }

                }
                return () -> computations;
              }).seq((seq, computations) -> {
                Numeric numeric1 = seq.numeric();
                List<DRes<BigInteger>> opened =
                    computations.stream().map(numeric1::open).collect(Collectors.toList());
                return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
              });
          List<BigInteger> output = runApplication(app);

          BigInteger multiply = first.multiply(second);
          BigInteger add = first.add(second);
          Assert.assertThat(output.size(), Is.is(numberOfComputations));
          for (int i = 0; i < output.size(); i++) {
            BigInteger result = output.get(i);
            if (i % 2 == 0) {
              Assert.assertEquals(multiply, result);
            } else {
              Assert.assertEquals(add, result);
            }

          }
        }
      };
    }
  }

  private static void assertAllDifferent(List<BigInteger> elements) {
    for (int i = 0; i < elements.size(); i++) {
      for (int j = 0; j < elements.size(); j++) {
        if (i != j) {
          Assert.assertNotEquals(elements.get(i), elements.get(j));
        }
      }
    }
  }
}
