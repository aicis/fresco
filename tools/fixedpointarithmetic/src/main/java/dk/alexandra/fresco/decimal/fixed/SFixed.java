package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Closed datatype for representing binary fixed point numbers, e.g. represent a fraction <i>x</i> as <i>n
 * 2<sup>e</sup></i> where <i>n</i> is an {@link SInt}, <i>e &ge; 0</i> is a precision (avaialble
 * via {@link #getScale()}) that may vary from value to value.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SFixed implements SReal, DRes<SReal> {
  private final DRes<SInt> value;
  private int scale;

  public SFixed(DRes<SInt> value, int scale) {
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
  public SFixed out() {
    return this;
  }
}
