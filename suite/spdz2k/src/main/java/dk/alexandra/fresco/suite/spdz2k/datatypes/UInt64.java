package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import java.math.BigInteger;

/**
 * A wrapper for the long type adhering to the {@link UInt} interface so that it can be used by
 * {@link CompUInt} instances.
 */
public class UInt64 implements UInt<UInt64> {

  private final long value;

  public UInt64(long value) {
    this.value = value;
  }

  @Override
  public UInt64 add(UInt64 other) {
    return new UInt64(value + other.value);
  }

  @Override
  public UInt64 multiply(UInt64 other) {
    return new UInt64(value * other.value);
  }

  @Override
  public UInt64 subtract(UInt64 other) {
    return new UInt64(value - other.value);
  }

  @Override
  public UInt64 negate() {
    return new UInt64(-value);
  }

  @Override
  public boolean isZero() {
    return value == 0;
  }

  @Override
  public int getBitLength() {
    return 64;
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
    return value;
  }

  @Override
  public int toInt() {
    return (int) value;
  }

  @Override
  public String toString() {
    return Long.toUnsignedString(value);
  }
}
