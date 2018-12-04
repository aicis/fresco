package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;

public class DummyArithmeticSubtractProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private DRes<SInt> left;
  private DRes<SInt> right;
  private DummyArithmeticSInt out;

  /**
   * Constructs a native subtraction protocol for the Dummy Arithmetic suite.
   *
   * @param left the left operand
   * @param right the right operand
   */
  public DummyArithmeticSubtractProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool rp, Network network) {
    BigIntegerI left = ((DummyArithmeticSInt) this.left.out()).getValue();
    BigIntegerI right = ((DummyArithmeticSInt) this.right.out()).getValue();
    out = new DummyArithmeticSInt(left.subtract(right));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }
}
