package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BiConsumer;

public class FieldInteger implements FieldElement {

  private BigIntMutable value;
  private Modulus modulus;

  public FieldInteger(BigIntMutable value, Modulus modulus) {
    this.value = value;
    this.modulus = modulus;
  }

  public FieldInteger(int i, Modulus modulus) {
    this(new BigIntMutable(i), modulus);
  }

  public FieldInteger(String toString, Modulus modulus) {
    this(new BigIntMutable(toString), modulus);
  }

  public static FieldElement fromBytes(byte[] bytes, Modulus modulus) {
    BigIntMutable bigIntMutable = BigIntMutable.fromBytes(bytes);
    bigIntMutable.mod(modulus.getBigIntMutable());
    return new FieldInteger(bigIntMutable, modulus);
  }

  @Override
  public FieldElement divide(int i) {
    return divide(new FieldInteger(i, modulus));
  }

  @Override
  public FieldElement divide(FieldElement operand) {
    return safe(this::div, operand);
  }

  private void div(BigIntMutable left, BigIntMutable right) {
    left.div(right);
  }

  @Override
  public FieldElement subtract(FieldElement operand) {
    return safe(this::sub, operand);
  }

  private void sub(BigIntMutable left, BigIntMutable right) {
    left.sub(right);
  }

  @Override
  public FieldElement multiply(FieldElement operand) {
    return safe(this::mul, operand);
  }

  private void mul(BigIntMutable left, BigIntMutable right) {
    left.mul(right);
  }

  @Override
  public FieldElement add(FieldElement operand) {
    return safe(this::add, operand);
  }

  private void add(BigIntMutable left, BigIntMutable right) {
    left.add(right);
  }

  private FieldInteger safe(BiConsumer<BigIntMutable, BigIntMutable> operation, FieldElement operand) {
    BigIntMutable copy = value.copy();
    BigIntMutable convertedOperand = toBigIntMutable(operand);
    operation.accept(copy, convertedOperand);
    copy.mod(modulus.getBigIntMutable());
    return new FieldInteger(copy, modulus);
  }

  private BigIntMutable toBigIntMutable(FieldElement operand) {
    if (operand instanceof FieldInteger) {
      return ((FieldInteger) operand).value;
    } else {
      return new BigIntMutable(operand.asBigInteger().toString());
    }
  }

  @Override
  public BigInteger asBigInteger() {
    return new BigInteger(value.toString());
  }

  public static FieldInteger fromBigInteger(BigInteger bigInteger, Modulus modulus) {
    if (bigInteger == null) {
      return null;
    }
    return new FieldInteger(bigInteger.toString(), modulus);
  }

  @Override
  public void toByteArray(byte[] bytes, int offset, int byteLength) {
    value.toByteArray(bytes, offset, byteLength);
  }

  @Override
  public int compareTo(FieldElement o) {
    //todo avoid conversion
    return asBigInteger().compareTo(o.asBigInteger());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FieldInteger fieldInteger = (FieldInteger) o;
    return Objects.equals(modulus, fieldInteger.modulus) &&
        Objects.equals(value, fieldInteger.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modulus, value);
  }

  @Override
  public String toString() {
    return "FieldInteger{" +
        "value=" + value +
        ", modulus =" + modulus +
        '}';
  }
}
