package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class ModulusBigInteger implements Serializable {

  private final BigInteger value;

  public ModulusBigInteger(BigInteger value) {
    this.value = Objects.requireNonNull(value);
  }

  public ModulusBigInteger(int value) {
    this(BigInteger.valueOf(value));
  }

  public ModulusBigInteger(String value) {
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
    ModulusBigInteger that = (ModulusBigInteger) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "ModulusBigInteger{" +
        "value=" + value +
        '}';
  }
}
