package dk.alexandra.fresco.tools.ot.otextension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;

public class BristolOtShared {
  protected RotShared rot;
  protected boolean initialized = false;
  protected int batchSize;

  public BristolOtShared(RotShared rot, int batchSize) {
    super();
    this.rot = rot;
    this.batchSize = batchSize;
  }

  public int getOtherId() {
    return rot.getOtherId();
  }

  public int getKbitLength() {
    return rot.getKbitLength();
  }

  public int getLambdaSecurityParam() {
    return rot.getLambdaSecurityParam();
  }

  public Random getRand() {
    return rot.getRand();
  }

  public Network getNetwork() {
    return rot.getNetwork();
  }
}
