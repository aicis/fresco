package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.SCENetworkSupplier;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;

/**
 * Interface which knows how to evaluate a "batch" of protocols. A batch is a collection of native
 * protocols which are functionally independent.
 * 
 * @author Kasper Damgaard
 *
 * @param <ResourcePoolT> The type of resource pool to use
 */
public interface BatchEvaluationStrategy<ResourcePoolT extends ResourcePool> {

  /**
   * @param protocols Array holding the protocols to be evaluated
   * @param resourcePool The resource pool.
   * @param network the SceNetwork used for the evaluation process.
   * 
   */
  <SceNetwork extends SCENetwork & SCENetworkSupplier> void processBatch(
      ProtocolCollection<ResourcePoolT> protocols, ResourcePoolT resourcePool, SceNetwork sceNetwork)
          throws IOException;
}
