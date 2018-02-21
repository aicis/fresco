package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CompUInt96 implements CompUInt<UInt64, UInt32, CompUInt96> {

  private static final CompUInt96 ONE = new CompUInt96(1);

  private final long high;
  private final int low;

  /**
   * Creates new {@link CompUInt96}.
   *
   * @param bytes bytes interpreted in big-endian order.
   */
  public CompUInt96(byte[] bytes) {
    byte[] padded = pad(bytes);
    ByteBuffer buffer = ByteBuffer.wrap(padded);
    buffer.order(ByteOrder.BIG_ENDIAN);
    this.high = buffer.getLong();
    this.low = buffer.getInt();
  }

  private CompUInt96(long high, int low) {
    this.high = high;
    this.low = low;
  }

  CompUInt96(BigInteger value) {
    this(value.toByteArray());
  }

  public CompUInt96(UInt64 value) {
    this(value.toLong());
  }

  public CompUInt96(UInt32 value) {
    this(value.toInt());
  }

  public CompUInt96(long value) {
    this.high = (value >>> 32);
    this.low = (int) value;
  }

  public CompUInt96(int value) {
    this.high = 0;
    this.low = value;
  }

  CompUInt96(CompUInt96 other) {
    this.high = other.high;
    this.low = other.low;
  }

  @Override
  public CompUInt96 add(CompUInt96 other) {
    long newLow = Integer.toUnsignedLong(this.low) + Integer.toUnsignedLong(other.low);
    long lowOverflow = newLow >>> 32;
    long newHigh = this.high + other.high + lowOverflow;
    return new CompUInt96(newHigh, (int) newLow);
  }

  @Override
  public CompUInt96 multiply(CompUInt96 other) {
    long thisLowAsLong = UInt.toUnLong(this.low);
    long otherLowAsLong = UInt.toUnLong(other.low);

    // low
    long t1 = thisLowAsLong * otherLowAsLong;
    long t3 = thisLowAsLong * other.high;

    // high
    long t7 = this.high * otherLowAsLong;

    long m1 = (t1 >>> 32);
    int m2 = (int) m1;
    long newMid = UInt.toUnLong(m2);

    long newHigh = t3 + t7 + (m1 >>> 32) + (newMid >>> 32);
    return new CompUInt96(newHigh, (int) t1);
  }

  @Override
  public CompUInt96 subtract(CompUInt96 other) {
    return this.add(other.negate());
  }

  @Override
  public CompUInt96 negate() {
    return new CompUInt96(~high, ~low).add(ONE);
  }

  @Override
  public boolean isZero() {
    return low == 0 && high == 0;
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  @Override
  public UInt64 computeOverflow() {
    CompUInt96 low = new CompUInt96(getLeastSignificant());
    return low.subtract(this).getMostSignificant();
  }

  @Override
  public UInt32 getLeastSignificant() {
    return new UInt32(toInt());
  }

  @Override
  public UInt64 getMostSignificant() {
    return new UInt64(high);
  }

  @Override
  public UInt64 getLeastSignificantAsHigh() {
    return new UInt64(toLong());
  }

  @Override
  public long toLong() {
    return (high << 32) + (UInt.toUnLong(this.low));
  }

  @Override
  public int toInt() {
    return low;
  }

  @Override
  public CompUInt96 shiftLowIntoHigh() {
    return new CompUInt96(toLong(), 0);
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
    buffer.putInt(low);
    buffer.flip();
    return buffer.array();
  }

}
