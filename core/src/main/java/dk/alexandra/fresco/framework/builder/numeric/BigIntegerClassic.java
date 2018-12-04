package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;

public class BigIntegerClassic implements BigIntegerI {

  private BigInteger value;
  private BigInteger fieldModulus;

  public BigIntegerClassic(BigInteger value, BigInteger fieldModulus) {
    this.value = value;
    this.fieldModulus = fieldModulus;
  }

  @Override
  public BigIntegerI modInverse(BigInteger modulus) {
    return new BigIntegerClassic(value.modInverse(modulus), fieldModulus);
  }

  @Override
  public BigIntegerI modPow(BigIntegerI pow, BigInteger modulus) {
    return create(value.modPow(pow.asBigInteger(), fieldModulus));
  }

  @Override
  public BigIntegerI divide(BigIntegerI denominator) {
    return create(value.divide(denominator.asBigInteger()));
  }

  private BigIntegerClassic create(BigInteger divide) {
    return new BigIntegerClassic(divide, fieldModulus);
  }

  @Override
  public BigIntegerI divide(int denominator) {
    return create(value.divide(BigInteger.valueOf(denominator)));
  }

  @Override
  public BigIntegerClassic subtract(BigIntegerI operand) {
    return create(value.subtract(operand.asBigInteger()));
  }

  @Override
  public BigIntegerClassic multiply(BigIntegerI operand) {
    return create(value.multiply(operand.asBigInteger()));
  }

  @Override
  public BigIntegerClassic add(BigIntegerI operand) {
    return create(value.add(operand.asBigInteger()));
  }

  @Override
  public BigInteger asBigInteger() {
    return value;
  }

  @Override
  public byte[] toByteArray() {
    return value.toByteArray();
  }

  @Override
  public int compareTo(BigIntegerI o) {
    return value.compareTo(o.asBigInteger());
  }

  @Override
  public String toString() {
    return "BigIntegerClassic{" +
        "value=" + value +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BigIntegerClassic that = (BigIntegerClassic) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
