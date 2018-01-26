package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * An evaluator is responsible for evaluating each native protocol. Every computation is reduced to
 * a number of native protocols. These are then evaluated in batches (the amount of native
 * protocols per batch can be configured by the user).
 *
 * @param <ResourcePoolT> The type of resource pool
 */
public interface ProtocolEvaluator<ResourcePoolT extends ResourcePool> {

  /**
   * Evaluates all gates produced by a ProtocolProducer.
   *
   * @param protocolProducer the protocol producer to evaluate
   * @param resourcePool the resource pool (for other resources than network)
   * @param network network to use for the evaluation
   * @return the overall statistics about the evaluation
   */
  EvaluationStatistics eval(
      ProtocolProducer protocolProducer, ResourcePoolT resourcePool, Network network);

  /**
   * Overall statistics about protocol evaluation
   */
  class EvaluationStatistics {

    private final int nativeProtocols;
    private final int batches;

    /**
     * Createes a new statistics object.
     *
     * @param nativeProtocols the total number of native protocols in evaluation
     * @param batches the total of batches in the evaluation
     */
    public EvaluationStatistics(int nativeProtocols, int batches) {

      this.nativeProtocols = nativeProtocols;
      this.batches = batches;
    }

    /**
     * Returns the total number of native protocols in evaluation.
     *
     * @return the total number of native protocols in evaluation
     */
    public int getNativeProtocols() {
      return nativeProtocols;
    }

    /**
     * Returns the total of batches in the evaluation.
     *
     * @return the total of batches in the evaluation
     */
    public int getBatches() {
      return batches;
    }
  }
}
