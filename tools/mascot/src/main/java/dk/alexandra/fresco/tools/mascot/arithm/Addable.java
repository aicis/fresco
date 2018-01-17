package dk.alexandra.fresco.tools.mascot.arithm;

/**
 * An implementing class must support arithmetic addition with instances of type {@link T}.
 * @param <T>
 */
public interface Addable<T> {

  T add(T other);
  
}
