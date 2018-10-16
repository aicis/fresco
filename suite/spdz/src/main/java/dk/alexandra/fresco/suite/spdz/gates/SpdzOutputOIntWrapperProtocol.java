package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import java.math.BigInteger;

/**
 * A wrapper protocol which calls the original BigInteger based output protocol and converts the
 * result.
 */
public class SpdzOutputOIntWrapperProtocol extends SpdzNativeProtocol<OInt>
    implements SpdzOutputProtocol {

  private OInt out;
  private final SpdzNativeProtocol<BigInteger> innerProtocol;
  private final OIntFactory oIntFactory;

  public SpdzOutputOIntWrapperProtocol(OIntFactory oIntFactory,
      SpdzNativeProtocol<BigInteger> innerProtocol) {
    this.innerProtocol = innerProtocol;
    this.oIntFactory = oIntFactory;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    EvaluationStatus innerStatus = innerProtocol.evaluate(round, spdzResourcePool, network);
    if (innerStatus == EvaluationStatus.IS_DONE) {
      this.out = oIntFactory.fromBigInteger(innerProtocol.out());
    }
    return innerStatus;
  }

  @Override
  public OInt out() {
    return out;
  }

}
