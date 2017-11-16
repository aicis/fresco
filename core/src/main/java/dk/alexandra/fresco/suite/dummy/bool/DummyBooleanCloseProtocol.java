package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
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
  public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
    switch (round) {
      case 0:
        if (resourcePool.getMyId() == sender) {
          network.sendToAll(new byte[]{BooleanSerializer.toBytes(input.out())});
        }
        return EvaluationStatus.HAS_MORE_ROUNDS;
      case 1:
        boolean r = BooleanSerializer.fromBytes(network.receive(sender)[0]);
        this.output = new DummyBooleanSBool();
        this.output.setValue(r);
        return EvaluationStatus.IS_DONE;
      default:
        throw new MPCException("Bad round: " + round);
    }
  }

  @Override
  public SBool out() {
    return output;
  }
}
