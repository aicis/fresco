package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultComparison;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestLogRounds;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.ZeroTestSpdz2k;

/**
 * Spdz2k optimized protocols for comparison.
 */
public class Spdz2kComparison<PlainT extends CompUInt<?, ?, PlainT>> extends DefaultComparison {

  private final CompUIntFactory<PlainT> factory;

  Spdz2kComparison(
      BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder, CompUIntFactory<PlainT> factory) {
    super(factoryNumeric, builder);
    this.factory = factory;
  }

  @Override
  public DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y,
      ComparisonAlgorithm algorithm) {
    if (algorithm == ComparisonAlgorithm.CONST_ROUNDS) {
      throw new UnsupportedOperationException("No constant rounds algorithm implemented");
    } else {
      DRes<SInt> diff = getBuilder().numeric().sub(x, y);
      return getBuilder().seq(new ZeroTestSpdz2k<>(diff, factory));
    }
  }

  protected CompUIntFactory<PlainT> getFactory() {
    return factory;
  }

}
