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
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.CorrelatedNoiseProtocol;
import dk.alexandra.fresco.suite.crt.protocols.LiftPQProtocol;
import dk.alexandra.fresco.suite.crt.protocols.LiftQPProtocol;
import dk.alexandra.fresco.suite.crt.protocols.MaskAndOpenComputation;
import dk.alexandra.fresco.suite.crt.protocols.MixedAddProtocol;
import dk.alexandra.fresco.suite.crt.protocols.Projection;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import dk.alexandra.fresco.suite.crt.protocols.Truncp;
import java.math.BigInteger;
import java.util.Random;
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
          Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getP(), ring.getQ());

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
              new MixedAddProtocol((CRTSInt) x).buildComputation(seq));
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
          Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getP(), ring.getQ());

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
            BigInteger toConvert = Util.mapToBigInteger(x, y, ring.getP(), ring.getQ());
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
            BigInteger toConvert = Util.mapToBigInteger(x, y, ring.getP(), ring.getQ());
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
//            BigInteger toConvert = ring.getQ().multiply(value)
//                .multiply(ring.getQ().modInverse(ring.getP()));
            BigInteger toConvert = Util.mapToBigInteger(value, BigInteger.ZERO, ring.getP(), ring.getQ());

            return new LiftPQProtocol(seq.numeric().known(toConvert)).buildComputation(seq);
          }).seq((seq, r) -> seq.numeric().open(r));
          BigInteger output = runApplication(app);

          CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
          Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getP(), ring.getQ());

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

            BigInteger toConvert = Util.mapToBigInteger(BigInteger.ZERO, value, ring.getP(), ring.getQ());
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
}
