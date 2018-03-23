package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.spdz2k.resource.SecureBroadcastUtil;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Collections;
import java.util.List;

/**
 * Generic native protocol implementing validation of previously received broadcast. <p>Used as a
 * building block in {@link dk.alexandra.fresco.suite.spdz2k.protocols.computations.BroadcastComputation}.</p>
 */
public class BroadcastValidationProtocol<ResourcePoolT extends ResourcePool> implements
    NativeProtocol<Void, ResourcePoolT> {

  private final List<byte[]> input;
  private SecureBroadcastUtil broadcast;
  private byte[] digest;

  /**
   * Creates new {@link BroadcastValidationProtocol}.
   *
   * @param input inputs received that need to be validated.
   */
  public BroadcastValidationProtocol(List<byte[]> input) {
    this.input = input;
  }

  public BroadcastValidationProtocol(byte[] input) {
    this(Collections.singletonList(input));
  }

  @Override
  public Void out() {
    throw new IllegalStateException("out() called on native protocol with void return");
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolT resourcePool, Network network) {
    if (resourcePool.getNoOfParties() <= 2) {
      return EvaluationStatus.IS_DONE;
    }
    if (round == 0) {
      broadcast = new SecureBroadcastUtil(network);
      digest = broadcast.computeAndSendDigests(input);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      broadcast.receiveAndValidateDigests(digest);
      return EvaluationStatus.IS_DONE;
    }
  }
}
