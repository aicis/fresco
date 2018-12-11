package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;

public final class ModulusMersennePrime implements Modulus {

  private final BigInteger bigInteger;
  private final MersennePrimeInteger value;

  private ModulusMersennePrime(MersennePrimeInteger value) {
    if (value == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.value = value;
    this.bigInteger = new BigInteger(value.toString());
  }

  MersennePrimeInteger getMersennePrimeInteger() {
    return value;
  }

  public BigInteger getBigInteger() {
    return bigInteger;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModulusMersennePrime that = (ModulusMersennePrime) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "ModulusMersennePrime{" +
        "value=" + value +
        '}';
  }
}
