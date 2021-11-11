package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Random;

public class DummyArithmeticRandomBitProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private final Random random;
  private SInt bit;

  public DummyArithmeticRandomBitProtocol(Random random) {
    this.random = random;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
      Network network) {
    bit = new DummyArithmeticSInt(resourcePool.getFieldDefinition().createElement(BigInteger.valueOf(random.nextInt(2))));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return bit;
  }
}
