package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.protocols.BitDecompositionProtocol;
import dk.alexandra.fresco.suite.crt.protocols.CorrelatedNoiseProtocol;
import dk.alexandra.fresco.suite.crt.protocols.DivisionProtocol;
import dk.alexandra.fresco.suite.crt.protocols.LEQProtocol;
import dk.alexandra.fresco.suite.crt.protocols.LiftPQProtocol;
import dk.alexandra.fresco.suite.crt.protocols.LiftQPProtocol;
import dk.alexandra.fresco.suite.crt.protocols.MaskAndOpenComputation;
import dk.alexandra.fresco.suite.crt.protocols.DummyMixedAddProtocol;
import dk.alexandra.fresco.suite.crt.protocols.Projection;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import dk.alexandra.fresco.suite.crt.protocols.Truncp;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Assert;

public class BasicCRTTests {

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

  public static class TestCorrelatedNoise<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer
              .seq(seq -> seq.append(
                  new CorrelatedNoiseProtocol<>())).seq((seq, r) -> seq.numeric().open(r));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Pair<BigInteger, BigInteger> crt = ring.mapToCRT(output);

          Assert.assertEquals(crt.getFirst(), crt.getSecond().mod(ring.getP()));
        }
      };
    }
  }

  public static class TestMixedAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          BigInteger x1 = BigInteger.valueOf(7);
          BigInteger x2 = BigInteger.valueOf(11);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
            CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
            return seq.numeric().known(Util.mapToBigInteger(x1, x2, ring.getP(), ring.getQ()));
          }).seq((seq, x) ->
              new DummyMixedAddProtocol(x).buildComputation(seq));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();

          Assert.assertEquals(x1.add(x2), output.mod(ring.getP()));
        }
      };
    }
  }

  public static class TestMaskAndOpen<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          BigInteger value = BigInteger.valueOf(7);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer
              .seq(seq -> seq.numeric().known(value)).seq((seq, x) ->
                  new MaskAndOpenComputation(x).buildComputation(seq));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Pair<BigInteger, BigInteger> crt = ring.mapToCRT(output);

          Assert.assertEquals(crt.getFirst(), crt.getSecond().mod(ring.getP()));
        }
      };
    }
  }

  public static class TestProjectionLeft<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          BigInteger x = BigInteger.valueOf(7);
          BigInteger y = BigInteger.valueOf(11);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
            CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
            BigInteger toConvert = ring.mapToBigInteger(x, y);
            return new Projection(seq.numeric().known(toConvert), Coordinate.LEFT)
                .buildComputation(seq);
          }).seq((seq, r) -> seq.numeric().open(r));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getP(), ring.getQ());

          Assert.assertEquals(x, crt.getFirst());
          Assert.assertEquals(BigInteger.ZERO, crt.getSecond());
        }
      };
    }
  }

  public static class TestProjectionRight<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          BigInteger x = BigInteger.valueOf(7);
          BigInteger y = BigInteger.valueOf(11);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
            CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
            BigInteger toConvert = ring.mapToBigInteger(x, y);
            return new Projection(seq.numeric().known(toConvert), Coordinate.RIGHT)
                .buildComputation(seq);
          }).seq((seq, r) -> seq.numeric().open(r));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getP(), ring.getQ());

          Assert.assertEquals(BigInteger.ZERO, crt.getFirst());
          Assert.assertEquals(y, crt.getSecond());
        }
      };
    }
  }

  public static class TestLiftPQ<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          BigInteger value = BigInteger.valueOf(1234);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
            CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
            BigInteger toConvert = ring.mapToBigInteger(value, BigInteger.ZERO);

            return new LiftPQProtocol(seq.numeric().known(toConvert)).buildComputation(seq);
          }).seq((seq, r) -> seq.numeric().open(r));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Pair<BigInteger, BigInteger> crt = ring.mapToCRT(output);

          Assert.assertEquals(value, crt.getSecond());
        }
      };
    }
  }

  public static class TestLiftQP<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          BigInteger value = BigInteger.valueOf(1234);
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
            CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
            BigInteger toConvert = ring.mapToBigInteger(BigInteger.ZERO, value);
            return new LiftQPProtocol(seq.numeric().known(toConvert)).buildComputation(seq);

          }).seq((seq, r) -> seq.numeric().open(r));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getP(), ring.getQ());

          Assert.assertEquals(value, crt.getFirst());
        }
      };
    }
  }

  public static class TestTruncp<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          BigInteger value = new BigInteger(84, new Random(1234));
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
            DRes<SInt> x = seq.numeric().known(value);
            DRes<SInt> y = new Truncp(x).buildComputation(seq);
            return seq.numeric().open(y);
          });
          BigInteger output = runApplication(app);
          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Assert.assertEquals(value.divide(ring.getP()), output);
        }
      };
    }
  }

  public static class TestDivision<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Random random = new Random(1234);
          BigInteger value = new BigInteger(60, random);
          BigInteger divisor = new BigInteger(20, random);

          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
            DRes<SInt> x = seq.numeric().known(value);
            DRes<SInt> y = new DivisionProtocol(x, divisor).buildComputation(seq);
            return seq.numeric().open(y);
          });
          BigInteger output = runApplication(app);
          Assert.assertEquals(value.divide(divisor), output);
        }
      };
    }
  }

  public static class TestLEQ<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {

          Random random = new Random(1234);
          int N = 100;

          for (int i = 0; i < N; i++) {
            BigInteger yValue = new BigInteger(127, random);
            BigInteger xValue = new BigInteger(127, random);

            Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
              DRes<SInt> x = seq.numeric().known(xValue);
              DRes<SInt> y = seq.numeric().known(yValue);
              return seq.numeric().open(new LEQProtocol(x, y).buildComputation(seq));
            });
            BigInteger output = runApplication(app);
            BigInteger expected = xValue.compareTo(yValue) <= 0 ? BigInteger.ONE : BigInteger.ZERO;
            Assert.assertEquals(expected, output);
          }
        }
      };
    }
  }

  public static class TestBitDecomposition<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {

          Random random = new Random(1234);
          int N = 1;

          for (int i = 0; i < N; i++) {
            BigInteger xValue = new BigInteger(63, random);

            Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
              DRes<SInt> x = seq.numeric().known(xValue);
              return new BitDecompositionProtocol(x, 64).buildComputation(seq);
            }).seq((seq, bits) -> DRes.of(bits.stream().map(seq.numeric()::open).collect(Collectors.toList()))).seq((seq, bits) -> DRes.of(bits.stream().map(DRes::out).collect(Collectors.toList())));
            List<BigInteger> output = runApplication(app);
            for (int j = 0; j < 64; j++) {
              boolean bj = xValue.testBit(j);
              BigInteger expected = bj ? BigInteger.ONE : BigInteger.ZERO;
              Assert.assertEquals(expected, output.get(j));
            }

          }
        }
      };
    }
  }

}