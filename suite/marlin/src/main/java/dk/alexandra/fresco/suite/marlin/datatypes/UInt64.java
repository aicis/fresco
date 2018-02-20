package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import java.math.BigInteger;

// wrapper for long so that CompUInt instances can use it
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
    // TODO optimize if bottle-neck
    return new BigInteger(1, toByteArray());
  }

  @Override
  public UInt64 shiftLowIntoHigh() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getLowAsLong() {
    return value;
  }

}
