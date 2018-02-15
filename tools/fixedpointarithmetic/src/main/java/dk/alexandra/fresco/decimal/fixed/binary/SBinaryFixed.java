package dk.alexandra.fresco.decimal.fixed.binary;

import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class SBinaryFixed implements SReal, DRes<SReal> {

  private final DRes<SInt> value;

  public SBinaryFixed(DRes<SInt> value) {
    this.value = value;
  }

  DRes<SInt> getSInt() {
    return value;
  }

  @Override
  public SBinaryFixed out() {
    return this;
  }

}
