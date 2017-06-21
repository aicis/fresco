package dk.alexandra.fresco.framework;

/**
 * The core factory to implement when creating a protocol.
 * Currently exists in two variants - the numeric and the binary
 */
public interface BuilderFactory {

  /**
   * Legacy method for getting the multiple inheritance factroy that is used as an outset when
   * building protocols
   *
   * @return the factroy.
   */
  ProtocolFactory getProtocolFactory();

}
