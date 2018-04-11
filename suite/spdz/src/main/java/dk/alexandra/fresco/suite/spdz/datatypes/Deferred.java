package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.DRes;

public class Deferred<T> implements DRes<T> {
  // TODO should this just be a future?

  private T value;

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
