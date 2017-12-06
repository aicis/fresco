package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

public class BatchEvaluationLoggingDecorator<ResourcePoolT extends ResourcePool>
    implements BatchEvaluationStrategy<ResourcePoolT>, PerformanceLogger {
  
  public static final String ID = "PARTY_ID";
  public static final String BATCH_COUNTER = "AMOUNT_OF_BATCHES";
  public static final String BATCH_NATIVE_PROTOCOLS = "TOTAL_AMOUNT";
  public static final String BATCH_MIN_PROTOCOLS = "MIN_AMOUNT_PER_BATCH";
  public static final String BATCH_MAX_PROTOCOLS = "MAX_AMOUNT_PER_BATCH";
  
  private BatchEvaluationStrategy<ResourcePoolT> delegate;
  private int counter = 0;
  private int noNativeProtocols = 0;
  private int minNoNativeProtocolsPerBatch = Integer.MAX_VALUE;
  private int maxNoNativeProtocolsPerBatch = 0;

  public BatchEvaluationLoggingDecorator(
      BatchEvaluationStrategy<ResourcePoolT> batchEvaluation) {
    this.delegate = batchEvaluation;
  }

  @Override
  public void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool,
      NetworkBatchDecorator network) {
    int size = protocols.size();
    this.counter++;
    noNativeProtocols += size;
    if (minNoNativeProtocolsPerBatch > size) {
      minNoNativeProtocolsPerBatch = size;
    }
    if (maxNoNativeProtocolsPerBatch < size) {
      maxNoNativeProtocolsPerBatch = size;
    }
    delegate.processBatch(protocols, resourcePool, network);
  }

  @Override
  public void printToLog(Logger log, int myId) {
    log.info(makeLogString(myId));
  }

  @Override
  public void reset() {
    counter = 0;
    noNativeProtocols = 0;
    minNoNativeProtocolsPerBatch = Integer.MAX_VALUE;
    maxNoNativeProtocolsPerBatch = 0;
  }

  @Override
  public Map<String, Object> getLoggedValues(int myId) {
    Map<String, Object> values = new HashMap<>();
    values.put(ID, myId);
    values.put(BATCH_COUNTER, counter);
    values.put(BATCH_NATIVE_PROTOCOLS, noNativeProtocols);
    values.put(BATCH_MIN_PROTOCOLS, minNoNativeProtocolsPerBatch);
    values.put(BATCH_MAX_PROTOCOLS, maxNoNativeProtocolsPerBatch);
    return values;
  }


}
