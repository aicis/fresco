package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Implements closing a value in the Dummy Arithmetic suite where all operations are done in the
 * clear. I.e., this really does nothing but send the open value to the other parties so they can
 * compute on it.
 */
public class DummyArithmeticCloseProtocol extends DummyArithmeticNativeProtocol<SInt> {

  private int targetId;
  private BigInteger value;
  private DummyArithmeticSInt closed;

  /**
   * Constructs a protocol to close an open value.
   *
   * @param value a computation output the value to close.
   * @param targetId id of the party supplying the open value.
   */
  public DummyArithmeticCloseProtocol(BigInteger value, int targetId) {
    this.targetId = targetId;
    this.value = value;
  }

  @Override
  public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool rp, Network network) {
    if (round == 0) {
      if (targetId == rp.getMyId()) {
        FieldElement open = Objects.isNull(value) ? null : rp.getFieldDefinition().createElement(value);
        network.sendToAll(rp.getFieldDefinition().serialize(open));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else { //if (round == 1) {
      byte[] bin = network.receive(targetId);
      closed = new DummyArithmeticSInt(rp.getFieldDefinition().deserialize(bin));
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return closed;
  }
}
