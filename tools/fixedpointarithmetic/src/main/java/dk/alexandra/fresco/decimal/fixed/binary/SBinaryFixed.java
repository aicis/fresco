package dk.alexandra.fresco.decimal.fixed.binary;

import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Closed datatype for representing fixed point numbers, e.g. represent a fraction <i>x</i> as <i>n
 * 2<sub>e</sub></i> where <i>n</i> is an {@link SInt} and <i>e > 0</i> is a fixed precision.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SBinaryFixed implements SReal, DRes<SReal> {

  private final DRes<SInt> value;

  public SBinaryFixed(DRes<SInt> value) {
    this.value = value;
  }

  DRes<SInt> getSInt() {
    return value;
  }

  @Override
  public SBinaryFixed out() {
    return this;
  }

}
