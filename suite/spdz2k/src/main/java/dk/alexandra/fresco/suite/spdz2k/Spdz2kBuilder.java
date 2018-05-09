package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Conversion;
import dk.alexandra.fresco.framework.builder.numeric.DefaultLogical;
import dk.alexandra.fresco.framework.builder.numeric.Logical;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.Spdz2kInputComputation;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kAddKnownProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kMultKnownProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kMultiplyProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOutputSinglePartyProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOutputToAll;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kRandomBitProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kRandomElementProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kSubtractFromKnownProtocol;
import java.math.BigInteger;

/**
 * Basic native builder for the SPDZ2k protocol suite.
 *
 * @param <PlainT> the type representing open values
 */
public class Spdz2kBuilder<PlainT extends CompUInt<?, ?, PlainT>> implements
    BuilderFactoryNumeric {

  private final CompUIntFactory<PlainT> factory;
  private final BasicNumericContext numericContext;
  private final boolean useBooleanMode;
  private final CompUIntArithmetic<PlainT> uIntArithmetic;

  public Spdz2kBuilder(CompUIntFactory<PlainT> factory, BasicNumericContext numericContext,
      boolean useBooleanMode) {
    this.factory = factory;
    this.uIntArithmetic = new CompUIntArithmetic<>(factory);
    this.numericContext = numericContext;
    this.useBooleanMode = useBooleanMode;
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return numericContext;
  }

  @Override
  public Comparison createComparison(ProtocolBuilderNumeric builder) {
    if (!useBooleanMode) {
      throw new IllegalStateException(
          "Need to enable boolean mode to perform comparisons in spdz2k");
    } else {
      return new Spdz2kComparison<>(this, builder, factory);
    }
  }

  @Override
  public Logical createLogical(ProtocolBuilderNumeric builder) {
    if (useBooleanMode) {
      return new Spdz2kLogicalBooleanMode<>(builder, factory);
    } else {
      return new Spdz2kLogical(builder);
    }
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    return new Numeric() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        return () -> factory.toSpdz2kSIntArithmetic(a).add(factory.toSpdz2kSIntArithmetic(b));
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return builder.append(new Spdz2kAddKnownProtocol<>(factory.createFromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> addOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new Spdz2kAddKnownProtocol<>(factory.fromOInt(a), b));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return () -> (factory.toSpdz2kSIntArithmetic(a))
            .subtract(factory.toSpdz2kSIntArithmetic(b));
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.append(
            new Spdz2kSubtractFromKnownProtocol<>(factory.createFromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> subFromOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new Spdz2kSubtractFromKnownProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> subOpen(DRes<SInt> a, DRes<OInt> b) {
        return builder.append(
            new Spdz2kAddKnownProtocol<>(factory.fromOInt(b).negate(), a));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return builder.append(
            new Spdz2kAddKnownProtocol<>(factory.createFromBigInteger(b).negate(), a));
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new Spdz2kMultiplyProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return multByOpen(factory.createFromBigInteger(a), b);
      }

      @Override
      public DRes<SInt> multByOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new Spdz2kMultKnownProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> randomBit() {
        return builder.append(new Spdz2kRandomBitProtocol<>());
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.append(new Spdz2kRandomElementProtocol<>());
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.append(new Spdz2kKnownSIntProtocol<>(factory.createFromBigInteger(value)));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        return builder.seq(
            new Spdz2kInputComputation<>(factory.createFromBigInteger(value), inputParty)
        );
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare) {
        return builder.append(new Spdz2kOutputToAll<>(secretShare));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        DRes<OInt> out = openAsOInt(secretShare);
        return () -> factory.fromOInt(out).toBigInteger();
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare, int outputParty) {
        return builder.append(new Spdz2kOutputSinglePartyProtocol<>(secretShare, outputParty));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        DRes<OInt> out = openAsOInt(secretShare, outputParty);
        return () -> {
          OInt res = out.out();
          if (res == null) {
            return null;
          } else {
            return factory.fromOInt(out).toBigInteger();
          }
        };
      }
    };
  }

  @Override
  public Conversion createConversion(ProtocolBuilderNumeric builder) {
    return new Spdz2kConversion(builder);
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OIntFactory getOIntFactory() {
    return factory;
  }

  @Override
  public OIntArithmetic getOIntArithmetic() {
    return uIntArithmetic;
  }

  @Override
  public RealNumericContext getRealNumericContext() {
    // TODO Auto-generated method stub
    return null;
  }

  class Spdz2kLogical extends DefaultLogical {

    Spdz2kLogical(
        ProtocolBuilderNumeric builder) {
      super(builder);
    }
  }

}
