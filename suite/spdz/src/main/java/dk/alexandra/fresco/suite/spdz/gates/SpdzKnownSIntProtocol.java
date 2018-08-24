package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class SpdzKnownSIntProtocol extends SpdzNativeProtocol<SInt> {

  private BigInteger value;
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
  public EvaluationStatus evaluate(
      int round,
      SpdzResourcePool spdzResourcePool,
      Network network) {
    secretValue = createKnownSpdzElement(spdzResourcePool, value);
    return EvaluationStatus.IS_DONE;
  }

  static SpdzSInt createKnownSpdzElement(
      SpdzResourcePool spdzResourcePool,
      BigInteger input) {
    BigInteger modulus = spdzResourcePool.getModulus();
    BigInteger value = input.mod(modulus);
    SpdzSInt elm;
    BigInteger globalKeyShare = spdzResourcePool.getDataSupplier().getSecretSharedKey();
    if (spdzResourcePool.getMyId() == 1) {
      elm = new SpdzSInt(value,
          value.multiply(globalKeyShare).mod(modulus), modulus);
    } else {
      elm = new SpdzSInt(BigInteger.ZERO,
          value.multiply(globalKeyShare).mod(modulus), modulus);
    }
    return elm;
  }
}
