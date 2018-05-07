package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

public class Spdz2kXorKnownProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<OInt> left;
  private final DRes<SInt> right;
  private SInt result;

  public Spdz2kXorKnownProtocol(
      DRes<OInt> left,
      DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
//    BigInteger value = resourcePool.getFactory().toBigInteger(left.out());
//    PlainT leftBit = resourcePool.getFactory().fromBit();
//    Spdz2kSIntBoolean<PlainT> rightBit = resourcePool.getFactory().toSpdz2kSIntBoolean(right);
//    result = leftBit.add(rightBit);
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    PlainT known = factory.fromOInt(left.out()).toBitRep();
    PlainT secretSharedKey = resourcePool.getDataSupplier().getSecretSharedKey();
    result = factory.toSpdz2kSIntBoolean(right).addConstant(known,
        secretSharedKey, factory.zero().toBitRep(), resourcePool.getMyId() == 1);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return result;
  }

}
