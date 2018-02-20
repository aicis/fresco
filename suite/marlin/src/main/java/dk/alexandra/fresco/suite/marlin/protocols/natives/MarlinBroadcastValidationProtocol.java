package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.Collections;
import java.util.List;

public class MarlinBroadcastValidationProtocol<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> extends
    MarlinNativeProtocol<Void, H, L, T> {

  private final List<byte[]> input;
  private Broadcast broadcast;
  private byte[] digest;

  public MarlinBroadcastValidationProtocol(List<byte[]> input) {
    this.input = input;
  }

  public MarlinBroadcastValidationProtocol(byte[] input) {
    this(Collections.singletonList(input));
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<H, L, T> resourcePool,
      Network network) {
    if (resourcePool.getNoOfParties() <= 2) {
      return EvaluationStatus.IS_DONE;
    }
    if (round == 0) {
      broadcast = resourcePool.createBroadcast(network);
      digest = broadcast.computeAndSendDigests(input);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      broadcast.receiveAndValidateDigests(digest);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Void out() {
    return null;
  }

}
