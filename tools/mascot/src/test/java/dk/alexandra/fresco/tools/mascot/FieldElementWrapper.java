package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.Is;
import org.junit.Assert;

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

  public static void assertEquals(List<FieldElement> expected, List<FieldElement> actual) {
    Assert.assertThat(expected, IsCollectionWithSize.hasSize(actual.size()));
    for (int i = 0; i < expected.size(); i++) {
      assertEqualsMessaged(" - error at index: " + i, expected.get(i), actual.get(i));
    }
  }

  public static void assertEquals(FieldElement expected, FieldElement actual) {
    assertEqualsMessaged("", expected, actual);
  }

  public static void assertEqualsMessaged(String message, FieldElement expected,
      FieldElement actual) {
    Assert.assertThat("Bit length mismatch" + message + " in " + actual,
        expected.getBitLength(), Is.is(actual.getBitLength()));
    Assert.assertThat("Modulus mismatch" + message + " in " + actual,
        expected.getModulus(), Is.is(actual.getModulus()));
    Assert.assertThat("Value mismatch" + message + " in " + actual,
        expected.getValue(), Is.is(actual.getValue()));
  }
}
