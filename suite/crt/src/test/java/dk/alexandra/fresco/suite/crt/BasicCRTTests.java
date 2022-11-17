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
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.suite.crt.fixed.CRTFixedNumeric;
import dk.alexandra.fresco.suite.crt.protocols.*;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import org.junit.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                                    new CorrelatedNoiseProtocol<>(seq))).seq((seq, r) -> seq.numeric().open(r));
                    BigInteger output = runApplication(app);

                    CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
                    Pair<BigInteger, BigInteger> crt = ring.integerToRNS(output);

                    System.out.println(crt.getFirst() + ", " + crt.getSecond());

                    Assert.assertEquals(crt.getFirst(), crt.getSecond().mod(ring.getLeftModulus()));
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
                        BigInteger toConvert = ring.RNStoBigInteger(x, y);
                        return new Projection<>(seq.numeric().known(toConvert), Coordinate.LEFT)
                                .buildComputation(seq);
                    }).seq((seq, r) -> seq.numeric().open(r));
                    BigInteger output = runApplication(app);

                    CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
                    Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getLeftModulus(), ring.getRightModulus());

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
                        BigInteger toConvert = ring.RNStoBigInteger(x, y);
                        return new Projection<>(seq.numeric().known(toConvert), Coordinate.RIGHT)
                                .buildComputation(seq);
                    }).seq((seq, r) -> seq.numeric().open(r));
                    BigInteger output = runApplication(app);

                    CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
                    Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getLeftModulus(), ring.getRightModulus());

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
                    CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
                    BigInteger value = new BigInteger("12345");
                    Application<BigInteger, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
                        BigInteger toConvert = ring.RNStoBigInteger(value, BigInteger.ZERO);
                        return new LiftPQProtocol<>(seq.numeric().known(toConvert)).buildComputation(seq);
                    }).seq((seq, r) -> seq.numeric().open(r));
                    BigInteger output = runApplication(app);
                    Assert.assertEquals(value, output.mod(ring.getLeftModulus()));
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
                        BigInteger toConvert = ring.RNStoBigInteger(BigInteger.ZERO, value);
                        return new LiftQPProtocol<>(seq.numeric().known(toConvert)).buildComputation(seq);

                    }).seq((seq, r) -> seq.numeric().open(r));
                    BigInteger output = runApplication(app);

                    CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
                    Pair<BigInteger, BigInteger> crt = Util.mapToCRT(output, ring.getLeftModulus(), ring.getRightModulus());

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
                        DRes<SInt> y = new Truncp<>(x).buildComputation(seq);
                        return seq.numeric().open(y);
                    });
                    BigInteger output = runApplication(app);
                    CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
                    Assert.assertTrue(output.subtract(value.divide(ring.getLeftModulus())).compareTo(BigInteger.valueOf(this.conf.getResourcePool().getNoOfParties())) <= 0);
                }
            };
        }
    }

    public static class TestFixedPointInput<ResourcePoolT extends ResourcePool>
            extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

        @Override
        public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

                @Override
                public void test() {

                    double input = Math.PI;

                    Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
                        FixedNumeric numeric = new CRTFixedNumeric(seq);
                        DRes<SFixed> x = numeric.input(input, 1);
                        return numeric.open(x);
                    });
                    BigDecimal output = runApplication(app);
                    Assert.assertEquals(input, output.doubleValue(), 0.001);

                }
            };
        }
    }

    public static class TestFixedPointMultiplication<ResourcePoolT extends ResourcePool>
            extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

        @Override
        public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

                @Override
                public void test() {

                    double a = Math.PI;
                    double b = 7.001;

                    Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
                        FixedNumeric numeric = new CRTFixedNumeric(seq);
                        DRes<SFixed> x = numeric.input(a, 1);
                        DRes<SFixed> y = numeric.input(b, 2);
                        return numeric.open(numeric.mult(x, y));
                    });
                    BigDecimal output = runApplication(app);
                    Assert.assertEquals(a * b, output.doubleValue(), 0.001);

                }
            };
        }
    }

    public static class TestFixedPointDivision<ResourcePoolT extends ResourcePool>
            extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

        @Override
        public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

                @Override
                public void test() {

                    double a = 7.001;
                    double b = Math.PI;

                    Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
                        FixedNumeric numeric = new CRTFixedNumeric(seq);
                        DRes<SFixed> y = numeric.input(a, 2);
                        return numeric.open(numeric.div(y, b));
                    });
                    BigDecimal output = runApplication(app);
                    Assert.assertEquals(a / b, output.doubleValue(), 0.001);

                }
            };
        }
    }

    public static class TestFixedPointSecretDivision<ResourcePoolT extends ResourcePool>
            extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

        @Override
        public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

                @Override
                public void test() {

                    double a = 17.90 + Math.PI;
                    double b = 0.2;

                    Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
                        FixedNumeric numeric = new CRTFixedNumeric(seq);
                        DRes<SFixed> x = numeric.input(a, 1);
                        DRes<SFixed> y = numeric.input(b, 2);
                        return numeric.open(numeric.div(x, y));
                    });
                    BigDecimal output = runApplication(app);
                    Assert.assertEquals(a / b, output.doubleValue(), 0.01);
                }
            };
        }
    }


    public static class TestRandomModP<ResourcePoolT extends ResourcePool>
            extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

        @Override
        public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

                @Override
                public void test() {

                    int n = 100;

                    Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> producer.seq(seq -> {
                        List<DRes<BigInteger>> output = Stream.generate(() -> seq.seq(new RandomModP<>())).limit(n).map(seq.numeric()::open).collect(
                                Collectors.toList());
                        return DRes.of(output);
                    }).seq((seq, output) -> DRes.of(output.stream().map(DRes::out).collect(Collectors.toList())));
                    List<BigInteger> output = runApplication(app);

                    CRTRingDefinition ring = (CRTRingDefinition) this.getFieldDefinition();
                    for (BigInteger r : output) {
                        if (r.compareTo(ring.getLeftModulus()) >= 0) {
                            Assert.fail();
                        }
                    }
                }
            };
        }
    }
}
