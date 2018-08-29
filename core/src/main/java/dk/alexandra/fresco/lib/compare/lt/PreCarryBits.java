package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
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
            (prevScope, pairs) -> prevScope.par(par -> {
//              par.seq(seq -> seq.debug().)
              return par.comparison().carry(pairs);
            }))
        .seq((seq, out) -> out.get(0).getSecond());
  }

}
