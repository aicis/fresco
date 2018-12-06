package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;

public class BigIntegerClassic implements FieldElement {

  private final BigInteger value;
  private final BigInteger fieldModulus;

  public BigIntegerClassic(BigInteger value, BigInteger fieldModulus) {
    this.value = value.mod(fieldModulus);
    this.fieldModulus = fieldModulus;
  }

  @Override
  public FieldElement modInverse(BigInteger modulus) {
    return new BigIntegerClassic(value.modInverse(modulus), fieldModulus);
  }

  @Override
  public FieldElement modPow(FieldElement pow, BigInteger modulus) {
    return create(value.modPow(pow.asBigInteger(), fieldModulus));
  }

  @Override
  public FieldElement divide(FieldElement denominator) {
    return create(value.divide(denominator.asBigInteger()));
  }

  private BigIntegerClassic create(BigInteger divide) {
    return new BigIntegerClassic(divide, fieldModulus);
  }

  @Override
  public FieldElement divide(int denominator) {
    return create(value.divide(BigInteger.valueOf(denominator)));
  }

  @Override
  public BigIntegerClassic subtract(FieldElement operand) {
    return create(value.subtract(operand.asBigInteger()));
  }

  @Override
  public BigIntegerClassic multiply(FieldElement operand) {
    return create(value.multiply(operand.asBigInteger()));
  }

  @Override
  public BigIntegerClassic add(FieldElement operand) {
    return create(value.add(operand.asBigInteger()));
  }

  @Override
  public BigInteger asBigInteger() {
    return value;
  }

  @Override
  public void toByteArray(byte[] bytes, int offset, int byteLength) {
    byte[] byteArray = value.toByteArray();
    System.arraycopy(byteArray, 0, bytes, byteLength - byteArray.length + offset, byteArray.length);
  }

  @Override
  public int compareTo(FieldElement o) {
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
