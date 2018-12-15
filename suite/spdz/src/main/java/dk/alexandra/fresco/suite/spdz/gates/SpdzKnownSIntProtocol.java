package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class SpdzKnownSIntProtocol extends SpdzNativeProtocol<SInt> {

  private final BigInteger value;
  private SpdzSInt secretValue;

  /**
   * Creates a gate loading a given value into a given SInt.
   *
   * @param value the value
   */
  public SpdzKnownSIntProtocol(BigInteger value) {
    this.value = value;
  }

  @Override
  public SInt out() {
    return secretValue;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool, Network network) {
    secretValue = createKnownSpdzElement(spdzResourcePool, value);
    return EvaluationStatus.IS_DONE;
  }

  static SpdzSInt createKnownSpdzElement(SpdzResourcePool spdzResourcePool, BigInteger input) {
    SpdzSInt elm;
    FieldElement value = spdzResourcePool.getFieldDefinition().createElement(input);
    FieldElement globalKeyShare = spdzResourcePool.getDataSupplier().getSecretSharedKey();

    FieldElement mac = value.multiply(globalKeyShare);

    if (spdzResourcePool.getMyId() == 1) {
      elm = new SpdzSInt(value, mac);
    } else {
      elm = new SpdzSInt(spdzResourcePool.getFieldDefinition().createElement(0), mac);
    }
    return elm;
  }
}
