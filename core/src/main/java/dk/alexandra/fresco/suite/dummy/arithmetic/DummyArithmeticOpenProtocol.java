package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Implements openings for the Dummy Arithmetic protocol suite, where all operations are done in the
 * clear.
 */
public class DummyArithmeticOpenProtocol extends DummyArithmeticNativeProtocol<BigInteger> {

  private BigInteger open;
  private DRes<SInt> closed;
  private int target;

  /**
   * Constructs a native protocol to open a closed integer towards a spcecific player.
   * 
   * @param c a computation supplying the {@link SInt} to open
   * @param target the id of party to open towards
   */
  public DummyArithmeticOpenProtocol(DRes<SInt> c, int target) {
    super();
    this.target = target;
    this.closed = c;
    this.open = null;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
      Network network) {
    if (resourcePool.getMyId() == target) {
      this.open = ((DummyArithmeticSInt) this.closed.out()).getValue();
    } else {
      this.open = null;
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public BigInteger out() {
    return this.open;
  }

}
