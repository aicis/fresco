package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class SpdzKnownSIntProtocol extends SpdzNativeProtocol<SInt> {

  private final BigIntegerI value;
  private final BigIntegerI zero;
  private SpdzSInt secretValue;

  /**
   * Creates a gate loading a given value into a given SInt.
   *
   * @param value the value
   */
  public SpdzKnownSIntProtocol(BigIntegerI value, BigIntegerI zero) {
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
      BigIntegerI input, BigIntegerI zero) {
    BigInteger modulus = spdzResourcePool.getModulus();
    SpdzSInt elm;
    BigIntegerI globalKeyShare =
        spdzResourcePool.getDataSupplier().getSecretSharedKey();

    BigIntegerI mac = input.multiply(globalKeyShare);

    if (spdzResourcePool.getMyId() == 1) {
      elm = new SpdzSInt(input, mac, modulus);
    } else {
      elm = new SpdzSInt(zero, mac, modulus);
    }
    return elm;
  }
}
