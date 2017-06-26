package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.InputBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.gt.GreaterThanReducerProtocol4;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocol4;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactoryImpl;
import java.math.BigInteger;

public class DefaultComparisonBuilder implements ComparisonBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilder builder;

  public DefaultComparisonBuilder(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilder builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  private GreaterThanReducerProtocol4 getGreaterThanProtocol(
      Computation<SInt> left,
      Computation<SInt> right,
      boolean longCompare) {
    int bitLength = factoryNumeric.getBasicNumericFactory().getMaxBitLength();
    if (longCompare) {
      bitLength *= 2;
    }
    BasicNumericFactory bnf = factoryNumeric.getBasicNumericFactory();
    NumericNegateBitFactoryImpl numericNegateBitFactory = new NumericNegateBitFactoryImpl(bnf);
    ZeroTestProtocolFactoryImpl zeroTestProtocolFactory = new ZeroTestProtocolFactoryImpl(bnf,
        factoryNumeric.getExpFromOInt(), numericNegateBitFactory,
        factoryNumeric.getPreprocessedExpPipe());
    return new GreaterThanReducerProtocol4(
        bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
        left, right,
        factoryNumeric);
  }

  @Override
  public Computation<SInt> compareLong(Computation<SInt> left, Computation<SInt> right) {
    GreaterThanReducerProtocol4 greaterThanProtocol = getGreaterThanProtocol(left, right,
        true);
    return builder.createSequentialSub(greaterThanProtocol);

  }

  @Override
  public Computation<SInt> compare(Computation<SInt> left, Computation<SInt> right) {
    GreaterThanReducerProtocol4 greaterThanProtocol = getGreaterThanProtocol(left, right,
        false);
    return builder.createSequentialSub(greaterThanProtocol);
  }

  public Computation<SInt> sign(Computation<SInt> x) {
    BasicNumericFactory bnf = factoryNumeric.getBasicNumericFactory();
    InputBuilder input = builder.createInputBuilder();
    Computation<SInt> compare = compare(
        input.known(BigInteger.valueOf(0)), x);
    OInt oInt = bnf.getOInt(BigInteger.valueOf(2));
    NumericBuilder numericBuilder = builder.numeric();
    Computation<SInt> twice = numericBuilder.mult(
        input.known(oInt.getValue()), compare);
    return numericBuilder.sub(twice, input.known(BigInteger.valueOf(1)));
  }

  @Override
  public Computation<SInt> compareZero(Computation<SInt> x, int bitLength) {
    return builder.createSequentialSub(
        new ZeroTestProtocol4(factoryNumeric, bitLength,
            x));
  }

}
