package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.*;

public class SFixed {

  private DRes<SInt> value;
  
  public SFixed(SInt value) {
    this.value = value;
  }
  public SFixed(DRes<SInt> value) {
    this.value = value;
  }
  
  public DRes<SInt> getSInt() {
    return value;
  }

}
