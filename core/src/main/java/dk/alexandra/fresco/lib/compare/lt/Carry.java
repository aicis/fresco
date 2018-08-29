package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

public class Carry implements ComputationParallel<List<SIntPair>, ProtocolBuilderNumeric> {

  private final List<SIntPair> pairs;

  public Carry(List<SIntPair> pairs) {
    this.pairs = pairs;
  }

  @Override
  public DRes<List<SIntPair>> buildComputation(ProtocolBuilderNumeric builder) {
    padIfUneven(pairs);
    List<SIntPair> nextRoundInner = new ArrayList<>(pairs.size() / 2);
    for (int i = 0; i < pairs.size() / 2; i++) {
      SIntPair left = pairs.get(2 * i + 1);
      SIntPair right = pairs.get(2 * i);
      nextRoundInner.add(carry(builder, left, right));
    }
    return () -> nextRoundInner;
  }

  private SIntPair carry(ProtocolBuilderNumeric builder, SIntPair left, SIntPair right) {
    if (left == null) {
      return right;
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

  private void padIfUneven(List<SIntPair> pairs) {
    int size = pairs.size();
    if (size % 2 != 0 && size != 1) {
      pairs.add(null);
    }
  }

}
