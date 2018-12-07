package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Modulus implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(Modulus.class);

  private final BigInteger bigInteger;
  private BigIntMutable bigIntMutable;

  public Modulus(BigInteger modulus) {
    if (modulus == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.bigInteger = modulus;
  }

  public Modulus(BigIntMutable modulus) {
    if (modulus == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.bigIntMutable = modulus;
    this.bigInteger = new BigInteger(modulus.toString());
  }

  public Modulus(int modulus) {
    this(BigInteger.valueOf(modulus));
  }

  public Modulus(String modulus) {
    this(new BigInteger(modulus));
  }

  public BigIntMutable getBigIntMutable() {
    if (bigIntMutable == null) {
      logger.debug("Converting BigInteger to BigIntMutable");
      bigIntMutable = new BigIntMutable(bigInteger.toString());
    }
    return bigIntMutable;
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
    Modulus modulus = (Modulus) o;
    return Objects.equals(bigInteger, modulus.bigInteger);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bigInteger);
  }

  @Override
  public String toString() {
    return bigInteger.toString();
  }
}
