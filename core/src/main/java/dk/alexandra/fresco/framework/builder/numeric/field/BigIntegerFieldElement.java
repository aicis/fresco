package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.MathUtils;
import java.math.BigInteger;

/**
 * An element in a field defined by a {@link BigIntegerModulus}.
 */
final class BigIntegerFieldElement implements FieldElement {

  private static final long serialVersionUID = -6786266947587799652L;

  private final BigInteger value;
  private final BigIntegerModulus modulus;

  private BigIntegerFieldElement(BigInteger value, BigIntegerModulus modulus) {
    this.value = modulus.reduceModThis(value);
    this.modulus = modulus;
  }

  private FieldElement create(BigInteger value) {
    return create(value, this.modulus);
  }

  static FieldElement create(BigInteger value, BigIntegerModulus modulus) {
    return new BigIntegerFieldElement(value, modulus);
  }

  static FieldElement create(long value, BigIntegerModulus modulus) {
    return create(BigInteger.valueOf(value), modulus);
  }

  static FieldElement create(String string, BigIntegerModulus modulus) {
    return create(new BigInteger(string), modulus);
  }

  @Override
  public FieldElement add(FieldElement operand) {
    return create(value.add(operand.toBigInteger()));
  }

  @Override
  public FieldElement subtract(FieldElement operand) {
    return create(value.subtract(operand.toBigInteger()));
  }

  @Override
  public FieldElement negate() {
    return create(getModulus().subtract(value));
  }

  @Override
  public FieldElement multiply(FieldElement operand) {
    return create(value.multiply(operand.toBigInteger()));
  }

  @Override
  public FieldElement sqrt() {
    return create(MathUtils.modularSqrt(value, getModulus()));
  }

  @Override
  public FieldElement modInverse() {
    return create(value.modInverse(getModulus()));
  }

  @Override
  public boolean isZero() {
    return BigInteger.ZERO.equals(value);
  }

  @Override
  public BigInteger toBigInteger() {
    return value;
  }

  private BigInteger getModulus() {
    return modulus.getBigInteger();
  }

  @Override
  public String toString() {
    return "BigIntegerFieldElement{"
        + "value=" + value
        + ", modulus=" + modulus
        + '}';
  }
}
