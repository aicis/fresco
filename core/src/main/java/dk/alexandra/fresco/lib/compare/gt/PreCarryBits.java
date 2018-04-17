package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreCarryBits implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SIntPair>>> pairsDef;

  PreCarryBits(DRes<List<DRes<SIntPair>>> pairs) {
    this.pairsDef = pairs;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    // TODO enforce that input size is power of 2?
    List<DRes<SIntPair>> pairs = pairsDef.out();
    // TODO this will reverse the actual list, not just the view. more efficient to only reverse the view
    Collections.reverse(pairs);
    int k = pairs.size();
    if (k == 1) {
      return pairs.get(0).out().getSecond();
    } else {
      DRes<List<DRes<SIntPair>>> nextRound = builder.par(par -> {
        List<DRes<SIntPair>> nextRoundInner = new ArrayList<>(k / 2);
        for (int i = 0; i < k / 2; i++) {
          DRes<SIntPair> left = pairs.get(2 * i + 1);
          DRes<SIntPair> right = pairs.get(2 * i);
          nextRoundInner.add(par.seq(new CarryHelper(left, right)));
        }
        Collections.reverse(nextRoundInner);
        return () -> nextRoundInner;
      });
      return builder.seq(new PreCarryBits(nextRound));
    }
  }

}
