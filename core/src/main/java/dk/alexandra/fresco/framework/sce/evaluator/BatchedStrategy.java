package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Iterator;

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
      NetworkBatchDecorator networkBatchDecorator) {
    int round = 0;
    while (protocols.size() > 0) {
      evaluateCurrentRound(protocols, networkBatchDecorator, resourcePool, round);
      networkBatchDecorator.flush();
      round++;
    }
  }

  private void evaluateCurrentRound(
      ProtocolCollection<ResourcePoolT> protocols, Network sceNetwork,
      ResourcePoolT rp, int round) {
    Iterator<NativeProtocol<?, ResourcePoolT>> iterator = protocols.iterator();
    while (iterator.hasNext()) {
      NativeProtocol<?, ResourcePoolT> protocol = iterator.next();
      EvaluationStatus status = protocol.evaluate(round, rp, sceNetwork);
      if (status.equals(EvaluationStatus.IS_DONE)) {
        iterator.remove();
      }
    }
  }
}
