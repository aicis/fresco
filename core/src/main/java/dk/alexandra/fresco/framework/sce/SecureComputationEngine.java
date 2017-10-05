package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.concurrent.Future;

/**
 * Core class of the fresco system, this must be initialized with a concrete ResourcePool
 * from the corresponding protocol suite and the types version of the ProtocolBuilder - hereafter
 * the SecureComputationEngine takes the protocol and runs the matching Application with the
 * corresponding types (i.e. numeric or binary).
 *
 * @param <ResourcePoolT> the resource pool
 * @param <Builder> the typed version of the builder
 */
public interface SecureComputationEngine<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

  /**
   * Executes an application based on the current configuration.
   *
   * @param application The application to evaluate.
   */
  <OutputT> OutputT runApplication(
      Application<OutputT, Builder> application,
      ResourcePoolT resources);

  /**
   * Executes an application based on the current SCEConfiguration. If the SecureComputationEngine
   * is not setup before (e.g. connected to other parties etc.), the SecureComputationEngine will
   * do the setup phase before running the application.
   * <br/>
   * In a normal application this should be the normal way to start an application since there
   * need to be allocated resources (the resource pool) and allowed for parallel work.
   *
   * @param application The application to evaluate.
   * @return the future holding the result
   */
  <OutputT> Future<OutputT> startApplication(
      Application<OutputT, Builder> application,
      ResourcePoolT resources);


  /**
   * Initializes the SecureComputationEngine. This method is idempotent - and sets up the
   * proces queue and initializes the protocol suite.
   */
  void setup();

  /**
   * Ensures that resources are shut down properly. Network is disconnected
   * and sockets are released.
   */
  void shutdownSCE();
}