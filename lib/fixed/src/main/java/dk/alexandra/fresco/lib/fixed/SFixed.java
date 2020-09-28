package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Closed datatype for representing binary fixed point numbers, e.g. represent a fraction <i>x</i>
 * as <i>m 2<sup>n</sup></i> where <i>m</i> is an {@link SInt}, <i>n &ge; 0</i> is a precision which
 * is defined by the context.
 */
public class SFixed implements DRes<SFixed> {
  private final DRes<SInt> value;

  public SFixed(DRes<SInt> value) {
    this.value = value;
  }

  public DRes<SInt> getSInt() {
    return value;
  }

  @Override
  public SFixed out() {
    return this;
  }
}
