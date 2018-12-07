package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Modulus implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(Modulus.class);

  private BigInteger bigInteger;
  private BigIntMutable bigIntImmutable;

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
    this.bigIntImmutable = modulus;
  }

  public Modulus(int modulus) {
    this(BigInteger.valueOf(modulus));
  }

  public Modulus(String modulus) {
    this(new BigInteger(modulus));
  }

  public BigIntMutable getBigIntMutable() {
    if (bigIntImmutable == null) {
      logger.debug("Converting BigInteger to BigIntMutable");
      bigIntImmutable = new BigIntMutable(bigInteger.toString());
    }
    return bigIntImmutable;
  }

  public BigInteger getBigInteger() {
    if (bigInteger == null) {
      logger.debug("Converting BigIntMutable to BigInteger");
      bigInteger = new BigInteger(bigIntImmutable.toString());
    }
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
    return Objects.equals(bigIntImmutable, modulus.bigIntImmutable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bigIntImmutable);
  }

  @Override
  public String toString() {
    if (bigInteger != null) {
      return bigInteger.toString();
    }
    return bigIntImmutable.toString();
  }
}
