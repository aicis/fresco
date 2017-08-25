package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;

/**
 * The core factory to implement when creating a protocol. Currently exists in two variants - the
 * numeric and the binary
 */
public interface BuilderFactory<Builder extends ProtocolBuilder,
    BuilderParallelT extends ProtocolBuilder> {

  /**
   * Legacy method for getting the multiple inheritance factroy that is used as an outset when
   * building protocols
   *
   * @return the factroy.
   */
  ProtocolFactory getProtocolFactory();

  /**
   * Creates a version of the Builder that matches the types of this factory.
   *
   * @return the builder to be used by the SecureComputationEngine
   */
  Builder createSequential();

  BuilderParallelT createParallel();
}
