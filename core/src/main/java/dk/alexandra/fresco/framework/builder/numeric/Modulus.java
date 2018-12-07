package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Modulus implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(Modulus.class);

  enum Based {
    BigInteger, BigIntMutable
  }

  private BigInteger bigInteger;
  private BigIntMutable bigIntMutable;
  private final Based based;

  public Modulus(BigInteger modulus) {
    if (modulus == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.bigInteger = modulus;
    this.based = Based.BigInteger;
  }

  public Modulus(BigIntMutable modulus) {
    if (modulus == null) {
      throw new IllegalArgumentException("modulus cannot be null");
    }
    this.bigIntMutable = modulus;
    this.based = Based.BigIntMutable;
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
      logger.debug("Converting BigInteger to BigIntMutable");
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
    if (based == Based.BigIntMutable && modulus.based == Based.BigIntMutable) {
      return Objects.equals(bigIntMutable, modulus.bigIntMutable);
    } else {
      return Objects.equals(getBigInteger(), modulus.getBigInteger());
    }
  }

  @Override
  public int hashCode() {
    if (based == Based.BigInteger) {
      return Objects.hash(bigInteger);
    }
    return Objects.hash(bigIntMutable);
  }

  @Override
  public String toString() {
    if (based == Based.BigInteger) {
      return bigInteger.toString();
    }
    return bigIntMutable.toString();
  }

  public int bitLength() {
    return getBigInteger().bitLength();
  }
}
