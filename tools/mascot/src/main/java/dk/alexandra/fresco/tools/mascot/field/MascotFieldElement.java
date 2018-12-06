package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BinaryOperator;

public final class MascotFieldElement implements Addable<MascotFieldElement> {

  private final BigInteger value;
  private final Modulus modulus;
  private final int bitLength;

  /**
   * Creates new field element.
   *
   * @param value value of element
   * @param modulus modulus defining field
   */
  public MascotFieldElement(BigInteger value, Modulus modulus) {
    this.value = Objects.requireNonNull(value);
    this.modulus = Objects.requireNonNull(modulus);
    this.bitLength = modulus.getBigInteger().bitLength();
    sanityCheck(value, modulus, bitLength);
  }

  public MascotFieldElement(MascotFieldElement other) {
    this(other.value, other.modulus);
  }

  public MascotFieldElement(String value, String modulus) {
    this(new BigInteger(value), new Modulus(modulus));
  }

  public MascotFieldElement(long value, Modulus modulus) {
    this(BigInteger.valueOf(value), modulus);
  }

  public MascotFieldElement(byte[] value, Modulus modulus) {
    this(new BigInteger(1, value), modulus);
  }

  private MascotFieldElement binaryOp(BinaryOperator<BigInteger> op, MascotFieldElement left,
      MascotFieldElement right) {
    return new MascotFieldElement(op.apply(left.toBigInteger(), right.toBigInteger()).mod(modulus.getBigInteger()),
        this.modulus);
  }

  public MascotFieldElement pow(int exponent) {
    return new MascotFieldElement(this.value.pow(exponent).mod(modulus.getBigInteger()), modulus);
  }

  @Override
  public MascotFieldElement add(MascotFieldElement other) {
    return binaryOp(BigInteger::add, this, other);
  }

  public MascotFieldElement subtract(MascotFieldElement other) {
    return binaryOp(BigInteger::subtract, this, other);
  }

  public MascotFieldElement multiply(MascotFieldElement other) {
    return binaryOp(BigInteger::multiply, this, other);
  }

  public MascotFieldElement negate() {
    return new MascotFieldElement(value.multiply(BigInteger.valueOf(-1)).mod(modulus.getBigInteger()), modulus);
  }

  public MascotFieldElement modInverse() {
    return new MascotFieldElement(value.modInverse(modulus.getBigInteger()), modulus);
  }

  public MascotFieldElement sqrt() {
    BigInteger rawSqrt = MathUtils.modularSqrt(value, modulus);
    return new MascotFieldElement(rawSqrt, modulus);
  }

  public BigInteger toBigInteger() {
    return this.value;
  }

  public boolean getBit(int bitIndex) {
    return value.testBit(bitIndex);
  }

  public MascotFieldElement select(boolean bit) {
    return bit ? this : new MascotFieldElement(BigInteger.ZERO, modulus);
  }

  public boolean isZero() {
    return value.equals(BigInteger.ZERO);
  }

  /**
   * Converts value into byte array. <p>Result is guaranteed to be exactly bitLength / 8 long
   * (truncates if underlying BigInteger value "overflows"). Result is in big-endian order. </p>
   *
   * @return byte representation of value
   */
  public byte[] toByteArray() {
    int byteLength = bitLength / 8;
    byte[] res = new byte[byteLength];
    byte[] array = value.toByteArray();
    int arrayStart = array.length > byteLength ? array.length - byteLength : 0;
    int resStart = array.length > byteLength ? 0 : byteLength - array.length;
    int len = Math.min(byteLength, array.length);
    System.arraycopy(array, arrayStart, res, resStart, len);
    return res;
  }

  public StrictBitVector toBitVector() {
    return new StrictBitVector(toByteArray());
  }

  public Modulus getModulus() {
    return this.modulus;
  }

  public int getBitLength() {
    return bitLength;
  }

  public BigInteger getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "MascotFieldElement [value=" + value + ", modulus=" + modulus + ", bitLength=" + bitLength
        + "]";
  }

  private void sanityCheck(BigInteger value, Modulus modulus, int bitLength) {
    if (bitLength % 8 != 0) {
      throw new IllegalArgumentException("Bit length must be multiple of 8");
    } else if (value.signum() == -1) {
      throw new IllegalArgumentException("Cannot have negative value");
    } else if (modulus.getBigInteger().signum() == -1) {
      throw new IllegalArgumentException("Cannot have negative modulus");
    } else if (value.compareTo(modulus.getBigInteger()) >= 0) {
      throw new IllegalArgumentException("Value must be smaller than modulus");
    }
  }

}
