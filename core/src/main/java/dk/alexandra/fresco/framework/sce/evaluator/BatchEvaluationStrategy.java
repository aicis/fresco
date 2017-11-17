package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * Interface which knows how to evaluate a "batch" of protocols. A batch is a collection of native
 * protocols which are functionally independent.
 *
 * @param <ResourcePoolT> The type of resource pool to use
 */
public interface BatchEvaluationStrategy<ResourcePoolT extends ResourcePool> {

  /**
   * @param protocols Array holding the protocols to be evaluated
   * @param resourcePool The resource pool.
   * @param network the Network used for the evaluation process.
   */
  void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool,
      NetworkBatchDecorator network);
}
