package dk.alexandra.fresco.suite.spdz2k;


import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;

public class Spdz2kBatchedBuilder<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kBuilder<PlainT> {

  public Spdz2kBatchedBuilder(
      CompUIntFactory<PlainT> factory,
      BasicNumericContext numericContext) {
    super(factory, numericContext);
  }


}
