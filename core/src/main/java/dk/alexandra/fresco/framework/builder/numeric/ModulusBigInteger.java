package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public final class ModulusBigInteger implements Serializable {

  private final BigInteger value;
  private final BigInteger halved;

  public ModulusBigInteger(BigInteger value) {
    Objects.requireNonNull(value);
    this.value = value;
    this.halved = value.divide(BigInteger.valueOf(2));
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

  public BigInteger getBigIntegerHalved() {
    return halved;
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
