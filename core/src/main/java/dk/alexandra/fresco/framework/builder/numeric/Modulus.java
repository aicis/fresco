package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Modulus implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(Modulus.class);

  private BigInteger bigInteger;
  private final BigIntMutable bigIntMutable;

  public Modulus(BigInteger modulus) {
    if (modulus == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.bigInteger = modulus;
    logger.debug("Converting BigInteger to BigIntMutable");
    bigIntMutable = new BigIntMutable(bigInteger.toString());
    ensureModulus();
  }

  public Modulus(BigIntMutable modulus) {
    if (modulus == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.bigIntMutable = modulus;
  }

  public Modulus(int modulus) {
    this(new BigIntMutable(modulus));
  }

  public Modulus(String modulus) {
    this(new BigIntMutable(modulus));
  }

  private void ensureModulus() {
    if (bigInteger == null && bigIntMutable == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
  }

  public BigIntMutable getBigIntMutable() {
    return bigIntMutable;
  }

  public BigInteger getBigInteger() {
    if (bigInteger == null) {
      logger.debug("Converting BigIntMutable to BigInteger");
      bigInteger = new BigInteger(bigIntMutable.toString());
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
    return Objects.equals(bigIntMutable, modulus.bigIntMutable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bigIntMutable);
  }

  @Override
  public String toString() {
    if (bigInteger != null) {
      return bigInteger.toString();
    }
    return bigIntMutable.toString();
  }
}
