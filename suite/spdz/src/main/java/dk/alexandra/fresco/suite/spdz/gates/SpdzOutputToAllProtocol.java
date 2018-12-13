package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
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

    FieldDefinition definition = spdzResourcePool.getFieldDefinition();
    if (round == 0) {
      SpdzSInt out = (SpdzSInt) in.out();
      network.sendToAll(definition.serialize(out.getShare()));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<byte[]> shares = network.receiveFromAll();
      FieldElement openedVal = definition.deserialize(shares.get(0));
      for (int i = 1; i < shares.size(); i++) {
        byte[] buffer = shares.get(i);
        openedVal = openedVal.add(definition.deserialize(buffer));
      }
      spdzResourcePool.getOpenedValueStore().pushOpenedValue(((SpdzSInt) in.out()), openedVal);
      this.out = spdzResourcePool.getFieldDefinition().convertRepresentation(openedVal);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public BigInteger out() {
    return out;
  }
}
