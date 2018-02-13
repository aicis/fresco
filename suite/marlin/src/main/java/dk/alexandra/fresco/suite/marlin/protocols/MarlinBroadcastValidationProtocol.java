package dk.alexandra.fresco.suite.marlin.protocols;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.Collections;
import java.util.List;

public class MarlinBroadcastProtocol<T extends BigUInt<T>> extends
    MarlinNativeProtocol<List<T>, T> {
  private final List<T> input;
  private Broadcast broadcast;
  private byte[] digest;
  private List<T> result;

  public MarlinBroadcastProtocol(List<T> input) {
    this.input = input;
  }

  public MarlinBroadcastProtocol(T input) {
    this(Collections.singletonList(input));
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool, Network network) {
    if (resourcePool.getNoOfParties() <= 2) {
      this.result = input;
      return EvaluationStatus.IS_DONE;
    }
    // with more than two parties we actually need to run broadcast validation
    if (round == 0) {
      broadcast = resourcePool.getBroadcast(network);
      digest = broadcast.computeAndSendDigests(resourcePool.getRawSerializer().serialize(input));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      broadcast.receiveAndValidateDigests(digest);
      result = input; // TODO possibly need to copy
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public List<T> out() {
    return result;
  }

}
