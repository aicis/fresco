package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultComparison;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.lt.MostSignBitSpdz2k;

/**
 * Spdz2k optimized protocols for comparison.
 */
public class Spdz2kComparison<PlainT extends CompUInt<?, ?, PlainT>> extends DefaultComparison {

  private final CompUIntFactory<PlainT> factory;

  public Spdz2kComparison(
      BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder, CompUIntFactory<PlainT> factory) {
    super(factoryNumeric, builder);
    this.factory = factory;
  }

  @Override
  public DRes<SInt> compareLT(DRes<SInt> x1, DRes<SInt> x2, ComparisonAlgorithm algorithm) {
    if (algorithm == ComparisonAlgorithm.LT_CONST_ROUNDS) {
      throw new UnsupportedOperationException(
          "No constant round comparison protocol implemented for Spdz2k");
    } else {
      DRes<SInt> diff = builder.numeric().sub(x1, x2);
      return builder.seq(new MostSignBitSpdz2k<>(diff, factory));
    }
  }

}
