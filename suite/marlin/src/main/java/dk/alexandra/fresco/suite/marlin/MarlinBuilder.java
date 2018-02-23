package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.protocols.computations.MarlinInputComputation;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinAddKnownProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinKnownSIntProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinMultiplyProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinOutputToAllProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinOutputSinglePartyProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinRandomBitProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinRandomElementProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinSubtractFromKnownProtocol;
import java.math.BigInteger;

public class MarlinBuilder<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> implements
    BuilderFactoryNumeric {

  private final CompUIntFactory<T> factory;
  private final BasicNumericContext numericContext;

  public MarlinBuilder(CompUIntFactory<T> factory, BasicNumericContext numericContext) {
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
        return () -> ((MarlinSInt<T>) a.out()).add((MarlinSInt<T>) b.out());
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return builder.append(new MarlinAddKnownProtocol<>(factory.createFromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return () -> ((MarlinSInt<T>) a.out()).subtract((MarlinSInt<T>) b.out());
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.append(
            new MarlinSubtractFromKnownProtocol<>(factory.createFromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return builder.append(
            new MarlinAddKnownProtocol<>(factory.createFromBigInteger(b).negate(), a));
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MarlinMultiplyProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return () -> ((MarlinSInt<T>) b.out()).multiply(factory.createFromBigInteger(a));
      }

      @Override
      public DRes<SInt> randomBit() {
        return builder.append(new MarlinRandomBitProtocol<>());
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.append(new MarlinRandomElementProtocol<>());
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.append(new MarlinKnownSIntProtocol<>(factory.createFromBigInteger(value)));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        return builder.seq(
            new MarlinInputComputation<>(factory.createFromBigInteger(value), inputParty)
        );
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return builder.append(new MarlinOutputToAllProtocol<>(secretShare));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        return builder.append(new MarlinOutputSinglePartyProtocol<>(secretShare, outputParty));
      }
    };
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    throw new UnsupportedOperationException();
  }

}
