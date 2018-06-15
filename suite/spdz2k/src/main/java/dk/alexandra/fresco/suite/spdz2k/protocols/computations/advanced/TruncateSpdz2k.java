package dk.alexandra.fresco.suite.spdz2k.protocols.computations.advanced;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;

/**
 * Probabilistic truncation protocol.
 *
 * Described by Mohassel and Rindal in https://eprint.iacr.org/2018/403.pdf (Figure 3).
 */
public class TruncateSpdz2k<PlainT extends CompUInt<?, ?, PlainT>> implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;
  private final int d;

  public TruncateSpdz2k(DRes<SInt> value, int d) {
    this.value = value;
    this.d = d;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return null;
  }
}
