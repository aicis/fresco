package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;

public class MarlinBuilder implements BuilderFactoryNumeric {

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return null;
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    return new Numeric() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        return null;
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
        return null;
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
        return null;
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        return null;
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return null;
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
