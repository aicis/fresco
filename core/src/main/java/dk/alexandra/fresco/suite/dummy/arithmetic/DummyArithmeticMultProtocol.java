package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Implements multiplication for the Dummy Arithmetic protocol suite, where all operations are done
 * in the clear.
 */
public class DummyArithmeticMultProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private DRes<SInt> left;
  private DRes<SInt> right;
  private DummyArithmeticSInt out;

  /**
   * Constructs a protocol to multiply the result of two computations.
   *
   * @param left the left operand
   * @param right the right operand
   */
  public DummyArithmeticMultProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool rp, Network network) {
    FieldElement l = left.out().getShare();
    FieldElement r = right.out().getShare();
    out = new DummyArithmeticSInt(l.multiply(r));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }
}
