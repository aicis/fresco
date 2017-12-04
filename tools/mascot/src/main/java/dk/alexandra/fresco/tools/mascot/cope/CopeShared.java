package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;

public class CopeShared extends TwoPartyProtocol {

  protected boolean initialized;

  public CopeShared(MascotContext ctx, Integer otherId) {
    super(ctx, otherId);
    this.initialized = false;
  }

}
