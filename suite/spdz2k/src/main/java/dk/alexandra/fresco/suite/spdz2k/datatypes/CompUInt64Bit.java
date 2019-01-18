package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;

public class CompUInt64Bit extends CompUInt64 {

  public CompUInt64Bit(int high, int bit) {
    super((UInt.toUnLong(high) << 1) | UInt.toUnLong(bit));
  }

  public CompUInt64Bit(long value) {
    super(value >>> 31);
  }

  public CompUInt64Bit(long value, boolean shift) {
    super(value);
  }

  @Override
  public CompUInt64 toBitRep() {
    throw new IllegalStateException("Already in bit form.");
  }

  @Override
  public CompUInt64 toArithmeticRep() {
    return new CompUInt64(value << 31);
  }

  @Override
  public BigInteger toBigInteger() {
    return toArithmeticRep().toBigInteger();
  }

  @Override
  public CompUInt64 multiply(CompUInt64 other) {
    return new CompUInt64Bit(value * other.value, false);
  }

  @Override
  public CompUInt64 add(CompUInt64 other) {
    return new CompUInt64Bit(value + other.value, false);
  }

  @Override
  public String toString() {
    return toBigInteger().toString() + "B";
  }

  @Override
  public CompUInt64 subtract(CompUInt64 other) {
    throw new UnsupportedOperationException("Subtraction not supported by bit representation");
  }

  @Override
  public CompUInt64 negate() {
    throw new UnsupportedOperationException("Negation not supported by bit representation");
  }

  @Override
  public int bitValue() {
    return (int) (value & 1L);
  }

  @Override
  public byte[] serializeLeastSignificant() {
    return new byte[]{(byte) bitValue()};
  }

  @Override
  public CompUInt64 multiplyByBit(int bitValue) {
    return new CompUInt64Bit(value * UInt.toUnLong(bitValue), false);
  }

}
