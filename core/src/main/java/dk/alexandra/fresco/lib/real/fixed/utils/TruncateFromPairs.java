package dk.alexandra.fresco.lib.real.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Probabilistic truncation protocol. <p>Described by Mohassel and Rindal in
 * https://eprint.iacr.org/2018/403.pdf (Figure 3).</p>
 */
public class TruncateFromPairs implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int shifts;

  public TruncateFromPairs(DRes<SInt> input, int shifts) {
    this.input = input;
    this.shifts = shifts;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<TruncationPair> truncationPairD = builder.advancedNumeric().generateTruncationPair(shifts);
    return builder.seq(seq -> {
      TruncationPair truncationPair = truncationPairD.out();
      // TODO look into making fixed-point arithmetic  tests pass when we subtract here (to be
      // consistent with original protocol)
      DRes<SInt> masked = seq.numeric().add(input, truncationPair.getRPrime());
      return seq.numeric().openAsOInt(masked);
    }).seq((seq, opened) -> {
      OInt shifted = seq.getOIntArithmetic().shiftRight(opened, shifts);
      DRes<SInt> r = truncationPairD.out().getR();
      return seq.numeric().subFromOpen(shifted, r);
    });
  }
}
