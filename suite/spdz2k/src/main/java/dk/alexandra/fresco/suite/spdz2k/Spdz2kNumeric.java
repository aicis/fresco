package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
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
import java.util.Objects;

public class Spdz2kNumeric<PlainT extends CompUInt<?, ?, PlainT>> implements Numeric {

  private final ProtocolBuilderNumeric builder;
  private final CompUIntFactory<PlainT> factory;

  Spdz2kNumeric(ProtocolBuilderNumeric builder, CompUIntFactory<PlainT> factory) {
    this.builder = builder;
    this.factory = factory;
  }

  @Override
  public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
    return () -> toSpdz2kSInt(a).add(toSpdz2kSInt(b));
  }

  @Override
  public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
    return builder.append(new Spdz2kAddKnownProtocol<>(factory.createFromBigInteger(a), b));
  }

  @Override
  public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
    return () -> (toSpdz2kSInt(a)).subtract(toSpdz2kSInt(b));
  }

  @Override
  public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
    return builder.append(
        new Spdz2kSubtractFromKnownProtocol<>(factory.createFromBigInteger(a), b));
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
    return () -> toSpdz2kSInt(b).multiply(factory.createFromBigInteger(a));
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

  /**
   * Get result from deferred and downcast result to {@link Spdz2kSInt <PlainT>}.
   */
  private Spdz2kSInt<PlainT> toSpdz2kSInt(DRes<SInt> value) {
    return Objects.requireNonNull((Spdz2kSInt<PlainT>) value.out());
  }

}
