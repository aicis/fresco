package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.SCENetworkSupplier;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class SequentialStrategy<ResourcePoolT extends ResourcePool> implements BatchEvaluationStrategy<ResourcePoolT>{

  @Override
  public <SceNetwork extends SCENetwork & SCENetworkSupplier> void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool, SceNetwork sceNetwork)
          throws IOException {
    Network network = resourcePool.getNetwork();
    for (NativeProtocol<?, ResourcePoolT> protocol : protocols) {
      int round = 0;
      EvaluationStatus status;
      do {
        status = protocol.evaluate(round, resourcePool, sceNetwork);
        // send phase
        Map<Integer, byte[]> output = sceNetwork.getOutputFromThisRound();
        for (int pId : output.keySet()) {
          // send array since queue is not serializable
          network.send(0, pId, output.get(pId));
        }

        // receive phase
        Map<Integer, ByteBuffer> inputForThisRound = new HashMap<>();
        for (int pId : sceNetwork.getExpectedInputForNextRound()) {
          byte[] messages = network.receive(0, pId);
          inputForThisRound.put(pId, ByteBuffer.wrap(messages));
        }
        sceNetwork.setInput(inputForThisRound);
        sceNetwork.nextRound();
        round++;
      } while (status.equals(EvaluationStatus.HAS_MORE_ROUNDS));
    }
  }

}
