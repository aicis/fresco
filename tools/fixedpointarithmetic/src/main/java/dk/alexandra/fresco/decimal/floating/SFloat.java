package dk.alexandra.fresco.decimal.floating;

import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class SFloat implements SReal, DRes<SReal> {
  private final DRes<SInt> value;
  private int scale;

  public SFloat(DRes<SInt> value, int scale) {
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
  public SFloat out() {
    return this;
  }
}
