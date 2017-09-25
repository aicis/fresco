package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.SCENetworkSupplier;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;

public class BatchEvaluationPerformanceDelegate<
  ResourcePoolT extends ResourcePool, 
  Builder extends ProtocolBuilder
  > 
  extends PerformanceLogger
  implements BatchEvaluationStrategy<ResourcePoolT>  {

  private BatchEvaluationStrategy<ResourcePoolT> batchEvaluation;
  private int noNativeProtocols = 0;
  private int minNoNativeProtocolsPerBatch = Integer.MAX_VALUE;
  private int maxNoNativeProtocolsPerBatch = 0;

  public BatchEvaluationPerformanceDelegate(BatchEvaluationStrategy<ResourcePoolT> batchEvaluation, int myId) {
    super(myId);
    this.batchEvaluation = batchEvaluation;
  }

  @Override
  public <sceNetwork extends SCENetwork & SCENetworkSupplier> void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool, sceNetwork network)
          throws IOException {
    int size = protocols.size();
    this.counter++;
    noNativeProtocols += size;
    if (minNoNativeProtocolsPerBatch > size) {
      minNoNativeProtocolsPerBatch = size;
    }
    if (maxNoNativeProtocolsPerBatch < size) {
      maxNoNativeProtocolsPerBatch = size;
    }
    batchEvaluation.processBatch(protocols, resourcePool, network);
  }

  @Override
  public void printPerformanceLog() {
    log.info("=== P"+this.myId+": Native protocols per batch metrics ===");
    if (counter == 0) {
      log.info("No batches were recorded");
    } else {
      log.info("Total amount of batches reached: " + counter);
      log.info("Total amount of native protocols evaluated: " + noNativeProtocols);
      log.info("minimum amount of native protocols evaluated in a single batch: "
          + minNoNativeProtocolsPerBatch);
      log.info("maximum amount of native protocols evaluated in a single batch: "
          + maxNoNativeProtocolsPerBatch);
      double avg = noNativeProtocols / (double) counter;
      log.info("Average amount of native protocols evaluated per batch: " + df.format(avg));
    }
  }

  @Override
  public void reset() {
    counter = 0;
    noNativeProtocols = 0;
    minNoNativeProtocolsPerBatch = Integer.MAX_VALUE;
    maxNoNativeProtocolsPerBatch = 0;
  }
  
  
}
