package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultComparison;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.lt.MostSignBitSpdz2k;
import java.util.List;

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
  public DRes<SInt> compareLT(DRes<SInt> x1, DRes<SInt> x2, Algorithm algorithm) {
    if (algorithm == Algorithm.CONST_ROUNDS) {
      throw new UnsupportedOperationException(
          "No constant round comparison protocol implemented for Spdz2k");
    } else {
      DRes<SInt> diff = builder.numeric().sub(x1, x2);
      return builder.seq(new MostSignBitSpdz2k<>(diff, factory));
    }
  }

  @Override
  public DRes<SInt> compareLTBits(DRes<OInt> openValue, DRes<List<DRes<SInt>>> secretBits) {
    DRes<List<DRes<SInt>>> converted = builder.conversion().toBooleanBatch(secretBits);
    return super.compareLTBits(openValue, converted);
  }

  protected CompUIntFactory<PlainT> getFactory() {
    return factory;
  }

}
