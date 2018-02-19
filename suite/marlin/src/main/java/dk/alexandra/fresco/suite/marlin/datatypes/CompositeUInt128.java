package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Unsigned 128-bit integer with support for in-place operations. <p>Loosely follows this article
 * https://locklessinc.com/articles/256bit_arithmetic/. Note that this class is NOT SAFE to
 * instantiate with negative values.</p>
 */
public class CompositeUInt128 implements CompositeUInt<CompositeUInt128> {

  private static final CompositeUInt128 MINUS_ONE = new CompositeUInt128(
      BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE));
  private final long high;
  private final int mid;
  private final int low;

  /**
   * Creates new {@link CompositeUInt128}.
   *
   * @param bytes bytes interpreted in big-endian order.
   */
  public CompositeUInt128(byte[] bytes) {
    byte[] padded = pad(bytes);
    ByteBuffer buffer = ByteBuffer.wrap(padded);
    buffer.order(ByteOrder.BIG_ENDIAN);
    this.high = buffer.getLong();
    this.mid = buffer.getInt();
    this.low = buffer.getInt();
  }

  private CompositeUInt128(long high, int mid, int low) {
    this.high = high;
    this.mid = mid;
    this.low = low;
  }

  CompositeUInt128(BigInteger value) {
    this(value.toByteArray());
  }

  public CompositeUInt128(long value) {
    this.high = 0;
    this.mid = (int) (value >>> 32);
    this.low = (int) value;
  }

  CompositeUInt128(CompositeUInt128 other) {
    this.high = other.high;
    this.mid = other.mid;
    this.low = other.low;
  }

  @Override
  public CompositeUInt128 add(CompositeUInt128 other) {
    long newLow = Integer.toUnsignedLong(this.low) + Integer.toUnsignedLong(other.low);
    long lowOverflow = newLow >>> 32;
    long newMid = Integer.toUnsignedLong(this.mid)
        + Integer.toUnsignedLong(other.mid)
        + lowOverflow;
    long midOverflow = newMid >>> 32;
    long newHigh = this.high + other.high + midOverflow;
    return new CompositeUInt128(newHigh, (int) newMid, (int) newLow);
  }

  @Override
  public CompositeUInt128 multiply(CompositeUInt128 other) {
    long thisLowAsLong = Integer.toUnsignedLong(this.low);
    long thisMidAsLong = Integer.toUnsignedLong(this.mid);
    long otherLowAsLong = Integer.toUnsignedLong(other.low);
    long otherMidAsLong = Integer.toUnsignedLong(other.mid);

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
    long newMid = Integer.toUnsignedLong(m2) + (t4 & 0xffffffffL);

    long newHigh = (t2 >>> 32)
        + t3
        + (t4 >>> 32)
        + t5
        + (Integer.toUnsignedLong(t6) << 32)
        + t7
        + (Integer.toUnsignedLong(t8) << 32)
        + (m1 >>> 32)
        + (newMid >>> 32);
    return new CompositeUInt128(newHigh, (int) newMid, (int) t1);
  }

  @Override
  public CompositeUInt128 subtract(CompositeUInt128 other) {
    return this.add(other.negate());
  }

  @Override
  public CompositeUInt128 negate() {
    return multiply(MINUS_ONE);
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
  public CompositeUInt128 computeOverflow() {
    return null;
  }

  @Override
  public CompositeUInt128 getSubRange(int from, int to) {
    return null;
  }

  @Override
  public CompositeUInt128 getLow() {
    return null;
  }

  @Override
  public CompositeUInt128 getHigh() {
    return null;
  }

  private long getLowLong() {
    return (Integer.toUnsignedLong(this.mid) << 32) + Integer.toUnsignedLong(this.low);
  }

  @Override
  public CompositeUInt128 shiftLowIntoHigh() {
    return new CompositeUInt128(getLowLong(), 0, 0);
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
  public int getBitLength() {
    return 128;
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
