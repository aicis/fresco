package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.function.BiConsumer;

public class BigInt implements BigIntegerI {

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

  public static BigIntegerI fromBytes(byte[] bytes, BigInteger modulus) {
    return new BigInt(BigIntMutable.fromBytes(bytes), modulus);
  }

  @Override
  public BigInt modInverse(BigInteger mod) {
    return fromConstant(asBigInteger().modInverse(mod), "modInverse",
        this.modulus);
  }

  @Override
  public BigIntegerI divide(BigIntegerI valueOf) {
    return fromConstant(asBigInteger().divide(valueOf.asBigInteger()),
        "divide(BigIntegerI)",
        this.modulus
    );
  }

  @Override
  public BigIntegerI divide(int i) {
    return fromConstant(asBigInteger().divide(new BigInt(i, modulus).asBigInteger()),
        "divide(int)",
        this.modulus);
  }

  @Override
  public BigIntegerI subtract(BigIntegerI operand) {
    return safe(this::sub, operand);
  }

  private void sub(BigIntMutable left, BigIntMutable right) {
    left.sub(right);
  }

  @Override
  public BigIntegerI multiply(BigIntegerI operand) {
    return safe(this::mul, operand);
  }

  private void mul(BigIntMutable left, BigIntMutable right) {
    left.mul(right);
  }

  @Override
  public BigIntegerI add(BigIntegerI operand) {
    return safe(this::add, operand);
  }

  private void add(BigIntMutable left, BigIntMutable right) {
    left.add(right);
  }

  private BigInt safe(BiConsumer<BigIntMutable, BigIntMutable> operation, BigIntegerI operand) {
    BigIntMutable copy = value.copy();
    BigIntMutable convertedOperand = toBigInt(operand);
    operation.accept(copy, convertedOperand);
    return new BigInt(copy, modulus);
  }

  private BigIntMutable toBigInt(BigIntegerI operand) {
    if (operand instanceof BigInt) {
      return ((BigInt) operand).value;
    } else {
      return mutableFromConstant(operand.asBigInteger(), "toBigInt");
    }
  }

  @Override
  public BigInteger asBigInteger() {
    return new BigInteger(toString());
  }

  @Override
  public byte[] toByteArray() {
    return value.toByteArray();
  }

  @Override
  public int compareTo(BigIntegerI o) {
    printSlow("compareTo");
    return asBigInteger().compareTo(o.asBigInteger());
  }

  public static BigInt fromConstant(BigInteger bigInteger, BigInteger modulus) {
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
    return new BigIntMutable(bigInteger.toString());
  }

  // todo avoid converting to BigInteger
  private static void printSlow(String methodName) {
    System.out.println("Slow conversion to BigInteger: " + methodName);
  }

  @Override
  public BigIntegerI modPow(BigIntegerI valueOf, BigInteger modulus) {
    return fromConstant(asBigInteger().modPow(valueOf.asBigInteger(), modulus), "modPow",
        this.modulus);
  }
}
