package dk.alexandra.fresco.suite.marlin.datatypes;

public interface BigUIntFactory<T extends BigUInt<T>> {

  /**
   * Creates new {@link T} from a raw array of bytes.
   */
  T createFromBytes(byte[] bytes);

  /**
   * Creates random {@link T}.
   */
  T createRandom();

}
