package dk.alexandra.fresco.tools.mascot.field;

import java.math.BigInteger;
import java.util.List;
import java.util.function.BinaryOperator;

public class FieldElement {

  private BigInteger value;
  private BigInteger modulus;
  private int bitLength;

  public FieldElement(BigInteger value, BigInteger modulus, int bitLength) {
    this.value = value;
    this.modulus = modulus;
    this.bitLength = bitLength;
  }

  public FieldElement(int value, BigInteger modulus, int bitLength) {
    this.value = BigInteger.valueOf(value);
    this.modulus = modulus;
    this.bitLength = bitLength;
  }

  public static FieldElement recombine(FieldElement generator, List<FieldElement> elements) {
    // TODO: optimize
    BigInteger modulus = generator.modulus;
    int bitLength = generator.bitLength;
    FieldElement accumulator = new FieldElement(BigInteger.ZERO, modulus, bitLength);
    int power = 0;
    for (FieldElement element : elements) {
      // TODO: do we need/ want modular exponentiation?
      accumulator = accumulator.add(generator.pow(power).multiply(element));
      power++;
    }
    return accumulator;
  }

  public static FieldElement recombine(List<FieldElement> elements, BigInteger modulus,
      int bitLength) {
    FieldElement generator = new FieldElement(BigInteger.valueOf(2), modulus, bitLength);
    return FieldElement.recombine(generator, elements);
  }

  private FieldElement binaryOp(BinaryOperator<BigInteger> op, FieldElement left,
      FieldElement right) {
    // TODO: check that modulus and size are same
    return new FieldElement(op.apply(left.getValue(), right.getValue()).mod(modulus), this.modulus,
        this.bitLength);
  }

  public FieldElement pow(int exponent) {
    return new FieldElement(this.value.pow(exponent).mod(modulus), modulus, bitLength);
  }

  public FieldElement add(FieldElement other) {
    return binaryOp((l, r) -> l.add(r), this, other);
  }

  public FieldElement subtract(FieldElement other) {
    return binaryOp((l, r) -> l.subtract(r), this, other);
  }

  public FieldElement multiply(FieldElement other) {
    return binaryOp((l, r) -> l.multiply(r), this, other);
  }

  public FieldElement negate() {
    return new FieldElement(value.multiply(BigInteger.valueOf(-1)).mod(modulus), modulus,
        bitLength);
  }

  BigInteger getValue() {
    return this.value;
  }

  public BigInteger getModulus() {
    return this.modulus;
  }

  public byte[] toByteArray() {
    // TODO: bit length?
    return value.toByteArray();
  }

  public BigInteger toBigInteger() {
    return this.value;
  }

  public boolean getBit(int bitIndex) {
    return value.testBit(bitIndex);
  }

  public FieldElement select(boolean bit) {
    return bit ? this : new FieldElement(BigInteger.ZERO, modulus, bitLength);
  }

  @Override
  public String toString() {
    return "FieldElement [value=" + value + ", modulus=" + modulus + ", bitLength=" + bitLength
        + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + bitLength;
    result = prime * result + ((modulus == null) ? 0 : modulus.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FieldElement other = (FieldElement) obj;
    if (bitLength != other.bitLength)
      return false;
    if (modulus == null) {
      if (other.modulus != null)
        return false;
    } else if (!modulus.equals(other.modulus))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  public int getBitLenght() {
    return bitLength;
  }

}