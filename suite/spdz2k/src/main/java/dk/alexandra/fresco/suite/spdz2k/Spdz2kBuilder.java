package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
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
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kMultiplyProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOutputSinglePartyProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOutputToAllProtocol;
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

  public Spdz2kBuilder(CompUIntFactory<PlainT> factory, BasicNumericContext numericContext) {
    this.factory = factory;
    this.numericContext = numericContext;
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return numericContext;
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    return new Numeric() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        return () -> factory.toSpdz2kSInt(a).add(factory.toSpdz2kSInt(b));
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
        return () -> (factory.toSpdz2kSInt(a)).subtract(factory.toSpdz2kSInt(b));
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.append(
            new Spdz2kSubtractFromKnownProtocol<>(factory.createFromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> subFromOpen(DRes<OInt> a, DRes<SInt> b) {
        // TODO should call .out inside evaluate instead
        return builder.append(
            new Spdz2kSubtractFromKnownProtocol<>(factory.fromOInt(a), b));
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
        return () -> factory.toSpdz2kSInt(b).multiply(factory.createFromBigInteger(a));
      }

      @Override
      public DRes<SInt> multByOpen(DRes<OInt> a, DRes<SInt> b) {
        return () -> factory.toSpdz2kSInt(b).multiply(factory.fromOInt(a));
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
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return builder.append(new Spdz2kOutputToAllProtocol<>(secretShare));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        return builder.append(new Spdz2kOutputSinglePartyProtocol<>(secretShare, outputParty));
      }
    };
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
    return new CompUIntArithmetic<>(factory);
  }

  @Override
  public RealNumericContext getRealNumericContext() {
    // TODO Auto-generated method stub
    return null;
  }

}
