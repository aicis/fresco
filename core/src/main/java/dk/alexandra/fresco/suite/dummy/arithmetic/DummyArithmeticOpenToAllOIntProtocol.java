package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Implements openings for the Dummy Arithmetic protocol suite, where all operations are done in the
 * clear.
 */
public class DummyArithmeticOpenToAllOIntProtocol extends DummyArithmeticNativeProtocol<OInt> {

  private DRes<SInt> closed;
  private OInt opened;
  private final OIntFactory oIntFactory;

  /**
   * Constructs a native protocol to open a closed integer towards all parties.
   *
   * @param s a computation supplying the {@link SInt} to open
   */
  public DummyArithmeticOpenToAllOIntProtocol(DRes<SInt> s, OIntFactory oIntFactory) {
    super();
    this.closed = s;
    this.oIntFactory = oIntFactory;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
      Network network) {
    opened = oIntFactory.fromBigInteger(new DummyArithmeticOpenToAllProtocol(closed).out());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public OInt out() {
    return opened;
  }

}
