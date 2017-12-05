package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.network.Network;

public class BaseProtocol {

  protected Integer myId;
  protected Network network;
  protected BigInteger modulus;
  protected int modBitLength;
  
  public BaseProtocol(MascotContext ctx) {
    super();
    this.myId = ctx.getMyId();
    this.network = ctx.getNetwork();
    this.modulus = ctx.getModulus();
    this.modBitLength = ctx.getkBitLength();
  }
  
}
