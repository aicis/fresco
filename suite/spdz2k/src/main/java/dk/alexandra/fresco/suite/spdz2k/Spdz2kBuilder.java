package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;

/**
 * Basic native builder for the SPDZ2k protocol suite.
 *
 * @param <PlainT> the type representing open values
 */
public class Spdz2kBuilder<PlainT extends CompUInt<?, ?, PlainT>> implements
    BuilderFactoryNumeric {

  private final BasicNumericContext numericContext;
  protected final CompUIntFactory<PlainT> factory;

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
    return new Spdz2kNumeric<>(builder, factory);
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    throw new UnsupportedOperationException();
  }


  @Override
  public RealNumericContext getRealNumericContext() {
    // TODO Auto-generated method stub
    return null;
  }

}
