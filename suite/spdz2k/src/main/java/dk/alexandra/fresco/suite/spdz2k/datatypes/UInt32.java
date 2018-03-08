package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import java.math.BigInteger;

/**
 * A wrapper for the int type adhering to the {@link UInt} interface so that it can be used by
 * {@link CompUInt} instances.
 */
public class UInt32 implements UInt<UInt32> {

  private final int value;

  public UInt32(int value) {
    this.value = value;
  }

  @Override
  public UInt32 add(UInt32 other) {
    return new UInt32(value + other.value);
  }

  @Override
  public UInt32 multiply(UInt32 other) {
    return new UInt32(value * other.value);
  }

  @Override
  public UInt32 subtract(UInt32 other) {
    return new UInt32(value - other.value);
  }

  @Override
  public UInt32 negate() {
    return new UInt32(-value);
  }

  @Override
  public boolean isZero() {
    return value == 0;
  }

  @Override
  public int getBitLength() {
    return 32;
  }

  @Override
  public byte[] toByteArray() {
    return ByteAndBitConverter.toByteArray(value);
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  @Override
  public long toLong() {
    return Integer.toUnsignedLong(value);
  }

  @Override
  public int toInt() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toUnsignedString(value);
  }

}
