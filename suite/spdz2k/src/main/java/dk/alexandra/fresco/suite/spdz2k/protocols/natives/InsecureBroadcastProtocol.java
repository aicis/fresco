package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.List;

/**
 * Generic native protocol implementing insecure broadcast. <p>Used as a building block in {@link
 * dk.alexandra.fresco.suite.spdz2k.protocols.computations.BroadcastComputation}.</p>
 */
public class InsecureBroadcastProtocol<ResourcePoolT extends ResourcePool> implements
    NativeProtocol<List<byte[]>, ResourcePoolT> {

  private final byte[] input;
  private List<byte[]> result;

  /**
   * Creates new {@link InsecureBroadcastProtocol}.
   *
   * @param input own input to broadcast
   */
  public InsecureBroadcastProtocol(byte[] input) {
    this.input = input;
  }

  @Override
  public List<byte[]> out() {
    return result;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolT resourcePool, Network network) {
    if (round == 0) {
      network.sendToAll(input.clone());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      result = network.receiveFromAll();
      return EvaluationStatus.IS_DONE;
    }
  }
}
