package dk.alexandra.fresco.tools.mascot;

import java.util.List;

public class MultiPartyProtocol extends BaseProtocol {

  protected List<Integer> partyIds;
  
  public MultiPartyProtocol(MascotContext ctx) {
    super(ctx);
    partyIds = ctx.getPartyIds();
  }

}
