package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.value.OInt;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CompUInt96 implements CompUInt<UInt64, UInt32, CompUInt96> {
  private static final CompUInt96 ONE = new CompUInt96((int) 1);

  private final int high;
  private final int mid;
  private final int low;

  /**
   * Creates new {@link CompUInt96}.
   *
   * @param bytes bytes interpreted in big-endian order.
   */
  public CompUInt96(byte[] bytes) {
    byte[] padded = CompUInt.pad(bytes, 96);
    ByteBuffer buffer = ByteBuffer.wrap(padded);
    buffer.order(ByteOrder.BIG_ENDIAN);
    this.high = buffer.getInt();
    this.mid = buffer.getInt();
    this.low = buffer.getInt();
  }

  CompUInt96(int high, int mid, int low) {
    this.high = high;
    this.mid = mid;
    this.low = low;
  }

  CompUInt96(BigInteger value) {
    this(value.toByteArray());
  }

  CompUInt96(UInt64 value) {
    this(value.toLong());
  }

  CompUInt96(UInt32 value) {
    this(value.toInt());
  }

  CompUInt96(long value) {
    this.high = 0;
    this.mid = (int) (value >>> 32);
    this.low = (int) value;
  }

  CompUInt96(int value) {
    this.high = 0;
    this.mid = 0;
    this.low = value;
  }

  CompUInt96(CompUInt96 other) {
    this.high = other.high;
    this.mid = other.mid;
    this.low = other.low;
  }

  @Override
  public CompUInt96 add(CompUInt96 other) {
    long newLow = Integer.toUnsignedLong(this.low) + Integer.toUnsignedLong(other.low);
    long lowOverflow = newLow >>> 32;
    long newMid = Integer.toUnsignedLong(this.mid)
        + Integer.toUnsignedLong(other.mid)
        + lowOverflow;
    long midOverflow = newMid >>> 32;
    long newHigh = UInt.toUnLong(this.high) + UInt.toUnLong(other.high) + midOverflow;
    return new CompUInt96((int) newHigh, (int) newMid, (int) newLow);
  }

  @Override
  public CompUInt96 multiply(CompUInt96 other) {
    long thisLowAsLong = UInt.toUnLong(this.low);
    long thisMidAsLong = UInt.toUnLong(this.mid);
    long otherLowAsLong = UInt.toUnLong(other.low);
    long otherMidAsLong = UInt.toUnLong(other.mid);

    // low
    long t1 = thisLowAsLong * otherLowAsLong;
    long t2 = thisLowAsLong * otherMidAsLong;
    long t3 = thisLowAsLong * other.high;

    // mid
    long t4 = thisMidAsLong * otherLowAsLong;
    long t5 = thisMidAsLong * otherMidAsLong;
    int t6 = this.mid * other.high;

    // high
    long t7 = this.high * otherLowAsLong;
    int t8 = this.high * other.mid;
    // we don't need the product of this.high and other.high since those overflow 2^128

    long m1 = (t1 >>> 32) + (t2 & 0xffffffffL);
    int m2 = (int) m1;
    long newMid = UInt.toUnLong(m2) + (t4 & 0xffffffffL);

    long newHigh = (t2 >>> 32)
        + t3
        + (t4 >>> 32)
        + t5
        + (UInt.toUnLong(t6) << 32)
        + t7
        + (UInt.toUnLong(t8) << 32)
        + (m1 >>> 32)
        + (newMid >>> 32);
    return new CompUInt96((int) newHigh, (int) newMid, (int) t1);
  }

  @Override
  public CompUInt96 subtract(CompUInt96 other) {
    return this.add(other.negate());
  }

  @Override
  public CompUInt96 negate() {
    return new CompUInt96(~high, ~mid, ~low).add(ONE);
  }

  @Override
  public boolean isZero() {
    return high == 0 && mid == 0 && low == 0;
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  @Override
  public UInt32 getLeastSignificant() {
    return new UInt32(low);
  }

  @Override
  public UInt64 getMostSignificant() {
    return new UInt64((UInt.toUnLong(high) << 32) + UInt.toUnLong(mid));
  }

  @Override
  public UInt64 getLeastSignificantAsHigh() {
    return new UInt64(toLong());
  }

  @Override
  public long toLong() {
    return (UInt.toUnLong(mid) << 32) + (UInt.toUnLong(this.low));
  }

  @Override
  public int toInt() {
    return low;
  }

  @Override
  public CompUInt96 shiftLowIntoHigh() {
    return new CompUInt96(mid, low, 0);
  }

  @Override
  public CompUInt96 clearHighBits() {
    return new CompUInt96(0, 0, low);
  }

  @Override
  public CompUInt96 modTwoToKMinOne() {
    int newLow = (int) (~(1L << 31) & low);
    return new CompUInt96(0, 0, newLow);
  }

  @Override
  public int getLowBitLength() {
    return 32;
  }

  @Override
  public int getHighBitLength() {
    return 64;
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

  @Override
  public int getBitLength() {
    return 96;
  }

  @Override
  public byte[] toByteArray() {
    ByteBuffer buffer = ByteBuffer.allocate(getBitLength() / 8);
    buffer.order(ByteOrder.BIG_ENDIAN);
    buffer.putInt(high);
    buffer.putInt(mid);
    buffer.putInt(low);
    buffer.flip();
    return buffer.array();
  }

  @Override
  public OInt out() {
    return this;
  }

}
