package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;

public final class ModulusMersennePrime implements Modulus {

  private final MersennePrimeInteger mersenne;
  private final BigInteger value;
  private BigInteger halved;

  private ModulusMersennePrime(MersennePrimeInteger mersenne) {
    if (mersenne == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.mersenne = mersenne;
    this.value = new BigInteger(mersenne.toString());
  }

  MersennePrimeInteger getMersennePrimeInteger() {
    return mersenne;
  }

  @Override
  public BigInteger getBigInteger() {
    return value;
  }

  @Override
  public BigInteger getBigIntegerHalved() {
    if (halved == null) {
      halved = value.divide(BigInteger.valueOf(2));
    }
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
