package dk.alexandra.fresco.framework;

/**
 * Is the root of all calculations in FRESCO.
 * This is intended to open up for some protocols to be implemented by producers.
 * Both {@link NativeProtocol} and intermediate results can be a DRes.
 *
 * @param <OutputT> the type of resulting parameter
 */
public interface DRes<OutputT> {

  /**
   * Gets the result of this computation.
   *
   * @return the result
   */
  OutputT out();
}
