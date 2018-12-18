package dk.alexandra.fresco.suite.spdz.datatypes;

import java.io.Serializable;

public class SpdzTriple implements Serializable {

  private static final long serialVersionUID = -4394957717364446774L;

  private final SpdzSInt elementA;
  private final SpdzSInt elementB;
  private final SpdzSInt elementC;

  /**
   * Construct a new SpdzTriple. The triple contains 3 values A, B and C
   * such that A * B = C. 
   * 
   * @param elementA A
   * @param elementB B
   * @param elementC C
   */
  public SpdzTriple(SpdzSInt elementA, SpdzSInt elementB, SpdzSInt elementC) {
    this.elementA = elementA;
    this.elementB = elementB;
    this.elementC = elementC;
  }

  /**
   * Constructor only used to serialization.
   */
  public SpdzTriple() {
    this.elementA = null;
    this.elementB = null;
    this.elementC = null;
  }

  public SpdzSInt getA() {
    return elementA;
  }

  public SpdzSInt getB() {
    return elementB;
  }

  public SpdzSInt getC() {
    return elementC;
  }

  @Override
  public String toString() {
    return "SpdzTriple [elementA=" + elementA + ", elementB=" + elementB + ", elementC=" + elementC + "]";
  }
}
