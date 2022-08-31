package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.CRTSIntProtocol;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTBigIntegerProtocol;
import dk.alexandra.fresco.suite.crt.suites.ProtocolSuiteProtocolSupplier;
import java.math.BigInteger;

public class CRTBuilderFactory<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool>
    implements BuilderFactoryNumeric {

  private final BasicNumericContext context;
  private final ProtocolSuiteProtocolSupplier<ResourcePoolA> left;
  private final ProtocolSuiteProtocolSupplier<ResourcePoolB> right;
  private final BigInteger p, q;

  public CRTBuilderFactory(ResourcePoolA resourcePoolLeft,
      ProtocolSuiteProtocolSupplier<ResourcePoolA> leftPspp,
      ResourcePoolB resourcePoolRight,
      ProtocolSuiteProtocolSupplier<ResourcePoolB> rightPspp) {

    if (resourcePoolLeft.getMyId() != resourcePoolRight.getMyId()
        || resourcePoolLeft.getNoOfParties() != resourcePoolRight.getNoOfParties()) {
      throw new IllegalArgumentException(
          "The protocol suites used must be configured with the same ID and number of players");
    }

    this.left = leftPspp;
    this.right = rightPspp;
    this.p = resourcePoolLeft.getModulus();
    this.q = resourcePoolRight.getModulus();
    this.context = new CRTNumericContext(
        p.bitLength() + q.bitLength() - 40, //TODO
        resourcePoolLeft.getMyId(), resourcePoolLeft.getNoOfParties(), left, right, p, q);
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

        return builder.par(par -> {

          CRTSInt aOut = (CRTSInt) a.out();
          DRes<SInt> aLeft = aOut.getLeft();
          DRes<SInt> aRight = aOut.getRight();

          CRTSInt bOut = (CRTSInt) b.out();
          DRes<SInt> bLeft = bOut.getLeft();
          DRes<SInt> bRight = bOut.getRight();

          return par.append(new CRTSIntProtocol<>(
              left.add(aLeft, bLeft),
              right.add(aRight, bRight)));
        });
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
        return builder.par(par -> {

          CRTSInt aOut = (CRTSInt) a.out();
          DRes<SInt> aLeft = aOut.getLeft();
          DRes<SInt> aRight = aOut.getRight();

          CRTSInt bOut = (CRTSInt) b.out();
          DRes<SInt> bLeft = bOut.getLeft();
          DRes<SInt> bRight = bOut.getRight();

          return par.append(new CRTSIntProtocol<>(
              left.sub(aLeft, bLeft), right.sub(aRight, bRight)));
        });
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
        return builder.par(par -> {
          CRTSInt aOut = (CRTSInt) a.out();
          DRes<SInt> aLeft = aOut.getLeft();
          DRes<SInt> aRight = aOut.getRight();

          CRTSInt bOut = (CRTSInt) b.out();
          DRes<SInt> bLeft = bOut.getLeft();
          DRes<SInt> bRight = bOut.getRight();

          return par.append(new CRTSIntProtocol<>(
              left.mult(aLeft, bLeft), right.mult(aRight, bRight)));
        });
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
        return builder.seq(seq -> builder.numeric().known(0)); // TODO
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.par(par -> par.append(new CRTSIntProtocol<>(
            left.randomElement(), right.randomElement())));
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.par(par -> {
          Pair<BigInteger, BigInteger> crt = mapToCRT(value);
          return par.append(new CRTSIntProtocol<>(
              left.known(crt.getFirst()), right.known(crt.getSecond())));
        });
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        return builder.par(par -> {
          Pair<BigInteger, BigInteger> crt = mapToCRT(value);
          return par.append(new CRTSIntProtocol<>(
              left.input(crt.getFirst(), inputParty), right.input(crt.getSecond(), inputParty)));
        });
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return builder.par(par -> {
          CRTSInt crtsInt = (CRTSInt) secretShare.out();
          return par.append(new CRTBigIntegerProtocol<>(
              left.open(crtsInt.getLeft()),
              right.open(crtsInt.getRight()),
              p, q));
        });
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        return builder.par(par -> {
          CRTSInt crtsInt = (CRTSInt) secretShare.out();
          return par.append(new CRTBigIntegerProtocol<>(
              left.open(crtsInt.getLeft(), outputParty),
              right.open(crtsInt.getRight(), outputParty),
              p, q));
        });
      }
    };
  }

  public Pair<BigInteger, BigInteger> mapToCRT(BigInteger x) {
    return Util.mapToCRT(x, p, q);
  }

}
