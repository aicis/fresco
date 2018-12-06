package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BiConsumer;

public class BigInt implements FieldElement {

  private BigIntMutable modulus;
  private BigIntMutable value;

  public BigInt(BigIntMutable value, BigIntMutable modulus) {
    this.value = value;
    this.modulus = modulus;
  }

  public BigInt(int i, BigIntMutable modulus) {
    this(new BigIntMutable(i), modulus);
  }

  public BigInt(String toString, BigIntMutable modulus) {
    this(new BigIntMutable(toString), modulus);
  }

  public static FieldElement fromBytes(byte[] bytes, BigInteger modulus) {
    return fromBytes(bytes, new BigIntMutable(modulus));
  }

  public static FieldElement fromBytes(byte[] bytes, BigIntMutable modulus) {
    BigIntMutable bigIntMutable = BigIntMutable.fromBytes(bytes);
    bigIntMutable.mod(modulus);
    return new BigInt(bigIntMutable, modulus);
  }

  @Override
  public FieldElement divide(int i) {
    return divide(new BigInt(i, modulus));
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

  private BigInt safe(BiConsumer<BigIntMutable, BigIntMutable> operation, FieldElement operand) {
    BigIntMutable copy = value.copy();
    BigIntMutable convertedOperand = toBigInt(operand);
    operation.accept(copy, convertedOperand);
    copy.mod(modulus);
    return new BigInt(copy, modulus);
  }

  private BigIntMutable toBigInt(FieldElement operand) {
    if (operand instanceof BigInt) {
      return ((BigInt) operand).value;
    } else {
      return mutableFromConstant(operand.asBigInteger());
    }
  }

  @Override
  public BigInteger asBigInteger() {
    return new BigInteger(value.toString());
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

  public static BigInt fromConstant(BigInteger bigInteger, BigInteger modulus) {
    return fromConstant(bigInteger, new BigIntMutable(modulus));
  }

  public static BigInt fromConstant(BigInteger bigInteger, BigIntMutable modulus) {
    if (bigInteger == null) {
      return null;
    }
    return new BigInt(mutableFromConstant(bigInteger), modulus);
  }

  // todo avoid converting to BigInteger
  private static BigIntMutable mutableFromConstant(BigInteger bigInteger) {
    if (bigInteger == null) {
      return null;
    }
    return new BigIntMutable(bigInteger.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BigInt bigInt = (BigInt) o;
    return Objects.equals(modulus, bigInt.modulus) &&
        Objects.equals(value, bigInt.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modulus, value);
  }

  @Override
  public String toString() {
    return "BigInt{" +
        "value=" + value +
        ", modulus =" + modulus +
        '}';
  }
}
