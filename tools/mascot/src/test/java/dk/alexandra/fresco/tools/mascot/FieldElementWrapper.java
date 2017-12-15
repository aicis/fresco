package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.Objects;

public class FieldElementWrapper {

  private FieldElement element;

  public FieldElementWrapper(FieldElement element) {
    Objects.requireNonNull(element);
    this.element = element;
  }

  BigInteger getValue() {
    return element.toBigInteger();
  }

  BigInteger getModulus() {
    return element.getModulus();
  }

  int getBitLength() {
    return element.getBitLength();
  }
  
  @Override
  public String toString() {
    return "FieldElementWrapper [element=" + element + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((element == null) ? 0 : element.hashCode());
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
    FieldElementWrapper other = (FieldElementWrapper) obj;
    if (getBitLength() != other.getBitLength()) {
      return false;
    }
    if (!getModulus().equals(other.getModulus())) {
      return false;
    }
    if (!getValue().equals(other.getValue())) {
      return false;
    }
    return true;
  }

}
