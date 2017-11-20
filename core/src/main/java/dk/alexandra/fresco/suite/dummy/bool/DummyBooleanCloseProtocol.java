package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;

public class DummyBooleanCloseProtocol extends DummyBooleanNativeProtocol<SBool> {

  public DRes<Boolean> input;
  public DummyBooleanSBool output;

  private int sender;

  /**
   * Constructs a protocol to close an open value.
   *
   * @param sender id of the party supplying the open value.
   * @param in a computation output the value to close.
   */
  public DummyBooleanCloseProtocol(int sender, DRes<Boolean> in) {
    input = in;
    output = null;
    this.sender = sender;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePool resourcePool, Network network) {
    if (round == 0) {
      if (resourcePool.getMyId() == sender) {
        network.sendToAll(new byte[]{BooleanSerializer.toBytes(input.out())});
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      boolean r = BooleanSerializer.fromBytes(network.receive(sender)[0]);
      this.output = new DummyBooleanSBool(r);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SBool out() {
    return output;
  }
}
