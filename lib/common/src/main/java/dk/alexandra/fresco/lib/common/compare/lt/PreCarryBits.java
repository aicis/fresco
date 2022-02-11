package dk.alexandra.fresco.lib.common.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.util.SIntPair;
import java.util.List;

public class PreCarryBits implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<SIntPair> pairsDef;

  PreCarryBits(List<SIntPair> pairs) {
    this.pairsDef = pairs;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> () -> pairsDef)
        .whileLoop((pairs) -> pairs.size() > 1,
            (prevScope, pairs) -> prevScope.par(par -> Comparison.using(par).carry(pairs)))
        .seq((seq, out) -> out.get(0).getSecond());
  }

}