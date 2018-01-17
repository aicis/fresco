package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class SFixedSIntWrapper implements SFixed, DRes<SFixedSIntWrapper> {

  private DRes<SInt> value;

  public SFixedSIntWrapper(DRes<SInt> value) {
    this.value = value;
  }

  DRes<SInt> getSInt() {
    return value;
  }

  @Override
  public SFixedSIntWrapper out() {
    return this;
  }
}
