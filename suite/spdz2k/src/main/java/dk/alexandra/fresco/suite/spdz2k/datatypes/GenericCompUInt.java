package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Implementation of {@link CompUInt} that allows for any size least-significant and
 * most-significant bit portion. <p>Note that this class is a lot slower than dedicated
 * implementations such as {@link CompUInt128}.</p>
 */
public class GenericCompUInt implements
    CompUInt<GenericCompUInt, GenericCompUInt, GenericCompUInt> {

  private final int lowBitLength;
  private final int[] ints;

  GenericCompUInt(final int[] ints, int lowBitLength) {
    this.lowBitLength = lowBitLength;
    this.ints = ints;
  }

  GenericCompUInt(int low, int requiredBitLength) {
    this(initIntArray(low, requiredBitLength));
  }

  GenericCompUInt(final int[] ints) {
    this(ints, ints.length / 2 * Integer.SIZE);
  }

  GenericCompUInt(GenericCompUInt other, int requiredBitLength) {
    this(pad(other.ints, requiredBitLength));
  }

  GenericCompUInt(byte[] bytes, int requiredBitLength) {
    this(toIntArray(bytes, requiredBitLength));
  }

  GenericCompUInt(BigInteger value, int requiredBitLength) {
    this(value.toByteArray(), requiredBitLength);
  }

  @Override
  public GenericCompUInt add(final GenericCompUInt other) {
    final int[] resultInts = new int[ints.length];
    long carry = 0;
    // big-endian, so reverse order
    for (int l = ints.length - 1; l >= 0; l--) {
      final long sum = UInt.toUnLong(ints[l]) + UInt.toUnLong(other.ints[l]) + carry;
      resultInts[l] = (int) sum;
      carry = sum >>> 32;
    }
    return new GenericCompUInt(resultInts);
  }

  @Override
  public GenericCompUInt multiply(final GenericCompUInt other) {
    // note that we assume that other has the same bit-length as this
    final int[] resultInts = new int[ints.length];
    for (int l = ints.length - 1; l >= 0; l--) {
      long carry = 0;
      int resIdxOffset = l;
      for (int r = ints.length - 1; r >= 0 && resIdxOffset >= 0; r--, resIdxOffset--) {
        final long product = UInt.toUnLong(ints[l]) * UInt.toUnLong(other.ints[r])
            + UInt.toUnLong(resultInts[resIdxOffset]) + carry;
        carry = product >>> 32;
        resultInts[resIdxOffset] = (int) product;
      }
    }
    return new GenericCompUInt(resultInts);
  }

  @Override
  public GenericCompUInt subtract(GenericCompUInt other) {
    return this.add(other.negate());
  }

  @Override
  public GenericCompUInt negate() {
    int[] reversed = new int[ints.length];
    for (int i = 0; i < reversed.length; i++) {
      reversed[i] = ~ints[i];
    }
    return new GenericCompUInt(reversed).add(one());
  }

  @Override
  public boolean isZero() {
    for (int val : ints) {
      if (val != 0) {
        return false;
      }
    }
    return true;
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
  public GenericCompUInt getLeastSignificant() {
    return getSubRange(0, getLowBitLength() / Integer.SIZE);
  }

  @Override
  public GenericCompUInt getLeastSignificantAsHigh() {
    return getSubRange(0, getHighBitLength() / Integer.SIZE);
  }

  @Override
  public GenericCompUInt getMostSignificant() {
    return getSubRange(getLowBitLength() / Integer.SIZE, getCompositeBitLength() / Integer.SIZE);
  }

  @Override
  public long toLong() {
    return (UInt.toUnLong(ints[ints.length - 2]) << 32) + UInt.toUnLong(ints[ints.length - 1]);
  }

  @Override
  public int toInt() {
    return ints[ints.length - 1];
  }

  @Override
  public GenericCompUInt shiftLowIntoHigh() {
    int[] shifted = new int[ints.length];
    int smallerByteLength = Integer.min(getLowBitLength(), getHighBitLength()) / Integer.SIZE;
    int lowByteLength = getLowBitLength() / Integer.SIZE;
    for (int i = 0; i < smallerByteLength; i++) {
      shifted[shifted.length - (lowByteLength + i + 1)] = ints[shifted.length - i - 1];
    }
    return new GenericCompUInt(shifted);
  }

  @Override
  public int getLowBitLength() {
    return lowBitLength;
  }

  @Override
  public int getHighBitLength() {
    return ints.length * Integer.SIZE - getLowBitLength();
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

  private GenericCompUInt one() {
    // TODO cache this
    int[] temp = new int[getCompositeBitLength() / Integer.SIZE];
    temp[temp.length - 1] = 1;
    return new GenericCompUInt(temp);
  }

  private GenericCompUInt getSubRange(int from, int to) {
    return new GenericCompUInt(getIntSubRange(from, to));
  }

  private int[] getIntSubRange(int from, int to) {
    // big-endian order so need to flip indexes
    int fromFlipped = ints.length - from - (to - from);
    int toFlipped = ints.length - from;
    return Arrays.copyOfRange(ints, fromFlipped, toFlipped);
  }

  private static int[] toIntArray(byte[] bytes, int requiredBitLength) {
    IntBuffer intBuf =
        ByteBuffer.wrap(CompUInt.pad(bytes, requiredBitLength))
            .order(ByteOrder.BIG_ENDIAN)
            .asIntBuffer();
    int[] array = new int[intBuf.remaining()];
    intBuf.get(array);
    return array;
  }

  private static int[] pad(int[] ints, int requiredBitLength) {
    int byteLength = requiredBitLength / Integer.SIZE;
    int[] padded = new int[byteLength];
    // assuming ints is shorter than required length
    System.arraycopy(ints, 0, padded, byteLength - ints.length, ints.length);
    return padded;
  }

  private static int[] initIntArray(int value, int requiredBitLength) {
    int[] temp = new int[requiredBitLength / Integer.SIZE];
    temp[temp.length - 1] = value;
    return temp;
  }

}
