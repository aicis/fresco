package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol evaluator implementation which works by evaluating native protocols in batches of a
 * certain batch size. How each batch is evaluated is up to the given batch evaluation strategy.
 * Each batch is required to contain only functionally independent native protocols.
 *
 * @param <ResourcePoolT> The resource pool type to use
 * @param <Builder> The builder type used
 */
public class BatchedProtocolEvaluator<
  ResourcePoolT extends ResourcePool, 
  Builder extends ProtocolBuilder
  >
    implements ProtocolEvaluator<ResourcePoolT, Builder> {

  private Logger logger = LoggerFactory.getLogger(BatchedProtocolEvaluator.class);
  private int maxBatchSize;
  private static final int MAX_EMPTY_BATCHES_IN_A_ROW = 10;

  private ProtocolSuite<ResourcePoolT, ?> protocolSuite;
  private BatchEvaluationStrategy<ResourcePoolT> batchEvaluator;

  public BatchedProtocolEvaluator(BatchEvaluationStrategy<ResourcePoolT> batchEvaluator) {
    this.batchEvaluator = batchEvaluator;
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

  @Override
  public void eval(ProtocolProducer protocolProducer, ResourcePoolT resourcePool)
      throws IOException {
    int batch = 0;
    int totalProtocols = 0;
    int totalBatches = 0;
    int zeroBatches = 0;

    SCENetworkImpl sceNetwork = createSceNetwork(resourcePool);
    ProtocolSuite.RoundSynchronization<ResourcePoolT> roundSynchronization =
        protocolSuite.createRoundSynchronization();
    do {
      ProtocolCollectionList<ResourcePoolT> protocols = new ProtocolCollectionList<>(maxBatchSize);
      protocolProducer.getNextProtocols(protocols);
      int size = protocols.size();

      roundSynchronization.beforeBatch(protocols, resourcePool);
      batchEvaluator.processBatch(protocols, resourcePool, sceNetwork);
      logger.trace("Done evaluating batch: " + batch++ + " with " + size + " native protocols");
      if (size == 0) {
        logger.debug("Batch " + batch + " is empty");
      }
      totalProtocols += size;
      totalBatches += 1;
      if (size == 0) {
        zeroBatches++;
      } else {
        zeroBatches = 0;
      }
      if (zeroBatches > MAX_EMPTY_BATCHES_IN_A_ROW) {
        throw new MPCException("Number of empty batches in a row reached "
            + MAX_EMPTY_BATCHES_IN_A_ROW + "; probably there is a bug in your protocol producer.");
      }
      roundSynchronization.finishedBatch(size, resourcePool, sceNetwork);
    } while (protocolProducer.hasNextProtocols());

    logger.debug("Evaluator done. Evaluated a total of " + totalProtocols
        + " native protocols in " + totalBatches + " batches.");
    roundSynchronization.finishedEval(resourcePool, sceNetwork);
  }

  private SCENetworkImpl createSceNetwork(ResourcePool resourcePool) {
    return new SCENetworkImpl(resourcePool.getNoOfParties(), resourcePool.getNetwork());
  }
}
