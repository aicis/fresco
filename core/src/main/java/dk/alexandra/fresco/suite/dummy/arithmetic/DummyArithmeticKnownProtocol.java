package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Implements using a known value in the Dummy Arithmetic suite where all operations are done in the
 * clear. I.e., this really does nothing but wrap the open value as an DummyArithmeticSInt.
 */
public class DummyArithmeticKnownProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private final BigInteger value;
  private DummyArithmeticSInt output;

  public DummyArithmeticKnownProtocol(BigInteger value) {
    this.value = value;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
      Network network) {
    output = new DummyArithmeticSInt(resourcePool.getFieldDefinition().createElement(value));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return output;
  }
}
