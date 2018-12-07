package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Modulus implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(Modulus.class);

  private BigInteger bigInteger;
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
    return Objects.equals(getBigInteger(), modulus.bigInteger)
        || Objects.equals(getBigIntMutable(), modulus.bigIntMutable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getBigInteger(), getBigIntMutable());
  }

  @Override
  public String toString() {
    if (bigInteger != null) {
      return bigInteger.toString();
    }
    return bigIntMutable.toString();
  }
}
