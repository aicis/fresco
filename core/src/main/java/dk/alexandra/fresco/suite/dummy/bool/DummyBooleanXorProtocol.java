package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Implements logical XOR for the Dummy Boolean protocol suite, where all operations are done in the
 * clear.
 */
public class DummyBooleanXorProtocol extends DummyBooleanNativeProtocol<SBool> {

  private DRes<SBool> left;
  private DRes<SBool> right;
  private DummyBooleanSBool out;

  /**
   * Constructs a protocol to XOR the result of two computations.
   *
   * @param left the left operand
   * @param right the right operand
   */
  public DummyBooleanXorProtocol(DRes<SBool> left, DRes<SBool> right) {
    super();
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePool resourcePool, Network network) {
    Boolean leftValue = ((DummyBooleanSBool) left.out()).getValue();
    Boolean rightValue = ((DummyBooleanSBool) right.out()).getValue();
    boolean value = leftValue ^ rightValue;
    out = new DummyBooleanSBool(value);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return out;
  }
}
