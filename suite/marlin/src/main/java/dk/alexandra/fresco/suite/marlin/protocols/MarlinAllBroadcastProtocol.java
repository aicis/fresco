package dk.alexandra.fresco.suite.marlin.protocols;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.List;

public class MarlinAllBroadcastProtocol<T extends BigUInt<T>> extends
    MarlinNativeProtocol<List<byte[]>, T> {

  private final byte[] input;
  private List<byte[]> result;

  public MarlinAllBroadcastProtocol(byte[] input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool, Network network) {
    if (round == 0) {
      network.sendToAll(input.clone());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      result = network.receiveFromAll();
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public List<byte[]> out() {
    return result;
  }

}
