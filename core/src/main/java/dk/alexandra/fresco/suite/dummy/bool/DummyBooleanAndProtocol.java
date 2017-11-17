package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Implements logical AND for the Dummy Boolean protocol suite, where all operations are done in the
 * clear.
 */
public class DummyBooleanAndProtocol extends DummyBooleanNativeProtocol<SBool> {

  private DRes<SBool> left;
  private DRes<SBool> right;
  private DummyBooleanSBool out;

  /**
   * Constructs a protocol to AND the result of two computations.
   *
   * @param left the left operand
   * @param right the right operand
   */
  public DummyBooleanAndProtocol(DRes<SBool> left, DRes<SBool> right) {
    super();
    this.left = left;
    this.right = right;
    this.out = null;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePool resourcePool, Network network) {
    out = new DummyBooleanSBool(
        ((DummyBooleanSBool) left.out()).getValue() & ((DummyBooleanSBool) right.out()).getValue());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return out;
  }
}
