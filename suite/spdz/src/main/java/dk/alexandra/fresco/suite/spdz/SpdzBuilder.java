package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Conversion;
import dk.alexandra.fresco.framework.builder.numeric.DefaultAdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultLogical;
import dk.alexandra.fresco.framework.builder.numeric.Logical;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.PreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.BigIntegerOIntArithmetic;
import dk.alexandra.fresco.framework.value.BigIntegerOIntFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAndBatchedProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputTwoPartyProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputSingleProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzRandomProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownRight;
import dk.alexandra.fresco.suite.spdz.gates.SpdzTruncationPairProtocol;
import java.math.BigInteger;
import java.util.List;

/**
 * Basic native builder for the SPDZ protocol suite.
 */
class SpdzBuilder implements BuilderFactoryNumeric {

  private BasicNumericContext basicNumericContext;
  private MiscBigIntegerGenerators miscOIntGenerators;
  private RealNumericContext realNumericContext;
  private final OIntFactory oIntFactory;
  private final OIntArithmetic oIntArithmetic;

  SpdzBuilder(BasicNumericContext basicNumericContext, RealNumericContext realNumericContext) {
    this.basicNumericContext = basicNumericContext;
    this.realNumericContext = realNumericContext;
    this.oIntFactory = new BigIntegerOIntFactory(basicNumericContext.getMaxBitLength());
    this.oIntArithmetic = new BigIntegerOIntArithmetic(oIntFactory);
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return basicNumericContext;
  }

  @Override
  public RealNumericContext getRealNumericContext() {
    return realNumericContext;
  }

  @Override
  public PreprocessedValues createPreprocessedValues(ProtocolBuilderNumeric protocolBuilder) {
    return pipeLength -> {
      SpdzExponentiationPipeProtocol spdzExpPipeProtocol =
          new SpdzExponentiationPipeProtocol(pipeLength);
      return protocolBuilder.append(spdzExpPipeProtocol);
    };
  }

  @Override
  public AdvancedNumeric createAdvancedNumeric(ProtocolBuilderNumeric builder) {
    return new SpdzAdvancedNumeric(this, builder);
  }

  @Override
  public Logical createLogical(ProtocolBuilderNumeric builder) {
    return new SpdzLogical(builder);
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric protocolBuilder) {
    return new Numeric() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        SpdzAddProtocol spdzAddProtocol = new SpdzAddProtocol(a, b);
        return protocolBuilder.append(spdzAddProtocol);
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        SpdzAddProtocolKnownLeft spdzAddProtocolKnownLeft = new SpdzAddProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzAddProtocolKnownLeft);
      }

      @Override
      public DRes<SInt> addOpen(DRes<OInt> a, DRes<SInt> b) {
        return add(protocolBuilder.getOIntFactory().toBigInteger(a.out()), b);
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        SpdzSubtractProtocol spdzSubtractProtocol = new SpdzSubtractProtocol(a, b);
        return protocolBuilder.append(spdzSubtractProtocol);
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        SpdzSubtractProtocolKnownLeft spdzSubtractProtocolKnownLeft =
            new SpdzSubtractProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzSubtractProtocolKnownLeft);
      }

      @Override
      public DRes<SInt> subFromOpen(DRes<OInt> a, DRes<SInt> b) {
        return sub(protocolBuilder.getOIntFactory().toBigInteger(a.out()), b);
      }

      @Override
      public DRes<SInt> subOpen(DRes<SInt> a, DRes<OInt> b) {
        return sub(a, protocolBuilder.getOIntFactory().toBigInteger(b.out()));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        SpdzSubtractProtocolKnownRight spdzSubtractProtocolKnownRight =
            new SpdzSubtractProtocolKnownRight(a, b);
        return protocolBuilder.append(spdzSubtractProtocolKnownRight);
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        SpdzMultProtocol spdzMultProtocol = new SpdzMultProtocol(a, b);
        return protocolBuilder.append(spdzMultProtocol);
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        SpdzMultProtocolKnownLeft spdzMultProtocol4 = new SpdzMultProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzMultProtocol4);

      }

      @Override
      public DRes<SInt> multByOpen(DRes<OInt> a, DRes<SInt> b) {
        return mult(protocolBuilder.getOIntFactory().toBigInteger(a.out()), b);
      }

      @Override
      public DRes<SInt> randomBit() {
        return protocolBuilder.append(new SpdzRandomBitProtocol());
      }

      @Override
      public DRes<SInt> randomElement() {
        return protocolBuilder.append(new SpdzRandomProtocol());
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return protocolBuilder.append(new SpdzKnownSIntProtocol(value));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        if (protocolBuilder.getBasicNumericContext().getNoOfParties() == 2) {
          return protocolBuilder.append(new SpdzInputTwoPartyProtocol(value, inputParty));
        } else {
          return protocolBuilder.append(new SpdzInputProtocol(value, inputParty));
        }
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        SpdzOutputToAllProtocol openProtocol = new SpdzOutputToAllProtocol(secretShare);
        return protocolBuilder.append(openProtocol);
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        SpdzOutputSingleProtocol openProtocol = new SpdzOutputSingleProtocol(secretShare,
            outputParty);
        return protocolBuilder.append(openProtocol);
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare) {
        DRes<BigInteger> value = open(secretShare);
        return () -> oIntFactory.fromBigInteger(value.out());
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare, int outputParty) {
        DRes<BigInteger> out = open(secretShare, outputParty);
        return () -> {
          BigInteger res = out.out();
          if (res == null) {
            return null;
          } else {
            return oIntFactory.fromBigInteger(res);
          }
        };
      }

    };
  }

  @Override
  public Conversion createConversion(ProtocolBuilderNumeric builder) {
    throw new UnsupportedOperationException(
        "This protocol suite does not currently support conversion");
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    if (miscOIntGenerators == null) {
      miscOIntGenerators = new MiscBigIntegerGenerators(basicNumericContext.getModulus());
    }
    return miscOIntGenerators;
  }

  @Override
  public OIntFactory getOIntFactory() {
    return oIntFactory;
  }

  @Override
  public OIntArithmetic getOIntArithmetic() {
    return oIntArithmetic;
  }

  class SpdzLogical extends DefaultLogical {

    protected SpdzLogical(
        ProtocolBuilderNumeric builder) {
      super(builder);
    }

    @Override
    public DRes<List<DRes<SInt>>> pairWiseAnd(DRes<List<DRes<SInt>>> bitsA,
        DRes<List<DRes<SInt>>> bitsB) {
      return builder.append(new SpdzAndBatchedProtocol(bitsA, bitsB));
    }
  }

  class SpdzAdvancedNumeric extends DefaultAdvancedNumeric {

    protected SpdzAdvancedNumeric(
        BuilderFactoryNumeric factoryNumeric,
        ProtocolBuilderNumeric builder) {
      super(factoryNumeric, builder);
    }

    @Override
    public DRes<TruncationPair> generateTruncationPair(int d) {
      return builder.append(new SpdzTruncationPairProtocol(d));
    }
  }

}
