package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * Native protocols should be implemented by the protocol suites developer. They are specific to a
 * single protocol suite and describes how to do a basic operation such as addition or XOR. They can
 * also be more advanced if the protocol suite has a specifically good way to do e.g. comparison.
 * 
 * @param <OutputT> The output type produced
 * @param <ResourcePoolT> The resource pool type
 */
public interface NativeProtocol<OutputT, ResourcePoolT extends ResourcePool> extends
    DRes<OutputT> {

  enum EvaluationStatus {
    IS_DONE, HAS_MORE_ROUNDS
  }

  /**
   * One round of evaluating the gate. Each round consist of only local computation.
   *
   * @param round Number of current round, starting with round 0.
   * @param resourcePool available resources can be found here. This also includes a threadpool if
   *        needed. It is advised to use only resources found here instead of creating them
   *        yourself. It is strongly advised not to use the network found here, but instead use the
   *        protocol network.
   * @param network A protocol's view of the network. This network does not immediately send data,
   *        but queues it for later use by the one calling this function.
   * @return HAS_MORE_ROUNDS if there are more rounds, i.e., if evaluate needs to be called again,
   *         IS_DONE if this is the last round.
   */
  EvaluationStatus evaluate(int round, ResourcePoolT resourcePool, Network network);

}
