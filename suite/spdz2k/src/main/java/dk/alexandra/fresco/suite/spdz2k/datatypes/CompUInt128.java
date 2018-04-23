package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.value.OInt;
import java.math.BigInteger;

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
   * Creates new {@link CompUInt128}. <p>Do <b>not</b> pad bytes by default.</p>
   *
   * @param bytes bytes interpreted in big-endian order.
   */
  public CompUInt128(byte[] bytes) {
    this(bytes, false);
  }

  /**
   * Creates new {@link CompUInt128}.
   *
   * @param bytes bytes interpreted in big-endian order.
   * @param requiresPadding indicates if the bytes need to be padded up to 16 bytes.
   */
  public CompUInt128(byte[] bytes, boolean requiresPadding) {
    byte[] padded = requiresPadding ? CompUInt.pad(bytes, 128) : bytes;
    if (padded.length == 8) {
      // we are instantiating from the least significant bits only
      this.high = 0L;
      this.mid = toInt(padded, 4);
      this.low = toInt(padded, 0);
    } else {
      this.high = toLong(padded, 8);
      this.mid = toInt(padded, 4);
      this.low = toInt(padded, 0);
    }
  }

  /**
   * Creates new {@link CompUInt128} from {@link BigInteger}.
   */
  public CompUInt128(BigInteger value) {
    this(value.toByteArray(), true);
  }

  CompUInt128(long high, int mid, int low) {
    this.high = high;
    this.mid = mid;
    this.low = low;
  }

  CompUInt128(UInt64 value) {
    this(value.toLong());
  }

  CompUInt128(long value) {
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

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

  @Override
  public byte[] toByteArray() {
    byte[] bytes = new byte[16];
    toByteArray(bytes, 0, low);
    toByteArray(bytes, 4, mid);
    toByteArrayLong(bytes, 8, high);
    return bytes;
  }

  @Override
  public boolean testBit(int bit) {
    // TODO optimize if bottle-neck
    long section;
    int relative;
    if (bit < Integer.SIZE) {
      section = low;
      relative = bit;
    } else if (bit < Long.SIZE) {
      section = mid;
      relative = bit - Integer.SIZE;
    } else {
      section = high;
      relative = bit - Long.SIZE;
    }
    return (((1L << relative) & section) >>> relative) == 1;
  }

  @Override
  public OInt out() {
    return this;
  }

  private void toByteArrayLong(byte[] bytes, int start, long value) {
    int offset = bytes.length - start - 1;
    for (int i = 0; i < 8; i++) {
      bytes[offset - i] = (byte) (value & 0xFF);
      value >>>= 8;
    }
  }

  private void toByteArray(byte[] bytes, int start, int value) {
    int offset = bytes.length - start - 1;
    for (int i = 0; i < 4; i++) {
      bytes[offset - i] = (byte) (value & 0xFF);
      value >>>= 8;
    }
  }

  private static long toLong(byte[] bytes, int start) {
    int flipped = bytes.length - start - 1;
    return (bytes[flipped] & 0xFFL)
        | (bytes[flipped - 1] & 0xFFL) << 8
        | (bytes[flipped - 2] & 0xFFL) << 16
        | (bytes[flipped - 3] & 0xFFL) << 24
        | (bytes[flipped - 4] & 0xFFL) << 32
        | (bytes[flipped - 5] & 0xFFL) << 40
        | (bytes[flipped - 6] & 0xFFL) << 48
        | (bytes[flipped - 7] & 0xFFL) << 56;
  }

  private static int toInt(byte[] bytes, int start) {
    int flipped = bytes.length - start - 1;
    return (bytes[flipped] & 0xFF)
        | (bytes[flipped - 1] & 0xFF) << 8
        | (bytes[flipped - 2] & 0xFF) << 16
        | (bytes[flipped - 3] & 0xFF) << 24;
  }

}
