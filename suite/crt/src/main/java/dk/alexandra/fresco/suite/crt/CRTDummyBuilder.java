package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.CRTBasicProtocol;
import dk.alexandra.fresco.suite.crt.protocols.CRTBigIntegerProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticAddProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticCloseProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticKnownProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticMultProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticOpenProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticOpenToAllProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticRandomBitProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticRandomElementProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSubtractProtocol;
import java.math.BigInteger;
import java.util.Random;

public class CRTDummyBuilder
    implements BuilderFactoryNumeric {

  private final BuilderFactoryNumeric left, right;
  private final BasicNumericContext context;
  private final Random random;

  public CRTDummyBuilder(BuilderFactoryNumeric left, BuilderFactoryNumeric right) {
    BasicNumericContext leftContext = left.getBasicNumericContext();
    BasicNumericContext rightContext = right.getBasicNumericContext();
    this.left = left;
    this.right = right;
    this.random = new Random(0);

    this.context = new BasicNumericContext(
        leftContext.getMaxBitLength(),
        leftContext.getMyId(),
        leftContext.getNoOfParties(),
        new BigIntegerFieldDefinition(leftContext.getModulus().multiply(rightContext.getModulus())),
        32);
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return context;
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    return new Numeric() {

      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        return builder.par(par -> par.append(new CRTBasicProtocol<>(
            new DummyArithmeticAddProtocol(((CRTSInt) a.out()).getLeft(), ((CRTSInt) b.out()).getLeft()),
            new DummyArithmeticAddProtocol(((CRTSInt) a.out()).getRight(), ((CRTSInt) b.out()).getRight()))));
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return builder.seq(seq -> {
          Numeric numeric = createNumeric(seq);
          DRes<SInt> aSecret = numeric.known(a);
          return numeric.add(aSecret, b);
        });
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return builder.par(par -> par.append(new CRTBasicProtocol<>(
            new DummyArithmeticSubtractProtocol(((CRTSInt) a.out()).getLeft(), ((CRTSInt) b.out()).getLeft()),
            new DummyArithmeticSubtractProtocol(((CRTSInt) a.out()).getRight(), ((CRTSInt) b.out()).getRight()))));
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.seq(seq -> {
          Numeric numeric = createNumeric(seq);
          DRes<SInt> aSecret = numeric.known(a);
          return numeric.sub(aSecret, b);
        });
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return builder.seq(seq -> {
          Numeric numeric = createNumeric(seq);
          DRes<SInt> bSecret = numeric.known(b);
          return numeric.sub(a, bSecret);
        });
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.par(par -> par.append(new CRTBasicProtocol<>(
            new DummyArithmeticMultProtocol(((CRTSInt) a.out()).getLeft(), ((CRTSInt) b.out()).getLeft()),
            new DummyArithmeticMultProtocol(((CRTSInt) a.out()).getRight(), ((CRTSInt) b.out()).getRight()))));
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return builder.seq(seq -> {
          Numeric numeric = createNumeric(seq);
          DRes<SInt> aSecret = numeric.known(a);
          return numeric.mult(aSecret, b);
        });
      }

      @Override
      public DRes<SInt> randomBit() {
        BigInteger bit = BigInteger.valueOf(random.nextInt(2));
        return builder.par(par -> par.append(new CRTBasicProtocol<>(
            new DummyArithmeticKnownProtocol(bit),
            new DummyArithmeticKnownProtocol(bit))));
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.par(par -> par.append(new CRTBasicProtocol<>(
            new DummyArithmeticRandomElementProtocol(random),
            new DummyArithmeticRandomElementProtocol(random))));
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.par(par -> {
          Pair<BigInteger, BigInteger> crt = Util
              .mapToCRT(value, left.getBasicNumericContext().getModulus(),
                  right.getBasicNumericContext().getModulus());
          return par.append(new CRTBasicProtocol<>(
              new DummyArithmeticKnownProtocol(crt.getFirst()),
              new DummyArithmeticKnownProtocol(crt.getSecond())));
        });
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        return builder.par(par -> {
          Pair<BigInteger, BigInteger> crt = Util
              .mapToCRT(value, left.getBasicNumericContext().getModulus(),
                  right.getBasicNumericContext().getModulus());
          FieldElement openLeft = left.getBasicNumericContext().getFieldDefinition().createElement(crt.getFirst());
          FieldElement openRight = right.getBasicNumericContext().getFieldDefinition().createElement(crt.getSecond());
          return par.append(new CRTBasicProtocol<>(
              new DummyArithmeticCloseProtocol(openLeft, inputParty),
              new DummyArithmeticCloseProtocol(openRight, inputParty)));
        });
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return builder.par(par -> {
          CRTSInt crtsInt = (CRTSInt) secretShare.out();
          return par.append(new CRTBigIntegerProtocol<>(
              new DummyArithmeticOpenToAllProtocol(crtsInt.getLeft()),
              new DummyArithmeticOpenToAllProtocol(crtsInt.getRight()),
              left.getBasicNumericContext().getModulus(),
              right.getBasicNumericContext().getModulus()));
        });
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        return builder.par(par -> {
          CRTSInt crtsInt = (CRTSInt) secretShare.out();
          return par.append(new CRTBigIntegerProtocol<>(
              new DummyArithmeticOpenProtocol(crtsInt.getLeft(), outputParty),
              new DummyArithmeticOpenProtocol(crtsInt.getRight(), outputParty),
              left.getBasicNumericContext().getModulus(),
              right.getBasicNumericContext().getModulus()));
        });
      }
    };
  }
}
