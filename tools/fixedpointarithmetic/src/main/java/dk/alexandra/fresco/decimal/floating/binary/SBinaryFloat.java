package dk.alexandra.fresco.decimal.floating.binary;

import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class SBinaryFloat implements SReal, DRes<SReal> {
  private final DRes<SInt> value;
  private int scale;

  public SBinaryFloat(DRes<SInt> value, int scale) {
    this.value = value;
    this.scale = scale;
  }

  DRes<SInt> getSInt() {
    return value;
  }

  int getScale() {
    return scale;
  }

  @Override
  public SBinaryFloat out() {
    return this;
  }
}
