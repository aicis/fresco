package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class DummyArithmeticSubtractProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private DRes<SInt> left;
  private DRes<SInt> right;
  private DummyArithmeticSInt out;

  /**
   * Constructs a native subtraction protocol for the Dummy Arithmetic suite.
   *
   * @param left the left operand
   * @param right the right operand
   *
   */
  public DummyArithmeticSubtractProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool rp, Network network) {
    BigInteger l = ((DummyArithmeticSInt) left.out()).getValue();
    BigInteger r = ((DummyArithmeticSInt) right.out()).getValue();
    BigInteger sub = l.subtract(r).mod(rp.getModulus());
    out = new DummyArithmeticSInt(sub);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }
}
