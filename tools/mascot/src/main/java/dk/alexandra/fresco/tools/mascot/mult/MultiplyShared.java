package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;

public class MultiplyShared extends TwoPartyProtocol {

  protected RotBatch<StrictBitVector> rot;
  protected int numLeftFactors;

  public MultiplyShared(MascotContext ctx, Integer otherId, int numLeftFactors) {
    super(ctx, otherId);
    this.numLeftFactors = numLeftFactors;
    this.rot = new BristolRotBatch(myId, otherId, modBitLength, ctx.getLambdaSecurityParam(),
        ctx.getRand(), network);
  }

}
