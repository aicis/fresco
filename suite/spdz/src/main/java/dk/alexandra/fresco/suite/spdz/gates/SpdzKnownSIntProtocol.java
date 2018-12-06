package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;

public class SpdzKnownSIntProtocol extends SpdzNativeProtocol<SInt> {

  private final FieldElement value;
  private final FieldElement zero;
  private SpdzSInt secretValue;

  /**
   * Creates a gate loading a given value into a given SInt.
   *
   * @param value the value
   */
  public SpdzKnownSIntProtocol(FieldElement value, FieldElement zero) {
    this.value = value;
    this.zero = zero;
  }

  @Override
  public SInt out() {
    return secretValue;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool, Network network) {
    secretValue = createKnownSpdzElement(spdzResourcePool, value, zero);
    return EvaluationStatus.IS_DONE;
  }

  static SpdzSInt createKnownSpdzElement(
      SpdzResourcePool spdzResourcePool,
      FieldElement input, FieldElement zero) {
    SpdzSInt elm;
    FieldElement globalKeyShare =
        spdzResourcePool.getDataSupplier().getSecretSharedKey();

    FieldElement mac = input.multiply(globalKeyShare);

    if (spdzResourcePool.getMyId() == 1) {
      elm = new SpdzSInt(input, mac);
    } else {
      elm = new SpdzSInt(zero, mac);
    }
    return elm;
  }
}
