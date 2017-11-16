package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class SpdzKnownSIntProtocol extends SpdzNativeProtocol<SInt> {

  BigInteger value;
  private SpdzSInt sValue;

  /**
   * Creates a gate loading a given value into a given SInt
   *
   * @param value the value
   */
  public SpdzKnownSIntProtocol(BigInteger value) {
    this.value = value;
  }

  @Override
  public SInt out() {
    return sValue;
  }

  @Override
  public EvaluationStatus evaluate(
      int round,
      SpdzResourcePool spdzResourcePool,
      Network network) {
    sValue = new SpdzSInt(createKnownSpdzElement(spdzResourcePool, value));
    return EvaluationStatus.IS_DONE;
  }

  static SpdzElement createKnownSpdzElement(
      SpdzResourcePool spdzResourcePool,
      BigInteger input) {
    BigInteger modulus = spdzResourcePool.getModulus();
    BigInteger value = input.mod(modulus);
    SpdzElement elm;
    BigInteger globalKeyShare = spdzResourcePool.getStore().getSSK();
    if (spdzResourcePool.getMyId() == 1) {
      elm = new SpdzElement(value,
          value.multiply(globalKeyShare).mod(modulus), modulus);
    } else {
      elm = new SpdzElement(BigInteger.ZERO,
          value.multiply(globalKeyShare).mod(modulus), modulus);
    }
    return elm;
  }
}
