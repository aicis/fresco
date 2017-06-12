package dk.alexandra.fresco.framework;

/**
 * Decorates a ProtocolFactory with the added functionality to append the created protocols
 * into a producer. Implementors should allow for sub factory creation that conveys a tree of
 * producers.
 */
public interface AppendingFactory<T extends ProtocolFactory> {

  /**
   * Re-creates this factory based on a parallel protocol producer inserted into the original
   * protocot producer.
   *
   * @return the new-ly created factory
   */
  T createParallelSubFactory();

  /**
   * Re-creates this factory based on a sequential protocol producer inserted into the original
   * protocot producer.
   *
   * @return the new-ly created factory
   */
  T createSequentialSubFactory();
}
