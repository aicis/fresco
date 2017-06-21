package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.gt.GreaterThanReducerProtocol4;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;
import java.math.BigInteger;

public class DefaultComparisonBuilder implements ComparisonBuilder<SInt> {

  private final BuilderFactoryNumeric<SInt> factoryNumeric;
  private final ProtocolBuilder<SInt> builder;

  public DefaultComparisonBuilder(BuilderFactoryNumeric<SInt> factoryNumeric,
      ProtocolBuilder<SInt> builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  private GreaterThanReducerProtocol4 getGreaterThanProtocol(
      Computation<SInt> left,
      Computation<SInt> right,
      SInt result, boolean longCompare) {
    int bitLength = factoryNumeric.getBasicNumericFactory().getMaxBitLength();
    if (longCompare) {
      bitLength *= 2;
    }
    BasicNumericFactory bnf = factoryNumeric.getBasicNumericFactory();
    NumericNegateBitFactoryImpl numericNegateBitFactory = new NumericNegateBitFactoryImpl(bnf);
    InnerProductFactoryImpl innerProductFactory = new InnerProductFactoryImpl(bnf,
        new EntrywiseProductFactoryImpl(bnf));
    MiscOIntGenerators misc = new MiscOIntGenerators(bnf);
    ZeroTestProtocolFactoryImpl zeroTestProtocolFactory = new ZeroTestProtocolFactoryImpl(bnf,
        factoryNumeric.getExpFromOInt(), numericNegateBitFactory,
        factoryNumeric.getPreprocessedExpPipe());
    return new GreaterThanReducerProtocol4(
        bitLength, BuilderFactoryNumeric.MAGIC_SECURE_NUMBER,
        left, right, result,
        factoryNumeric.getBasicNumericFactory(),
        numericNegateBitFactory,
        factoryNumeric.getRandomAdditiveMaskFactory(),
        zeroTestProtocolFactory, misc, innerProductFactory,
        factoryNumeric.getInversionFactory(), factoryNumeric);
  }

  @Override
  public Computation<SInt> compareLong(Computation<SInt> left, Computation<SInt> right) {
    SInt result = factoryNumeric.getBasicNumericFactory().getSInt();
    GreaterThanReducerProtocol4 greaterThanProtocol = getGreaterThanProtocol(left, right,
            result, true);
    builder.append((ProtocolProducer) greaterThanProtocol);
    return greaterThanProtocol;
  }

  @Override
  public Computation<SInt> compare(Computation<SInt> left, Computation<SInt> right) {
    SInt result = factoryNumeric.getBasicNumericFactory().getSInt();
    GreaterThanReducerProtocol4 greaterThanProtocol = getGreaterThanProtocol(left, right,
        result, false);
    builder.append((ProtocolProducer) greaterThanProtocol);
    return greaterThanProtocol;
  }

  public Computation<SInt> sign(Computation<SInt> x) {
    BasicNumericFactory bnf = factoryNumeric.getBasicNumericFactory();
    Computation<SInt> compare = compare(
        () -> builder.getSIntFactory().getSInt(0), x);
    OInt oInt = bnf.getOInt(BigInteger.valueOf(2));
    NumericBuilder<SInt> numericBuilder = builder.numeric();
    Computation<SInt> twice = numericBuilder.mult(
        () -> builder.getSIntFactory().getSInt(oInt.getValue()), compare);
    Computation<SInt> result = numericBuilder.sub(twice,
        () -> builder.getSIntFactory().getSInt(1));
    return result;
  }

}
