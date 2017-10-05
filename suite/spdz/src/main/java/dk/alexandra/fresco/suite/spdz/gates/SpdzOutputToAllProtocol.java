package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

public class SpdzOutputToAllProtocol extends SpdzNativeProtocol<BigInteger> {

  private DRes<SInt> in;
  private BigInteger out;

  public SpdzOutputToAllProtocol(DRes<SInt> in) {
    this.in = in;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      SCENetwork network) {
    spdzResourcePool.setOutputProtocolInBatch(true);

    SpdzStorage storage = spdzResourcePool.getStore();
    BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
    switch (round) {
      case 0:
        SpdzSInt out = (SpdzSInt) in.out();
        network.sendToAll(serializer.toBytes(out.value.getShare()));
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
        storage.addClosedValue(((SpdzSInt) in.out()).value);
        BigInteger tmpOut = openedVal;
        tmpOut = spdzResourcePool.convertRepresentation(tmpOut);
        this.out = tmpOut;
        return EvaluationStatus.IS_DONE;
      default:
        throw new MPCException("No more rounds to evaluate.");
    }
  }

  @Override
  public BigInteger out() {
    return out;
  }
}
