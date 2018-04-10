package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class DeferredSInt implements DRes<SInt> {
  // TODO should this just be a future?

  private SInt value;

  public void callback(SInt value) {
    if (this.value != null) {
      throw new IllegalArgumentException("Value already assigned");
    }
    this.value = value;
  }

  @Override
  public SInt out() {
    return value;
  }

}
