package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import java.math.BigInteger;

public class DummyArithmeticOIntProtocolWrapper extends
    DummyArithmeticNativeProtocol<OInt> {

  private final DummyArithmeticNativeProtocol<BigInteger> innerProtocol;
  private final OIntFactory oIntFactory;
  private OInt out;

  public DummyArithmeticOIntProtocolWrapper(
      DummyArithmeticNativeProtocol<BigInteger> innerProtocol,
      OIntFactory oIntFactory) {
    this.innerProtocol = innerProtocol;
    this.oIntFactory = oIntFactory;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
      Network network) {
    innerProtocol.evaluate(round, resourcePool, network);
    out = oIntFactory.fromBigInteger(innerProtocol.out());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public OInt out() {
    return out;
  }
}
