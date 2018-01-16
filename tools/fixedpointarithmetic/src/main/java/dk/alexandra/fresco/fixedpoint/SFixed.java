package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Wrapper for SInt used to represent fixed point values.
 */
public class SFixed {

  private DRes<SInt> value;
  
  public SFixed(DRes<SInt> value) {
    this.value = value;
  }
  
  public DRes<SInt> getSInt() {
    return value;
  }

}
