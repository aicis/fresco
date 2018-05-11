package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

/**
 * Native protocol for converting boolean shares to arithmetic shares.
 */
public class Spdz2kBooleanToArithmeticProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> bool;
  private SInt arithmetic;
  private Spdz2kSIntArithmetic<PlainT> arithmeticR;
  private Spdz2kSIntBoolean<PlainT> c;

  public Spdz2kBooleanToArithmeticProtocol(DRes<SInt> bool) {
    this.bool = bool;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      arithmeticR = resourcePool.getDataSupplier().getNextBitShare();
      Spdz2kSIntBoolean<PlainT> booleanR = arithmeticR.toBoolean();
      c = factory.toSpdz2kSIntBoolean(bool).xor(booleanR);
      network.sendToAll(c.serializeShareLow());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      PlainT openC = receiveAndReconstruct(network, resourcePool.getNoOfParties(), factory);
      resourcePool.getOpenedValueStore().pushOpenedValue(
          c.asArithmetic(),
          openC.toArithmeticRep()
      );
      // TODO
      PlainT openCBit = openC.bitValue() == 1 ? factory.one() : factory.zero();
      PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      boolean isPartyOne = resourcePool.getMyId() == 1;
      arithmetic = arithmeticR.addConstant(openCBit, macKeyShare, factory.zero(),
          isPartyOne).subtract(arithmeticR.multiply(factory.two().multiply(openCBit)));
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Receive shares of value and reconstruct. <p>Note that this includes overflow into top s
   * bits.</p>
   */
  private PlainT receiveAndReconstruct(Network network, int noOfParties,
      CompUIntFactory<PlainT> factory) {
    int received = network.receive(1)[0];
    PlainT opened = factory.fromBit(received);
    for (int i = 2; i <= noOfParties; i++) {
      received = network.receive(i)[0];
      opened = opened.add(factory.fromBit(received));
    }
    return opened;
  }

  @Override
  public SInt out() {
    return arithmetic;
  }

}
