package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

public class PreCarryBits implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<SIntPair>> pairsDef;

  PreCarryBits(DRes<List<SIntPair>> pairs) {
    this.pairsDef = pairs;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> pairsDef)
        .whileLoop((pairs) -> pairs.size() > 1,
            (prevSeq, pairs) -> prevSeq.par(par -> {
              padIfUneven(pairs);
              List<SIntPair> nextRoundInner = new ArrayList<>(pairs.size() / 2);
              for (int i = 0; i < pairs.size() / 2; i++) {
                SIntPair left = pairs.get(2 * i + 1);
                SIntPair right = pairs.get(2 * i);
                nextRoundInner.add(carry(par, left, right));
              }
              return () -> nextRoundInner;
            })).seq((ignored, out) -> out.get(0).getSecond());
  }

  private SIntPair carry(ProtocolBuilderNumeric builder, SIntPair left, SIntPair right) {
    if (left == null) {
      return right;
    }
    if (right == null) {
      return left;
    }
    DRes<SInt> p1 = left.getFirst();
    DRes<SInt> g1 = left.getSecond();
    DRes<SInt> p2 = right.getFirst();
    DRes<SInt> g2 = right.getSecond();
    DRes<SInt> p = builder.logical().and(p1, p2);
    DRes<SInt> q = builder.seq(seq -> {
      DRes<SInt> temp = seq.logical().and(p2, g1);
      return seq.logical().halfOr(temp, g2);
    });
    return new SIntPair(p, q);
  }

  /**
   * Pad with dummy null element if number of pairs is uneven.
   */
  private void padIfUneven(List<SIntPair> pairs) {
    int size = pairs.size();
    if (size % 2 != 0 && size != 1) {
      pairs.add(null);
    }
  }

}
