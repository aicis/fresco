package dk.alexandra.fresco.suite.spdz.datatypes;

import java.io.Serializable;

public class SpdzTriple implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -4394957717364446774L;

  private final SpdzElement a;
  private final SpdzElement b;
  private final SpdzElement c;

  public SpdzTriple(SpdzElement a, SpdzElement b, SpdzElement c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public SpdzTriple() {
    this.a = null;
    this.b = null;
    this.c = null;
  }

  public SpdzElement getA() {
    return a;
  }

  public SpdzElement getB() {
    return b;
  }

  public SpdzElement getC() {
    return c;
  }

  @Override
  public String toString() {
    return "SpdzTriple [a=" + a + ", b=" + b + ", c=" + c + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((a == null) ? 0 : a.hashCode());
    result = prime * result + ((b == null) ? 0 : b.hashCode());
    result = prime * result + ((c == null) ? 0 : c.hashCode());
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
    if (a == null) {
      if (other.a != null) {
        return false;
      }
    } else if (!a.equals(other.a)) {
      return false;
    }
    if (b == null) {
      if (other.b != null) {
        return false;
      }
    } else if (!b.equals(other.b)) {
      return false;
    }
    if (c == null) {
      if (other.c != null) {
        return false;
      }
    } else if (!c.equals(other.c)) {
      return false;
    }
    return true;
  }


}
