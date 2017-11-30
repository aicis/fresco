package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.time.Duration;
import java.util.concurrent.Future;

/**
 * Core class of the fresco system, this must be initialized with a concrete ResourcePool from the
 * corresponding protocol suite and the types version of the ProtocolBuilder - hereafter the
 * SecureComputationEngine takes the protocol and runs the matching Application with the
 * corresponding types (i.e. numeric or binary).
 *
 * @param <ResourcePoolT> the resource pool
 * @param <BuilderT> the typed version of the builder
 */
public interface SecureComputationEngine<ResourcePoolT extends ResourcePool,
    BuilderT extends ProtocolBuilder> {

  static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);

  /**
   * Executes an application based on the current configuration.
   * <p>
   * By default this method times out after waiting 10 minutes for output.
   * </p>
   *
   * @param application the application to evaluate
   * @param resources the resource pool used in evaluation
   * @param network the network
   * @return output of the application
   */
  default <OutputT> OutputT runApplication(Application<OutputT, BuilderT> application,
      ResourcePoolT resources, Network network) {
    return runApplication(application, resources, network, DEFAULT_TIMEOUT);
  }

  /**
   * Executes an application based on the current configuration.
   *
   * @param application the application to evaluate
   * @param resources the resource pool used in evaluation
   * @param network the network
   * @param timeout time to wait for application output
   * @return output of the application
   */
  <OutputT> OutputT runApplication(Application<OutputT, BuilderT> application,
      ResourcePoolT resources, Network network, Duration timeout);

  /**
   * Executes an application based on the current SCEConfiguration. If the SecureComputationEngine
   * is not setup before (e.g. connected to other parties etc.), the SecureComputationEngine will do
   * the setup phase before running the application. <br/>
   * In a normal application this should be the normal way to start an application since there need
   * to be allocated resources (the resource pool) and allowed for parallel work.
   *
   * @param application The application to evaluate.
   * @param resources the resource pool used in evaluation
   * @param network the network to run the computations on
   * @return the future holding the result
   */
  <OutputT> Future<OutputT> startApplication(Application<OutputT, BuilderT> application,
      ResourcePoolT resources, Network network);


  /**
   * Initializes the SecureComputationEngine. This method is idempotent - and sets up the proces
   * queue and initializes the protocol suite.
   */
  void setup();

  /**
   * Ensures that resources held managed by this engine are shut down properly.
   * <p>
   * Note that this does not go for any external resources such as the network or resource pool.
   * </p>
   */
  void shutdownSCE();


}
