package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * Central class that builds complex trees of protocol producers based on
 * the sequence in which they are created.
 * <p>This class stores the intention of building
 * a protocol producer rather than the actual protocol producer and only when requested
 * actually evaluates the closure and returns the actual protocol producer.</p>
 * <p>This class also exposes builders with an intuitive and readable api but
 * automatically creates native protocols and adds these to this protocol builder as
 * intentions to be resolved later</p>
 */
public interface ProtocolBuilder {

  /**
   * Building the actual protocol producer. Implementors decide which producer to create.
   *
   * @return the protocol producer that has been build
   */
  ProtocolProducer build();
}
