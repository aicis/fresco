package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;

public class BatchedSequentialEvaluator<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder>
    implements ProtocolEvaluator<ResourcePoolT, Builder> {

  private static final int DEFAULT_CHANNEL = 0;

  private int maxBatchSize;

  private ProtocolSuite<ResourcePoolT, ?> protocolSuite;

  BatchedSequentialEvaluator() {
    this.maxBatchSize = 4096;
  }

  @Override
  public void setProtocolInvocation(ProtocolSuite<ResourcePoolT, Builder> pii) {
    this.protocolSuite = pii;
  }

  /**
   * Sets the maximum amount of gates evaluated in each batch.
   *
   * @param maxBatchSize the maximum batch size.
   */
  @Override
  public void setMaxBatchSize(int maxBatchSize) {
    this.maxBatchSize = maxBatchSize;
  }

  public void eval(ProtocolProducer protocolProducer, ResourcePoolT resourcePool)
      throws IOException {
    SCENetworkImpl sceNetwork = createSceNetwork(resourcePool);
    ProtocolSuite.RoundSynchronization<ResourcePoolT> roundSynchronization =
        protocolSuite.createRoundSynchronization();
    do {
      ProtocolCollectionList<ResourcePoolT> protocols =
          new ProtocolCollectionList<>(maxBatchSize);
      protocolProducer.getNextProtocols(protocols);
      int size = protocols.size();

      BatchedStrategy.processBatch(protocols, sceNetwork, DEFAULT_CHANNEL, resourcePool);
      roundSynchronization.finishedBatch(size, resourcePool, sceNetwork);
    } while (protocolProducer.hasNextProtocols());

    roundSynchronization.finishedEval(resourcePool, sceNetwork);
  }

  private SCENetworkImpl createSceNetwork(ResourcePool resourcePool) {
    return new SCENetworkImpl(resourcePool.getNoOfParties());
  }
}
