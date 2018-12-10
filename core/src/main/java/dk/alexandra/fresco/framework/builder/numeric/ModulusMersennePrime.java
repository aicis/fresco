package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModulusMersennePrime implements Modulus<MersennePrimeInteger> {

  private static final Logger logger = LoggerFactory.getLogger(ModulusMersennePrime.class);

  private BigInteger bigInteger;
  private final MersennePrimeInteger value;

  private ModulusMersennePrime(MersennePrimeInteger value) {
    if (value == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.value = value;
  }

  public ModulusMersennePrime(int value) {
    this(new MersennePrimeInteger(value));
  }

  public ModulusMersennePrime(String value) {
    this(new MersennePrimeInteger(value));
  }

  @Override
  public Modulus<MersennePrimeInteger> half() {
    MersennePrimeInteger copy = value.copy();
    copy.div(new MersennePrimeInteger(2));
    return new ModulusMersennePrime(copy);
  }

  @Override
  public MersennePrimeInteger get() {
    return value;
  }

  public BigInteger getBigInteger() {
    if (bigInteger == null) {
      logger.debug("Converting MersennePrimeInteger to BigInteger");
      bigInteger = new BigInteger(value.toString());
    }
    return bigInteger;
  }

  @Override
  public int bytesLength() {
    // todo EOA implement
    return 0;
  }

  @Override
  public int bitLength() {
    // todo EOA implement
    return 0;
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
