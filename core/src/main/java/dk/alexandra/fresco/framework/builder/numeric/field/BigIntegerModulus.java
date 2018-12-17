package dk.alexandra.fresco.framework.builder.numeric.field;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

final class BigIntegerModulus implements Serializable {

  private final BigInteger value;

  BigIntegerModulus(BigInteger value) {
    this.value = Objects.requireNonNull(value);
  }

  BigInteger getBigInteger() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BigIntegerModulus that = (BigIntegerModulus) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "BigIntegerModulus{"
        + "value=" + value
        + '}';
  }
}
