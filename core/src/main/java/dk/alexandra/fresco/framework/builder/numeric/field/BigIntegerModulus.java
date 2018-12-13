package dk.alexandra.fresco.framework.builder.numeric.field;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class BigIntegerModulus implements Serializable {

  private final BigInteger value;

  public BigIntegerModulus(BigInteger value) {
    this.value = Objects.requireNonNull(value);
    FieldUtils.ensureDivisible(value.bitLength());
  }

  public BigIntegerModulus(int value) {
    this(BigInteger.valueOf(value));
  }

  public BigIntegerModulus(String value) {
    this(new BigInteger(value));
  }

  public BigInteger getBigInteger() {
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
    return "BigIntegerModulus{" +
        "value=" + value +
        '}';
  }
}
