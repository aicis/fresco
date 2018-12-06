package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.util.List;

public class SpdzOutputToAllProtocol extends SpdzNativeProtocol<BigInteger>
    implements SpdzOutputProtocol {

  private DRes<SInt> in;
  private BigInteger out;

  public SpdzOutputToAllProtocol(DRes<SInt> in) {
    this.in = in;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {

    ByteSerializer<FieldElement> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      SpdzSInt out = (SpdzSInt) in.out();
      network.sendToAll(serializer.serialize(out.getShare()));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<byte[]> shares = network.receiveFromAll();
      FieldElement openedVal =
          BigInt.fromConstant(BigInteger.valueOf(0),
              spdzResourcePool.getModulus());
      for (byte[] buffer : shares) {
        openedVal = openedVal.add(serializer.deserialize(buffer));
      }
      spdzResourcePool.getOpenedValueStore().pushOpenedValue(((SpdzSInt) in.out()), openedVal);
      this.out = spdzResourcePool.convertRepresentation(openedVal);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public BigInteger out() {
    return out;
  }
}
