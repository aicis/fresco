package dk.alexandra.fresco.framework;

/**
 * Class representing a deferred results. <p>In addition to {@link DRes}, this also has a callback
 * method.</p>
 */
public class Deferred<T> implements DRes<T> {
  // TODO should this just be a future?

  private T value;

  /**
   * Assigns actual value. <p>Throws if value has already been assigned.</p>
   */
  public void callback(T value) {
    if (this.value != null) {
      throw new IllegalArgumentException("Value already assigned");
    }
    this.value = value;
  }

  @Override
  public T out() {
    return value;
  }

}
