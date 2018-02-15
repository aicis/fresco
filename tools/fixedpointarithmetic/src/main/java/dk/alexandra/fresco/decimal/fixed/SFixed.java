package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class SFixed implements SReal, DRes<SReal> {

  private final DRes<SInt> value;

  public SFixed(DRes<SInt> value) {
    this.value = value;
  }

  DRes<SInt> getSInt() {
    return value;
  }

  @Override
  public SFixed out() {
    return this;
  }

}
