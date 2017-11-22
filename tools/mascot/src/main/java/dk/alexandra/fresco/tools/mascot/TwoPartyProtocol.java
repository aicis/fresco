package dk.alexandra.fresco.tools.mascot;

public class TwoPartyProtocol extends BaseProtocol {

  protected Integer otherId;
  
  public TwoPartyProtocol(MascotContext ctx, Integer otherId) {
    super(ctx);
    this.otherId = otherId;
  }
  
  public Integer getOtherId() {
    return otherId;
  }

}
