package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;

public class BaseProtocol {

  protected Integer myId;
  protected Network network;
  protected BigInteger modulus;
  protected int modBitLength;
  protected FieldElementSerializer feSerializer;
  
  public BaseProtocol(MascotContext ctx) {
    super();
    this.myId = ctx.getMyId();
    this.network = ctx.getNetwork();
    this.modulus = ctx.getModulus();
    this.modBitLength = ctx.getkBitLength();
    this.feSerializer = ctx.getFeSerializer();
  }
  
}
