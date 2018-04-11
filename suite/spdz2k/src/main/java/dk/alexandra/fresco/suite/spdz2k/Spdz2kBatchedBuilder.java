package dk.alexandra.fresco.suite.spdz2k;


import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;

/**
 * Builder that uses batched native protocols for the SPDZ2k protocol suite.
 */
class Spdz2kBatchedBuilder<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kBuilder<PlainT> {

  Spdz2kBatchedBuilder(
      CompUIntFactory<PlainT> factory,
      BasicNumericContext numericContext) {
    super(factory, numericContext);
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    if (builder.isParallel()) {
      return new Spdz2kBatchedNumeric<>(builder, factory);
    } else {
      return new Spdz2kNumeric<>(builder, factory);
    }
  }

}
