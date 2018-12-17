package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.MathUtils;
import java.math.BigInteger;

final class BigIntegerFieldElement implements FieldElement {

  private final BigInteger value;
  private final BigIntegerModulus modulus;

  private BigIntegerFieldElement(BigInteger value, BigIntegerModulus modulus) {
    this.value = value.mod(modulus.getBigInteger());
    this.modulus = modulus;
  }

  private FieldElement create(BigInteger value) {
    return new BigIntegerFieldElement(value, modulus);
  }

  static FieldElement create(BigInteger value, BigIntegerModulus modulus) {
    return new BigIntegerFieldElement(value, modulus);
  }

  static FieldElement create(int value, BigIntegerModulus modulus) {
    return create(BigInteger.valueOf(value), modulus);
  }

  static FieldElement create(String string, BigIntegerModulus modulus) {
    return create(new BigInteger(string), modulus);
  }

  @Override
  public FieldElement add(FieldElement operand) {
    return create(value.add(extractValue(operand)));
  }

  @Override
  public FieldElement subtract(FieldElement operand) {
    return create(value.subtract(extractValue(operand)));
  }

  @Override
  public FieldElement negate() {
    return create(getModulus().subtract(value));
  }

  @Override
  public FieldElement multiply(FieldElement operand) {
    return create(value.multiply(extractValue(operand)));
  }

  @Override
  public FieldElement sqrt() {
    return create(MathUtils.modularSqrt(value, getModulus()));
  }

  @Override
  public FieldElement modInverse() {
    return create(value.modInverse(getModulus()));
  }

  static BigInteger extractValue(FieldElement element) {
    return ((BigIntegerFieldElement) element).value;
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
