package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
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

public class DefaultComparisonBuilder<SIntT extends SInt> implements ComparisonBuilder<SIntT> {

  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  private final ProtocolBuilder<SIntT> builder;

  public DefaultComparisonBuilder(BuilderFactoryNumeric<SIntT> factoryNumeric,
      ProtocolBuilder<SIntT> builder) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
  }

  private GreaterThanReducerProtocol4 getGreaterThanProtocol(
      Computation<SIntT> left,
      Computation<SIntT> right,
      SIntT result, boolean longCompare) {
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
        factoryNumeric.getInversionFactory(), (BuilderFactoryNumeric) factoryNumeric);
  }

  @Override
  public Computation<SIntT> compareLong(Computation<SIntT> left, Computation<SIntT> right) {
    SIntT result = (SIntT) factoryNumeric.getBasicNumericFactory().getSInt();
    GreaterThanReducerProtocol4 greaterThanProtocol = getGreaterThanProtocol(left, right,
            result, true);
    builder.append((ProtocolProducer) greaterThanProtocol);
    return greaterThanProtocol;
  }

  @Override
  public Computation<SIntT> compare(Computation<SIntT> left, Computation<SIntT> right) {
    SIntT result = (SIntT) factoryNumeric.getBasicNumericFactory().getSInt();
    GreaterThanReducerProtocol4 greaterThanProtocol = getGreaterThanProtocol(left, right,
        result, false);
    builder.append((ProtocolProducer) greaterThanProtocol);
    return greaterThanProtocol;
  }

  public Computation<SIntT> sign(Computation<SIntT> x) {
    BasicNumericFactory bnf = factoryNumeric.getBasicNumericFactory();
    Computation<SIntT> compare = (Computation<SIntT>) compare(
        () -> builder.createConstant(0), x);
    OInt oInt = bnf.getOInt(BigInteger.valueOf(2));
    NumericBuilder<SIntT> numericBuilder = builder.numeric();
    Computation<SIntT> twice = numericBuilder.mult(
        () -> (SIntT) builder.getSIntFactory().getSInt(oInt.getValue()), compare);
    Computation<SIntT> result = numericBuilder.sub(twice, () -> builder.createConstant(1));
    return result;
  }

}
