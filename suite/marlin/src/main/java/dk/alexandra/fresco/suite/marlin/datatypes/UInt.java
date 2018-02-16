package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class UInt implements BigUInt<UInt> {

  private final int[] ints;

  public UInt(int[] chunks) {
    this.ints = chunks;
  }

  public UInt(byte[] bytes, int requiredBitLength) {
    this(toIntArray(bytes, requiredBitLength));
  }

  public UInt(BigInteger value, int requiredBitLength) {
    this(value.toByteArray(), requiredBitLength);
  }

  public UInt(long value, int requiredBitLength) {
    this(longToInts(value, requiredBitLength));
  }

  @Override
  public UInt add(UInt other) {
    int[] resultInts = new int[ints.length];
    long carry = 0;
    // big-endian, so reverse order
    for (int l = ints.length - 1; l >= 0; l--) {
      long sum = toULong(ints[l]) + toULong(other.ints[l]) + carry;
      resultInts[l] = (int) sum;
      carry = sum >>> 32;
    }
    return new UInt(resultInts);
  }

  @Override
  public UInt multiply(UInt other) {
    // TODO get rid off extra multiplication
    // note that we assume that other has the same bit-length as this
    int[] resultInts = new int[ints.length];
    for (int l = resultInts.length - 1; l >= 0; l--) {
      long carry = 0;
      int resIdxOffset = l;
      for (int r = resultInts.length - 1; r >= 0 && resIdxOffset >= 0; r--, resIdxOffset--) {
        long product = toULong(ints[l]) * toULong(other.ints[r])
            + toULong(resultInts[resIdxOffset]) + carry;
        carry = product >>> 32;
        resultInts[resIdxOffset] = (int) product;
      }
    }
    return new UInt(resultInts);
  }

  @Override
  public UInt subtract(UInt other) {
    return this.add(other.negate());
  }

  @Override
  public UInt negate() {
    int[] reversed = new int[ints.length];
    for (int i = 0; i < reversed.length; i++) {
      reversed[i] = ~ints[i];
    }
    // TODO cache this
    UInt one = new UInt(1, 128);
    return new UInt(reversed).add(one);
  }

  @Override
  public boolean isZero() {
    return false;
  }

  @Override
  public int getBitLength() {
    return ints.length * Integer.SIZE;
  }

  @Override
  public byte[] toByteArray() {
    ByteBuffer buffer = ByteBuffer.allocate(getBitLength() / Byte.SIZE);
    IntBuffer intBuffer = buffer.asIntBuffer();
    intBuffer.put(ints);
    return buffer.array();
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  @Override
  public long getLow() {
    return 0;
  }

  @Override
  public long getHigh() {
    return 0;
  }

  @Override
  public UInt shiftLowIntoHigh() {
    return null;
  }

  private long toULong(int value) {
    return Integer.toUnsignedLong(value);
  }

  private static int[] toIntArray(byte[] bytes, int requiredBitLength) {
    IntBuffer intBuf =
        ByteBuffer.wrap(pad(bytes, requiredBitLength))
            .order(ByteOrder.BIG_ENDIAN)
            .asIntBuffer();
    int[] array = new int[intBuf.remaining()];
    intBuf.get(array);
    return array;
  }

  private static byte[] pad(byte[] bytes, int requiredBitLength) {
    byte[] padded = new byte[requiredBitLength / 8];
    // potentially drop byte containing sign bit
    boolean dropSignBitByte = (bytes[0] == 0x00);
    int bytesLength = dropSignBitByte ? bytes.length - 1 : bytes.length;
    if (bytesLength > padded.length) {
      throw new IllegalArgumentException("Exceeds capacity");
    }
    int srcPos = dropSignBitByte ? 1 : 0;
    System.arraycopy(bytes, srcPos, padded, padded.length - bytesLength, bytesLength);
    return padded;
  }

  private static int[] longToInts(long value, int requiredBitLength) {
    int[] ints = new int[requiredBitLength / Integer.SIZE];
    ints[ints.length - 1] = (int) value;
    ints[ints.length - 2] = (int) (value >>> 32);
    return ints;
  }

}
