package dk.alexandra.fresco.framework;

/**
 * Is the root of all calculations in fresco.
 * This is intended to open up for some protocols to be implemented by producers.
 * Both NativeProtocol and ProtocolProducer can be a computation
 *
 * @param <OutputT> the type of resulting parameter
 */
public interface Computation<OutputT> {

  /**
   * Gets the result of this computation.
   *
   * @return the result
   */
  OutputT out();
}
