package dk.alexandra.fresco.suite.spdz2k.protocols.computations.advanced;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTruncationPair;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kTruncationPairProtocol;

/**
 * Probabilistic truncation protocol.
 *
 * Described by Mohassel and Rindal in https://eprint.iacr.org/2018/403.pdf (Figure 3).
 */
public class TruncateSpdz2k<PlainT extends CompUInt<?, ?, PlainT>> implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;
  private final int d;
  private final CompUIntFactory<PlainT> factory;

  public TruncateSpdz2k(DRes<SInt> value, int d, CompUIntFactory<PlainT> factory) {
    this.value = value;
    this.d = d;
    this.factory = factory;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<Spdz2kTruncationPair<PlainT>> truncationPairD = builder
        .append(new Spdz2kTruncationPairProtocol<>(d));
    return builder.seq(seq -> {
      Spdz2kTruncationPair<PlainT> truncationPair = truncationPairD.out();
      DRes<SInt> masked = seq.numeric().add(value, truncationPair.getRPrime());
      return seq.numeric().openAsOInt(masked);
    }).seq((seq, openedOInt) -> {
      PlainT opened = factory.fromOInt(openedOInt);
      PlainT shifted = opened.shiftRightLowOnly(d);
      DRes<SInt> r = truncationPairD.out().getR();
      return seq.numeric().subFromOpen(shifted, r);
    });
  }
}
