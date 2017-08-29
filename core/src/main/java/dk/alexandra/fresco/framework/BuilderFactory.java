package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;

/**
 * The core factory to implement when creating a protocol. Currently exists in two variants - the
 * numeric and the binary
 */
public interface BuilderFactory<BuilderT extends ProtocolBuilder> {

  /**
   * Creates a version of the Builder that matches the types of this factory. This builder will
   * execute the appended protocols in sequence.
   *
   * @return the builder to be used by the SecureComputationEngine
   */
  BuilderT createSequential();

  /**
   * Creates a version of the Builder that matches the types of this factory. This builder will
   * execute the appended protocols in parallel (larger batches).
   *
   * @return the builder to be used by the SecureComputationEngine
   */
  BuilderT createParallel();
}
