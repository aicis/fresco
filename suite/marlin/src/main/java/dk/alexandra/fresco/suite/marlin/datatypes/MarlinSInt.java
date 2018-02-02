package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.value.SInt;

public class MarlinSInt<T extends BigUInt<T>> implements SInt {

  private final MarlinElement<T> value;

  public MarlinSInt(MarlinElement<T> value) {
    this.value = value;
  }

  @Override
  public SInt out() {
    return this;
  }

  public MarlinElement<T> getValue() {
    return value;
  }

}
