package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;

public class BaseProtocol {

  protected final MascotResourcePool resourcePool;
  protected final Network network;

  public BaseProtocol(MascotResourcePool resourcePool, Network network) {
    this.resourcePool = resourcePool;
    this.network = network;
  }

  public int getMyId() {
    return resourcePool.getMyId();
  }

  public BigInteger getModulus() {
    return resourcePool.getModulus();
  }

  public int getModBitLength() {
    return resourcePool.getModBitLength();
  }

  public int getLambdaSecurityParam() {
    return resourcePool.getLambdaSecurityParam();
  }

  public FieldElementSerializer getFieldElementSerializer() {
    return resourcePool.getFieldElementSerializer();
  }

  public FieldElementPrg getLocalSampler() {
    return resourcePool.getLocalSampler();
  }

}
