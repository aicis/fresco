package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.value.SInt;
import java.io.Serializable;

public class SpdzSInt implements SInt, Serializable {

  private static final long serialVersionUID = -9048612329603301195L;
  public SpdzElement value;

  public SpdzSInt() {
    this.value = null;
  }

  public SpdzSInt(SpdzElement e) {
    this.value = e;
  }

  @Override
  public String toString() {
    return "SpdzSInt(" + this.value + ")";
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
    SpdzSInt other = (SpdzSInt) obj;
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  @Override
  public SInt out() {
    return this;
  }
}
