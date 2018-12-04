
package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Implements closing a value in the Dummy Arithmetic suite where all operations are done in the
 * clear. I.e., this really does nothing but send the open value to the other parties so they can
 * compute on it.
 */
public class DummyArithmeticCloseProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private int targetId;
  private DRes<BigIntegerI> open;
  private DummyArithmeticSInt closed;

  /**
   * Constructs a protocol to close an open value.
   *
   * @param targetId id of the party supplying the open value.
   * @param open a computation output the value to close.
   */
  public DummyArithmeticCloseProtocol(int targetId, DRes<BigIntegerI> open) {
    this.targetId = targetId;
    this.open = open;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool rp, Network network) {
    if (round == 0) {
      if (targetId == rp.getMyId()) {
        BigIntegerI out = open.out();
        network.sendToAll(rp.getSerializer().serialize(out));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else { //if (round == 1) {
      byte[] bin = network.receive(targetId);
      closed = new DummyArithmeticSInt(rp.getSerializer().deserialize(bin));
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return closed;
  }
}
