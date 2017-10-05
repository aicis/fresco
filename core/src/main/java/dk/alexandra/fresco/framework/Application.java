package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;

/**
 * The Application interface should be implemented by all MPC applications that should run within
 * FRESCO. Application developers choose between the different type of builders and the output type
 * the application should produce. The developer is then provided with a method to fill out and a
 * builder to use of the specified type. This builder can then be used to construct the application.
 * 
 * @param <OutputT> The output type
 * @param <Builder> The builder type (i.e. currently either binary or arithmetic)
 */
public interface Application<OutputT, Builder extends ProtocolBuilder>
    extends Computation<OutputT, Builder> {

  /**
   * Closes the application and allows the output to be produced and allocated resources to be
   * released.
   */
  default void close() {

  }
}
