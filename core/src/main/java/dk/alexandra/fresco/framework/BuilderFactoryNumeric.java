package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;

public interface BuilderFactoryNumeric<SIntT extends SInt> extends BuilderFactory {

  int MAGIC_SECURE_NUMBER = 60;

  BasicNumericFactory<SIntT> getBasicNumericFactory();

  NumericBuilder<SIntT> createNumericBuilder(ProtocolBuilder builder);

  default PreprocessedExpPipeFactory getPreprocessedExpPipe() {
    return (PreprocessedExpPipeFactory) getBasicNumericFactory();
  }

  default LocalInversionFactory getInversionFactory() {
    return (LocalInversionFactory) getBasicNumericFactory();
  }

  default ExpFromOIntFactory getExpFromOInt() {
    return (ExpFromOIntFactory) getBasicNumericFactory();
  }

  default ComparisonBuilder<SIntT> createComparisonBuilder(ProtocolBuilder<SIntT> builder) {
    return new DefaultComparisonBuilder<>(this, builder);
  }


  default EntrywiseProductFactoryImpl getDotProductFactory() {
    return new EntrywiseProductFactoryImpl(getBasicNumericFactory());
  }

  default InnerProductFactoryImpl getInnerProductFactory() {
    return new InnerProductFactoryImpl(getBasicNumericFactory(), getDotProductFactory());
  }

  default RandomAdditiveMaskFactory getRandomAdditiveMaskFactory() {
    return new RandomAdditiveMaskFactoryImpl(
        getBasicNumericFactory(), getBasicNumericFactory(),
        getInnerProductFactory());
  }

  default RightShiftFactory getRightShiftFactory() {
    LocalInversionFactory localInversionFactory = getInversionFactory();
    RandomAdditiveMaskFactory randomAdditiveMaskFactory = getRandomAdditiveMaskFactory();
    return new RightShiftFactoryImpl(
        getBasicNumericFactory(),
        randomAdditiveMaskFactory,
        localInversionFactory);
  }

  default IntegerToBitsFactory getIntegerToBitsFactory() {
    return new IntegerToBitsFactoryImpl(getBasicNumericFactory(), getRightShiftFactory());
  }

  default BitLengthFactory getBitLengthFactory() {
    return new BitLengthFactoryImpl(getBasicNumericFactory(), getIntegerToBitsFactory());
  }

  default ExponentiationFactory getExponentiationFactory() {
    return new ExponentiationFactoryImpl(getBasicNumericFactory(), getIntegerToBitsFactory());
  }

  default ComparisonProtocolFactory getComparisonFactory() {
    BasicNumericFactory<SIntT> factory = getBasicNumericFactory();
    return
        new ComparisonProtocolFactoryImpl(MAGIC_SECURE_NUMBER,
            factory,
            getInversionFactory(),
            factory,
            getExpFromOInt(),
            getPreprocessedExpPipe(
            ),
            this);

  }
}
