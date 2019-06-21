package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.SReal;

/**
 * Closed datatype for representing binary fixed point numbers, e.g. represent a fraction <i>x</i>
 * as <i>m 2<sup>n</sup></i> where <i>m</i> is an {@link SInt}, <i>n &ge; 0</i> is a precision
 * which is defined by the context and can be accessed through the {@link BasicNumericContext}.
 */
public class SFixed implements SReal, DRes<SReal> {
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
