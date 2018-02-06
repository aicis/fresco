package dk.alexandra.fresco.framework;

/**
 * Is the root of all calculations in FRESCO. This class represents a deferred result, meaning that
 * the result cannot be guaranteed to be present before an evaluation has taken place. This is
 * intended to open up for some protocols to be implemented by producers. Both
 * {@link NativeProtocol} and intermediate results can be a DRes.
 *
 * @param <OutputT> the type of resulting parameter
 */
public interface DRes<OutputT> {

  /**
   * Gets the result of this computation. The result might not be known before evaluation has
   * reached and touched this result. i.e. calling out() before having evaluated the protocol
   * resulting in this deferred result will return null.
   *
   * @return the result
   */
  OutputT out();
}
