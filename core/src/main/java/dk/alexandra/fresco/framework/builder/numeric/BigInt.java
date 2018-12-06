package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BiConsumer;

public class BigInt implements FieldElement {

  private BigInteger modulus;
  private BigIntMutable value;

  public BigInt(BigIntMutable value, BigInteger modulus) {
    this.value = value;
    this.modulus = modulus;
  }

  public BigInt(int i, BigInteger modulus) {
    this(new BigIntMutable(i), modulus);
  }

  public BigInt(String toString, BigInteger modulus) {
    this(new BigIntMutable(toString), modulus);
  }

  public static FieldElement fromBytes(byte[] bytes, BigInteger modulus) {
    BigIntMutable bigIntMutable = BigIntMutable.fromBytes(bytes);
    bigIntMutable.mod(mutableFromConstant(modulus, "fromBytes"));
    return new BigInt(bigIntMutable, modulus);
  }

  @Override
  public BigInt modInverse(BigInteger operand) {
    return fromConstant(asBigInteger().modInverse(operand), "modInverse",
        this.modulus);
  }

  @Override
  public FieldElement divide(FieldElement valueOf) {
    return fromConstant(asBigInteger().divide(valueOf.asBigInteger()),
        "divide(FieldElement)",
        this.modulus
    );
  }

  @Override
  public FieldElement divide(int i) {
    return fromConstant(asBigInteger().divide(new BigInt(i, modulus).asBigInteger()),
        "divide(int)",
        this.modulus);
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
    copy.mod(mutableFromConstant(modulus, "EOA"));
    return new BigInt(copy, modulus);
  }

  private BigIntMutable toBigInt(FieldElement operand) {
    if (operand instanceof BigInt) {
      return ((BigInt) operand).value;
    } else {
      return mutableFromConstant(operand.asBigInteger(), "toBigInt");
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
    printSlow("compareTo");
    return asBigInteger().compareTo(o.asBigInteger());
  }

  public static BigInt fromConstant(BigInteger bigInteger, BigInteger modulus) {
    if (bigInteger == null) {
      return null;
    }
    return new BigInt(mutableFromConstant(bigInteger, "N/A"), modulus);
  }

  // todo avoid converting to BigInteger
  private static BigInt fromConstant(BigInteger bigInteger,
      String methodName, BigInteger modulus) {
    return new BigInt(mutableFromConstant(bigInteger, methodName), modulus);
  }

  // todo avoid converting to BigInteger
  private static BigIntMutable mutableFromConstant(BigInteger bigInteger,
      String methodName) {
    printSlow(methodName);
    if (bigInteger == null) {
      return null;
    }
    return new BigIntMutable(bigInteger.toString());
  }

  // todo avoid converting to BigInteger
  private static void printSlow(String methodName) {
//    System.out.println("Slow conversion to BigInteger: " + methodName);
  }

  @Override
  public FieldElement modPow(FieldElement valueOf, BigInteger modulus) {
    return fromConstant(asBigInteger().modPow(valueOf.asBigInteger(), modulus), "modPow",
        this.modulus);
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
