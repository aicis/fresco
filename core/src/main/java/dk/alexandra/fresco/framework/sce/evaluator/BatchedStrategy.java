package dk.alexandra.fresco.framework.sce.evaluator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * This class implements the core of a general batched communication strategy for evaluating
 * Protocols. In this strategy a number of Protocols will be evaluated round by round in such a way
 * that the communication of all Protocols is collected and batched together between rounds. More
 * precisely the process is as follows for a batch of Protocols:
 * <p>
 * 1. Evaluate the next round of all Protocols and collect messages to be sent in this round.
 * </p>
 * <p>
 * 2. Send all messages collected in step 1.
 * </p>
 * <p>
 * 3. Recieve all messages expected before the next round.
 * </p>
 * <p>
 * 4. If there are Protocols that are not done start over at step 1.
 * </p>
 * <p>
 * The processing is done is in a sequential manner (i.e. no parallelization).
 * </p>
 */
public class BatchedStrategy<ResourcePoolT extends ResourcePool>
    implements BatchEvaluationStrategy<ResourcePoolT> {

  @Override
  public void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool,
      SCENetwork sceNetwork)
          throws IOException {
    Network network = resourcePool.getNetwork();
    int round = 0;
    while (protocols.size() > 0) {
      evaluateCurrentRound(protocols, sceNetwork, 0, resourcePool, network, round);

      round++;
    }
  }  

  private void evaluateCurrentRound(
      ProtocolCollection<ResourcePoolT> protocols, SCENetwork sceNetwork, int channel,
      ResourcePoolT rp, Network network, int round) throws IOException {
    Iterator<NativeProtocol<?, ResourcePoolT>> iterator = protocols.iterator();
    while (iterator.hasNext()) {
      NativeProtocol<?, ResourcePoolT> protocol = iterator.next();
      EvaluationStatus status = protocol.evaluate(round, rp, sceNetwork);
      if (status.equals(EvaluationStatus.IS_DONE)) {
        iterator.remove();
      }
    }    
    // Send/Receive data for this round if SCENetwork is a supplier
    Map<Integer, ByteBuffer> inputs = new HashMap<>();

    // Send data
    Map<Integer, byte[]> output = sceNetwork.getOutputFromThisRound();
    for (Map.Entry<Integer, byte[]> e : output.entrySet()) {
      network.send(channel, e.getKey(), e.getValue());
    }

    // receive data
    Set<Integer> expected = sceNetwork.getExpectedInputForNextRound();
    for (int i : expected) {
      byte[] data = network.receive(channel, i);
      inputs.put(i, ByteBuffer.wrap(data));
    }

    sceNetwork.setInput(inputs);
    sceNetwork.nextRound();
  }
}
