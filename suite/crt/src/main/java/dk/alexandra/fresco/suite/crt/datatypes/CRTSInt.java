package dk.alexandra.fresco.suite.crt.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class CRTSInt implements SInt {

  private final DRes<SInt> x, y;

  public CRTSInt(DRes<SInt> x, DRes<SInt> y) {
    this.x = x;
    this.y = y;
  }

  public DRes<SInt> getLeft() {
    return x;
  }

  public DRes<SInt> getRight() {
    return y;
  }

  @Override
  public SInt out() {
    return this;
  }

}
