package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

public class SpdzOutputProtocol extends SpdzNativeProtocol<BigInteger> {

  private DRes<SInt> in;
  private BigInteger out;
  private int target_player;
  private SpdzInputMask mask;

  public SpdzOutputProtocol(DRes<SInt> in, int target_player) {
    this.in = in;
    this.target_player = target_player;
  }

  @Override
  public BigInteger out() {
    return out;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      SCENetwork network) {
    spdzResourcePool.setOutputProtocolInBatch(true);

    int myId = spdzResourcePool.getMyId();
    SpdzStorage storage = spdzResourcePool.getStore();
    BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
    switch (round) {
      case 0:
        this.mask = storage.getSupplier().getNextInputMask(target_player);
        SpdzSInt closedValue = (SpdzSInt) this.in.out();
        SpdzElement inMinusMask = closedValue.value.subtract(this.mask.getMask());
        storage.addClosedValue(inMinusMask);
        network.sendToAll(serializer.toBytes(inMinusMask.getShare()));
        network.expectInputFromAll();
        return EvaluationStatus.HAS_MORE_ROUNDS;
      case 1:
        List<ByteBuffer> shares = network.receiveFromAll();
        BigInteger openedVal = BigInteger.valueOf(0);
        for (ByteBuffer buffer : shares) {
          openedVal = openedVal.add(serializer.toBigInteger(buffer));
        }
        openedVal = openedVal.mod(spdzResourcePool.getModulus());
        storage.addOpenedValue(openedVal);
        if (target_player == myId) {
          openedVal = openedVal.add(this.mask.getRealValue()).mod(spdzResourcePool.getModulus());
          this.out = openedVal;
        }
        return EvaluationStatus.IS_DONE;
      default:
        throw new MPCException("No more rounds to evaluate.");
    }
  }

}
