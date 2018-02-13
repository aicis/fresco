package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinAddProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinInputProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinKnownSIntProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinMultiplyProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinOutputProtocol;
import java.math.BigInteger;

public class MarlinBuilder<T extends BigUInt<T>> implements BuilderFactoryNumeric {

  private final BigUIntFactory<T> factory;
  private final BasicNumericContext numericContext;

  MarlinBuilder(BigUIntFactory<T> factory, BasicNumericContext numericContext) {
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
        return builder.append(new MarlinAddProtocol<T>(a, b));
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return null;
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return null;
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return null;
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return null;
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MarlinMultiplyProtocol<T>(a, b));
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return null;
      }

      @Override
      public DRes<SInt> randomBit() {
        return null;
      }

      @Override
      public DRes<SInt> randomElement() {
        return null;
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.append(new MarlinKnownSIntProtocol<>(factory.createFromBigInteger(value)));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        return builder.append(new MarlinInputProtocol<>(
            factory.createFromBigInteger(value), inputParty));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return builder.append(new MarlinOutputProtocol<T>(secretShare));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        return null;
      }
    };
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    throw new UnsupportedOperationException();
  }

}
