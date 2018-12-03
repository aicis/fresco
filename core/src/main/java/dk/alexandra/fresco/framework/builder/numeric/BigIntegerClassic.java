package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;

public class BigIntegerClassic implements BigIntegerI {

  private BigInteger value;

  public BigIntegerClassic(BigInteger value) {
    this.value = value;
  }

  @Override

  public void mod(BigInteger modulus) {
    value = value.mod(modulus);
  }

  @Override
  public BigIntegerI modInverse(BigInteger modulus) {
    return new BigIntegerClassic(value.modInverse(modulus));
  }

  @Override
  public BigIntegerI modPow(BigIntegerI pow, BigInteger modulus) {
    return new BigIntegerClassic(value.modPow(pow.asBigInteger(), modulus));
  }

  @Override
  public BigIntegerI copy() {
    return new BigIntegerClassic(value);
  }

  @Override
  public BigIntegerI divide(BigIntegerI denominator) {
    return new BigIntegerClassic(value.divide(denominator.asBigInteger()));
  }

  @Override
  public BigIntegerI divide(int denominator) {
    return new BigIntegerClassic(value.divide(BigInteger.valueOf(denominator)));
  }

  @Override
  public void subtract(BigIntegerI operand) {
    value = value.subtract(operand.asBigInteger());
  }

  @Override
  public void multiply(BigIntegerI operand) {
    value = value.multiply(operand.asBigInteger());
  }

  @Override
  public void add(BigIntegerI operand) {
    value = value.add(operand.asBigInteger());
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
}
