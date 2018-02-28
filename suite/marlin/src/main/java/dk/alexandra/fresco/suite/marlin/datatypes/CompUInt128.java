package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Unsigned 128-bit integer with support for in-place operations. <p>Loosely follows this article
 * https://locklessinc.com/articles/256bit_arithmetic/. Note that this class is NOT SAFE to
 * instantiate with negative values.</p>
 */
public class CompUInt128 implements CompUInt<UInt64, UInt64, CompUInt128> {

  private static final CompUInt128 ONE = new CompUInt128(1);

  private final long high;
  private final int mid;
  private final int low;

  /**
   * Creates new {@link CompUInt128}.
   *
   * @param bytes bytes interpreted in big-endian order.
   */
  public CompUInt128(byte[] bytes) {
    byte[] padded = pad(bytes);
    ByteBuffer buffer = ByteBuffer.wrap(padded);
    buffer.order(ByteOrder.BIG_ENDIAN);
    this.high = buffer.getLong();
    this.mid = buffer.getInt();
    this.low = buffer.getInt();
  }

  private CompUInt128(long high, int mid, int low) {
    this.high = high;
    this.mid = mid;
    this.low = low;
  }

  CompUInt128(BigInteger value) {
    this(value.toByteArray());
  }

  public CompUInt128(UInt64 value) {
    this(value.toLong());
  }

  public CompUInt128(long value) {
    this.high = 0;
    this.mid = (int) (value >>> 32);
    this.low = (int) value;
  }

  CompUInt128(CompUInt128 other) {
    this.high = other.high;
    this.mid = other.mid;
    this.low = other.low;
  }

  @Override
  public CompUInt128 add(CompUInt128 other) {
    long newLow = Integer.toUnsignedLong(this.low) + Integer.toUnsignedLong(other.low);
    long lowOverflow = newLow >>> 32;
    long newMid = Integer.toUnsignedLong(this.mid)
        + Integer.toUnsignedLong(other.mid)
        + lowOverflow;
    long midOverflow = newMid >>> 32;
    long newHigh = this.high + other.high + midOverflow;
    return new CompUInt128(newHigh, (int) newMid, (int) newLow);
  }

  @Override
  public CompUInt128 multiply(CompUInt128 other) {
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
    int t6 = (int) (this.mid * other.high);

    // high
    long t7 = this.high * otherLowAsLong;
    int t8 = (int) (this.high * other.mid);
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
    return new CompUInt128(newHigh, (int) newMid, (int) t1);
  }

  @Override
  public CompUInt128 subtract(CompUInt128 other) {
    return this.add(other.negate());
  }

  @Override
  public CompUInt128 negate() {
    return new CompUInt128(~high, ~mid, ~low).add(ONE);
  }

  @Override
  public boolean isZero() {
    return low == 0 && mid == 0 && high == 0;
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  @Override
  public UInt64 computeOverflow() {
    CompUInt128 low = new CompUInt128(getLeastSignificant());
    return low.subtract(this).getMostSignificant();
  }

  @Override
  public UInt64 getLeastSignificant() {
    return new UInt64(toLong());
  }

  @Override
  public UInt64 getMostSignificant() {
    return new UInt64(high);
  }

  @Override
  public UInt64 getLeastSignificantAsHigh() {
    return getLeastSignificant();
  }

  @Override
  public long toLong() {
    return (UInt.toUnLong(this.mid) << 32) + UInt.toUnLong(this.low);
  }

  @Override
  public int toInt() {
    return low;
  }

  @Override
  public CompUInt128 shiftLowIntoHigh() {
    return new CompUInt128(toLong(), 0, 0);
  }

  @Override
  public int getLowBitLength() {
    return 64;
  }

  @Override
  public int getHighBitLength() {
    return 64;
  }

  private byte[] pad(byte[] bytes) {
    byte[] padded = new byte[getBitLength() / 8];
    // potentially drop byte containing sign bit
    boolean dropSignBitByte = (bytes[0] == 0x00);
    int bytesLen = dropSignBitByte ? bytes.length - 1 : bytes.length;
    if (bytesLen > padded.length) {
      throw new IllegalArgumentException("Exceeds capacity");
    }
    int srcPos = dropSignBitByte ? 1 : 0;
    System.arraycopy(bytes, srcPos, padded, padded.length - bytesLen, bytesLen);
    return padded;
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

  @Override
  public byte[] toByteArray() {
    ByteBuffer buffer = ByteBuffer.allocate(getBitLength() / 8);
    buffer.order(ByteOrder.BIG_ENDIAN);
    buffer.putLong(high);
    buffer.putInt(mid);
    buffer.putInt(low);
    buffer.flip();
    return buffer.array();
  }

}
