package dk.alexandra.fresco.suite.spdz.datatypes;

import java.io.Serializable;

public class SpdzTriple implements Serializable {

  private static final long serialVersionUID = -4394957717364446774L;

  private final SpdzElement elementA;
  private final SpdzElement elementB;
  private final SpdzElement elementC;

  /**
   * Construct a new SpdzTriple. The triple contains 3 values A, B and C
   * such that A * B = C. 
   * 
   * @param elementA A
   * @param elementB B
   * @param elementC C
   */
  public SpdzTriple(SpdzElement elementA, SpdzElement elementB, SpdzElement elementC) {
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

  public SpdzElement getA() {
    return elementA;
  }

  public SpdzElement getB() {
    return elementB;
  }

  public SpdzElement getC() {
    return elementC;
  }

  @Override
  public String toString() {
    return "SpdzTriple [elementA=" + elementA + ", elementB=" + elementB + ", elementC=" + elementC + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((elementA == null) ? 0 : elementA.hashCode());
    result = prime * result + ((elementB == null) ? 0 : elementB.hashCode());
    result = prime * result + ((elementC == null) ? 0 : elementC.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SpdzTriple other = (SpdzTriple) obj;
    if (elementA == null) {
      if (other.elementA != null) {
        return false;
      }
    } else if (!elementA.equals(other.elementA)) {
      return false;
    }
    if (elementB == null) {
      if (other.elementB != null) {
        return false;
      }
    } else if (!elementB.equals(other.elementB)) {
      return false;
    }
    if (elementC == null) {
      if (other.elementC != null) {
        return false;
      }
    } else if (!elementC.equals(other.elementC)) {
      return false;
    }
    return true;
  }


}
