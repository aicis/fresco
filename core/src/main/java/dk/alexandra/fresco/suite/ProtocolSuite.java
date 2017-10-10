package dk.alexandra.fresco.suite;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;

public interface ProtocolSuite<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

  /**
   * Initializes the protocol suite by supplying any needed resources to the protocol suite. The
   * protocol invocation implementation is then in charge of supplying the needed resources to it's
   * internal protocols when needed.
   */
  BuilderFactory<Builder> init(ResourcePoolT resourcePool);

  /**
   * Get a RoundSynchronization used by evaluators to signal progress and allow protocols to do
   * additional work during evaluation. Only RoundSynchronization.finishedBatch is guaranteed to be
   * called by the evaluator.
   *
   * @return a RoundSynchronization that can be used by current evaluation.
   */
  RoundSynchronization<ResourcePoolT> createRoundSynchronization();

  interface RoundSynchronization<ResourcePoolT> {

    /**
     * Let's the protocol suite know that now is a possible point of synchronization. The invariant
     * is that all threads are done executing. This means that no network connections are busy any
     * more as all gates up until now has been evaluated.
     *
     * @param gatesEvaluated Indicates how many gates was evaluated since last call to synchronize.
     *        It is therefore _not_ indicative of a total amount.
     */
    void finishedBatch(int gatesEvaluated, ResourcePoolT resourcePool, SCENetwork sceNetwork)
        throws IOException;

    /**
     * Let the protocol suite know that the evaluation has reached it's end. Runtime can then do
     * cleanup or resume background activities if needed.
     */
    void finishedEval(ResourcePoolT resourcePool, SCENetwork sceNetwork) throws IOException;
  }

  /**
   * Dummy round synchronization that does nothing.
   */
  class DummyRoundSynchronization<ResourcePoolT extends ResourcePool>
      implements RoundSynchronization<ResourcePoolT> {

    @Override
    public void finishedBatch(int gatesEvaluated, ResourcePoolT resourcePool,
        SCENetwork sceNetwork) {

    }

    @Override
    public void finishedEval(ResourcePoolT resourcePool, SCENetwork sceNetwork) {

    }
  }
}
