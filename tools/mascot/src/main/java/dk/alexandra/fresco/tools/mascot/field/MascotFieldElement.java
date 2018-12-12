package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BinaryOperator;

public final class MascotFieldElement implements FieldElement, Addable<MascotFieldElement> {

  private final BigInteger value;
  private final BigInteger modulus;
  private final int bitLength;

  /**
   * Creates new field element.
   *
   * @param value value of element
   * @param modulus modulus defining field
   */
  public MascotFieldElement(BigInteger value, BigInteger modulus) {
    this.value = Objects.requireNonNull(value);
    this.modulus = Objects.requireNonNull(modulus);
    this.bitLength = modulus.bitLength();
    sanityCheck(value, modulus, bitLength);
  }

  public MascotFieldElement(MascotFieldElement other) {
    this(other.value, other.modulus);
  }

  public MascotFieldElement(String value, String modulus) {
    this(new BigInteger(value), new BigInteger(modulus));
  }

  public MascotFieldElement(long value, BigInteger modulus) {
    this(BigInteger.valueOf(value), modulus);
  }

  public MascotFieldElement(byte[] value, BigInteger modulus) {
    this(new BigInteger(1, value), modulus);
  }

  @Override
  public MascotFieldElement create(BigInteger value) {
    return new MascotFieldElement(value.mod(modulus), modulus);
  }

  @Override
  public BigInteger getValue(FieldElement element) {
    return ((MascotFieldElement) element).value;
  }

  @Override
  public BigInteger getValue() {
    return value;
  }

  @Override
  public MascotFieldElement binaryOp(BinaryOperator<BigInteger> op, FieldElement left,
      FieldElement right) {
    return create(op.apply(getValue(left), getValue(right)));
  }

  @Override
  public MascotFieldElement pow(int exponent) {
    return create(this.value.pow(exponent));
  }

  @Override
  public MascotFieldElement add(FieldElement other) {
    return add((MascotFieldElement) other);
  }

  @Override
  public MascotFieldElement add(MascotFieldElement other) {
    return binaryOp(BigInteger::add, this, other);
  }

  @Override
  public MascotFieldElement subtract(FieldElement other) {
    return binaryOp(BigInteger::subtract, this, other);
  }

  @Override
  public MascotFieldElement multiply(FieldElement other) {
    return binaryOp(BigInteger::multiply, this, other);
  }

  @Override
  public MascotFieldElement negate() {
    return create(value.multiply(BigInteger.valueOf(-1)));
  }

  @Override
  public MascotFieldElement modInverse() {
    return create(value.modInverse(modulus));
  }

  @Override
  public MascotFieldElement sqrt() {
    return create(MathUtils.modularSqrt(value, modulus));
  }

  @Override
  public BigInteger convertToBigInteger() {
    return value;
  }

  @Override
  public boolean getBit(int bitIndex) {
    return value.testBit(bitIndex);
  }

  @Override
  public MascotFieldElement select(boolean bit) {
    return bit ? this : new MascotFieldElement(BigInteger.ZERO, modulus);
  }

  @Override
  public boolean isZero() {
    return value.equals(BigInteger.ZERO);
  }

  /**
   * Converts value into byte array. <p>Result is guaranteed to be exactly bitLength / 8 long
   * (truncates if underlying BigInteger value "overflows"). Result is in big-endian order. </p>
   *
   * @return byte representation of value
   */
  @Override
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

  @Override
  public StrictBitVector toBitVector() {
    return new StrictBitVector(toByteArray());
  }

  @Override
  public BigInteger getModulus() {
    return this.modulus;
  }

  @Override
  public int getBitLength() {
    return bitLength;
  }

  @Override
  public String toString() {
    return "MascotFieldElement [value=" + value + ", modulus=" + modulus + ", bitLength="
        + bitLength
        + "]";
  }

  private void sanityCheck(BigInteger value, BigInteger modulus, int bitLength) {
    if (bitLength % 8 != 0) {
      throw new IllegalArgumentException("Bit length must be multiple of 8");
    } else if (value.signum() == -1) {
      throw new IllegalArgumentException("Cannot have negative value");
    } else if (modulus.signum() == -1) {
      throw new IllegalArgumentException("Cannot have negative modulus");
    } else if (value.compareTo(modulus) >= 0) {
      throw new IllegalArgumentException("Value must be smaller than modulus");
    }
  }
}
