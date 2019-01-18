package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.value.OInt;
import java.math.BigInteger;

public class CompUInt64 implements CompUInt<UInt32, UInt32, CompUInt64> {

  private static final CompUInt64 ZERO = new CompUInt64(0);
  private static final CompUInt64 ONE = new CompUInt64(1);

  protected final long value;

  public CompUInt64(byte[] bytes) {
    this(bytes, false);
  }

  public CompUInt64(byte[] bytes, boolean requiresPadding) {
    byte[] padded = requiresPadding ? CompUInt.pad(bytes, 64) : bytes;
    if (padded.length == 4) {
      // we are instantiating from the least significant bits only
      this.value = UInt.toUnLong(ByteAndBitConverter.toInt(padded, 0));
    } else {
      this.value = ByteAndBitConverter.toLong(padded, 0);
    }
  }

  public CompUInt64(long value) {
    this.value = value;
  }

  public CompUInt64(BigInteger value) {
    this.value = value.longValue();
  }

  @Override
  public UInt32 getMostSignificant() {
    return new UInt32((int) (value >>> 32));
  }

  @Override
  public UInt32 getLeastSignificant() {
    return new UInt32((int) (value & 0xfffffffffL));
  }

  @Override
  public UInt32 getLeastSignificantAsHigh() {
    return new UInt32((int) (value & 0xfffffffffL));
  }

  @Override
  public CompUInt64 shiftLeftSmall(int n) {
    return new CompUInt64(value << n);
  }

  @Override
  public CompUInt64 shiftRightSmall(int n) {
    return new CompUInt64(value >>> n);
  }

  @Override
  public CompUInt64 shiftRightLowOnly(int n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompUInt64 shiftLowIntoHigh() {
    return new CompUInt64(value << 32);
  }

  @Override
  public CompUInt64 clearAboveBitAt(int bitPos) {
    long mask = ~(0x8000000000000000L >> (63 - bitPos));
    return new CompUInt64(value & mask);
  }

  @Override
  public CompUInt64 clearHighBits() {
    return new CompUInt64((value & 0xffffffffL));
  }

  @Override
  public CompUInt64 toBitRep() {
    return new CompUInt64Bit(value);
  }

  @Override
  public CompUInt64 toArithmeticRep() {
    throw new IllegalStateException("Already arithmetic");
  }

  @Override
  public CompUInt64 multiplyByBit(int bit) {
    return new CompUInt64(this.value * bit);
  }

  @Override
  public boolean testBit(int bitPos) {
    return (((1L << bitPos) & value) >>> bitPos) == 1;
  }

  @Override
  public CompUInt64 testBitAsUInt(int bitPos) {
    return testBit(bitPos) ? ONE : ZERO;
  }

  @Override
  public int getLowBitLength() {
    return 32;
  }

  @Override
  public int getHighBitLength() {
    return 32;
  }

  @Override
  public byte[] serializeLeastSignificant() {
    return ByteAndBitConverter.toByteArray((int) (value));
  }

  @Override
  public int bitValue() {
    return (int) value & 1; // lowest bit
  }

  @Override
  public OInt out() {
    return this;
  }

  @Override
  public CompUInt64 add(CompUInt64 other) {
    return new CompUInt64(value + other.value);
  }

  @Override
  public CompUInt64 multiply(CompUInt64 other) {
    return new CompUInt64(value * other.value);
  }

  @Override
  public CompUInt64 subtract(CompUInt64 other) {
    return new CompUInt64(value - other.value);
  }

  @Override
  public CompUInt64 negate() {
    return new CompUInt64(-value);
  }

  @Override
  public boolean isZero() {
    return value == 0;
  }

  @Override
  public boolean isOne() {
    return value == 1;
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
    return (int) (value & 0xffffffffL);
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

}
