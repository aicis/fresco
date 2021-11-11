package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Random;

public class DummyArithmeticRandomElementProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private final Random random;
  private DummyArithmeticSInt elm;

  public DummyArithmeticRandomElementProtocol(Random random) {
    this.random = random;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
      Network network) {
    BigInteger r;
    BigInteger modulus = resourcePool.getModulus();
    do {
      r = new BigInteger(modulus.bitLength() + 1, random);
    } while (r.compareTo(modulus) >= 0);
    elm = new DummyArithmeticSInt(resourcePool.getFieldDefinition().createElement(r));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return elm;
  }
}
