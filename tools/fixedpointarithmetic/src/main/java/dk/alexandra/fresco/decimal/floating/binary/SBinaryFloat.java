package dk.alexandra.fresco.decimal.floating.binary;

import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Closed datatype for representing fixed point numbers, e.g. represent a fraction <i>x</i> as <i>n
 * 2<sub>e</sub></i> where <i>n</i> is an {@link SInt} and <i>e > 0</i> is a precision (avaialble
 * via {@link #getScale()}) that may vary from value to value.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
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
