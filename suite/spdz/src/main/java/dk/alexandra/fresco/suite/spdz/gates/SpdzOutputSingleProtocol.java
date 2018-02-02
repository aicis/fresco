package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.util.List;

public class SpdzOutputSingleProtocol extends SpdzNativeProtocol<BigInteger>
    implements SpdzOutputProtocol {

  private DRes<SInt> in;
  private BigInteger out;
  private int targetPlayer;
  private SpdzInputMask mask;

  public SpdzOutputSingleProtocol(DRes<SInt> in, int targetPlayer) {
    this.in = in;
    this.targetPlayer = targetPlayer;
  }

  @Override
  public BigInteger out() {
    return out;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {

    int myId = spdzResourcePool.getMyId();
    SpdzStorage storage = spdzResourcePool.getStore();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      this.mask = storage.getSupplier().getNextInputMask(targetPlayer);
      SpdzSInt closedValue = (SpdzSInt) this.in.out();
      SpdzElement inMinusMask = closedValue.value.subtract(this.mask.getMask());
      storage.addClosedValue(inMinusMask);
      network.sendToAll(serializer.serialize(inMinusMask.getShare()));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<byte[]> shares = network.receiveFromAll();
      BigInteger openedVal = BigInteger.valueOf(0);
      for (byte[] buffer : shares) {
        openedVal = openedVal.add(serializer.deserialize(buffer));
      }
      openedVal = openedVal.mod(spdzResourcePool.getModulus());
      storage.addOpenedValue(openedVal);
      if (targetPlayer == myId) {
        openedVal = openedVal.add(this.mask.getRealValue()).mod(spdzResourcePool.getModulus());
        this.out = openedVal;
      }
      return EvaluationStatus.IS_DONE;
    }
  }

}
