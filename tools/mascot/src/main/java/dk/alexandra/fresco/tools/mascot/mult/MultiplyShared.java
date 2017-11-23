package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.ot.base.DummyRotBatch;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

public class MultiplyShared extends TwoPartyProtocol {

  protected RotBatch<BigInteger> rot;
  protected int numLeftFactors;

  public MultiplyShared(MascotContext ctx, Integer otherId, int numLeftFactors) {
    super(ctx, otherId);
    this.numLeftFactors = numLeftFactors;
    this.rot = new DummyRotBatch(otherId, ctx.getNetwork(), ctx.getRand(), ctx.getkBitLength());
  }
  
}
