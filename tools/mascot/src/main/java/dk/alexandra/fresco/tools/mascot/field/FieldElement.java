package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;
import java.math.BigInteger;
import java.util.function.BinaryOperator;


public final class FieldElement implements Addable<FieldElement> {

  final BigInteger value;
  final BigInteger modulus;
  final int bitLength;

  /**
   * Creates new field element.
   * 
   * @param value value of element
   * @param modulus modulus defining field
   * @param bitLength bit length of modulus
   */
  public FieldElement(BigInteger value, BigInteger modulus, int bitLength) {
    sanityCheck(value, modulus, bitLength);
    this.value = value;
    this.modulus = modulus;
    this.bitLength = bitLength;
  }

  public FieldElement(FieldElement other) {
    this(other.value, other.modulus, other.bitLength);
  }

  public FieldElement(String value, String modulus, int bitLength) {
    this(new BigInteger(value), new BigInteger(modulus), bitLength);
  }

  public FieldElement(long value, BigInteger modulus, int bitLength) {
    this(BigInteger.valueOf(value), modulus, bitLength);
  }

  public FieldElement(byte[] value, BigInteger modulus, int bitLength) {
    this(new BigInteger(1, value), modulus, bitLength);
  }

  private FieldElement binaryOp(BinaryOperator<BigInteger> op, FieldElement left,
      FieldElement right) {
    return new FieldElement(op.apply(left.toBigInteger(), right.toBigInteger()).mod(modulus),
        this.modulus, this.bitLength);
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

  public BigInteger toBigInteger() {
    return this.value;
  }

  public boolean getBit(int bitIndex) {
    return value.testBit(bitIndex);
  }

  public FieldElement select(boolean bit) {
    return bit ? this : new FieldElement(BigInteger.ZERO, modulus, bitLength);
  }

  /**
   * Converts value into byte array. <br>
   * Result is guaranteed to exactly bitLength / 8 long.
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
    return new StrictBitVector(toByteArray(), bitLength);
  }

  public BigInteger getModulus() {
    return this.modulus;
  }

  public int getBitLength() {
    return bitLength;
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FieldElement other = (FieldElement) obj;
    if (bitLength != other.bitLength) {
      return false;
    }
    if (modulus == null) {
      if (other.modulus != null) {
        return false;
      }
    } else if (!modulus.equals(other.modulus)) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  private void sanityCheck(BigInteger value, BigInteger modulus, int bitLength) {
    if (bitLength % 8 != 0) {
      throw new IllegalArgumentException("Bit length must be multiple of 8");
    } else if (value.signum() == -1) {
      throw new IllegalArgumentException("Cannot have negative value");
    } else if (modulus.signum() == -1) {
      throw new IllegalArgumentException("Cannot have negative modulus");
    } else if (modulus.bitLength() != bitLength) {
      throw new IllegalArgumentException("Modulus bit length must match bit length");
    } else if (value.compareTo(modulus) != -1) {
      throw new IllegalArgumentException("Value must be smaller than modulus");
    }
  }

}
