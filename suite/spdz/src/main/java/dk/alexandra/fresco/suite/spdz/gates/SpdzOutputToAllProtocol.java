package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.util.List;

public class SpdzOutputToAllProtocol extends SpdzNativeProtocol<BigInteger>
    implements SpdzRequiresMacCheck {

  private DRes<SInt> in;
  private SpdzSInt outTemp;
  private BigInteger out;

  public SpdzOutputToAllProtocol(DRes<SInt> in) {
    this.in = in;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {

    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      outTemp = (SpdzSInt) in.out();
      network.sendToAll(serializer.serialize(outTemp.getShare()));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<byte[]> shares = network.receiveFromAll();
      BigInteger openedVal = BigInteger.valueOf(0);
      for (byte[] buffer : shares) {
        openedVal = openedVal.add(serializer.deserialize(buffer));
      }
      openedVal = openedVal.mod(spdzResourcePool.getModulus());
      spdzResourcePool.getOpenedValueStore().pushOpenedValue(outTemp, openedVal);
      BigInteger tmpOut = openedVal;
      tmpOut = spdzResourcePool.convertRepresentation(tmpOut);
      this.out = tmpOut;
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public BigInteger out() {
    return out;
  }
}
