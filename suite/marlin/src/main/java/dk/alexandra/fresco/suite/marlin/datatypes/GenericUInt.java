package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericUInt implements CompUInt<GenericUInt, GenericUInt, GenericUInt> {

  private final int[] ints;

  public GenericUInt(final int[] ints) {
    this.ints = ints;
  }

  public GenericUInt(GenericUInt other, int requiredBitLength) {
    this(pad(other.ints, requiredBitLength));
  }

  public GenericUInt(byte[] bytes, int requiredBitLength) {
    this(toIntArray(bytes, requiredBitLength));
  }

  public GenericUInt(BigInteger value, int requiredBitLength) {
    this(value.toByteArray(), requiredBitLength);
  }

  public GenericUInt(long value, int requiredBitLength) {
    this(longToInts(value, requiredBitLength));
  }

  @Override
  public GenericUInt add(final GenericUInt other) {
    final int[] resultInts = new int[ints.length];
    long carry = 0;
    // big-endian, so reverse order
    for (int l = ints.length - 1; l >= 0; l--) {
      final long sum = toULong(ints[l]) + toULong(other.ints[l]) + carry;
      resultInts[l] = (int) sum;
      carry = sum >>> 32;
    }
    return new GenericUInt(resultInts);
  }

  @Override
  public GenericUInt multiply(final GenericUInt other) {
    // TODO get rid off extra multiplication
    // note that we assume that other has the same bit-length as this
    final int[] resultInts = new int[ints.length];
    for (int l = ints.length - 1; l >= 0; l--) {
      long carry = 0;
      int resIdxOffset = l;
      for (int r = ints.length - 1; r >= 0 && resIdxOffset >= 0; r--, resIdxOffset--) {
        final long product = toULong(ints[l]) * toULong(other.ints[r])
            + toULong(resultInts[resIdxOffset]) + carry;
        carry = product >>> 32;
        resultInts[resIdxOffset] = (int) product;
      }
    }
    return new GenericUInt(resultInts);
  }

  @Override
  public GenericUInt subtract(GenericUInt other) {
    return this.add(other.negate());
  }

  @Override
  public GenericUInt negate() {
    int[] reversed = new int[ints.length];
    for (int i = 0; i < reversed.length; i++) {
      reversed[i] = ~ints[i];
    }
    // TODO cache this
    GenericUInt one = new GenericUInt(1, 128);
    return new GenericUInt(reversed).add(one);
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
  public GenericUInt computeOverflow() {
    GenericUInt low = new GenericUInt(getLowAsLong(), ints.length * Integer.SIZE);
    return low.subtract(this).getHigh();
  }

  private GenericUInt getSubRange(int from, int to) {
    return new GenericUInt(getIntSubRange(from, to));
  }

  @Override
  public GenericUInt getLow() {
    return getSubRange(0, 2);
  }

  @Override
  public GenericUInt getHigh() {
    return getSubRange(2, 4);
  }

  @Override
  public long getLowAsLong() {
    return (toULong(ints[ints.length - 2]) << 32) + toULong(ints[ints.length - 1]);
  }

  @Override
  public GenericUInt shiftLowIntoHigh() {
    int[] shifted = new int[ints.length];
    shifted[0] = ints[shifted.length - 2];
    shifted[1] = ints[shifted.length - 1];
    return new GenericUInt(shifted);
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

  private int[] getIntSubRange(int from, int to) {
    // big-endian order so need to flip indexes
    int fromFlipped = ints.length - from - (to - from);
    int toFlipped = ints.length - from;
    return Arrays.copyOfRange(ints, fromFlipped, toFlipped);
  }

  private static long toULong(final int value) {
    return value & 0xffffffffL;
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
    byte[] padded = new byte[requiredBitLength / Byte.SIZE];
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

  private static int[] pad(int[] ints, int requiredBitLength) {
    int byteLength = requiredBitLength / Integer.SIZE;
    int[] padded = new int[byteLength];
    // assuming ints is shorter than required length
    System.arraycopy(ints, 0, padded, byteLength - ints.length, ints.length);
    return padded;
  }

  private static int[] longToInts(long value, int requiredBitLength) {
    int[] ints = new int[requiredBitLength / Integer.SIZE];
    ints[ints.length - 1] = (int) value;
    ints[ints.length - 2] = (int) (value >>> 32);
    return ints;
  }

  public static void runUInt() {
    GenericCompUIntFactory factory = new GenericCompUIntFactory();
    int numValues = 1000000;
    List<GenericUInt> left = new ArrayList<>(numValues);
    List<GenericUInt> right = new ArrayList<>(numValues);
    for (int i = 0; i < numValues; i++) {
      left.add(factory.createRandom());
      right.add(factory.createRandom());
    }
    GenericUInt other = UInt.innerProduct(left, right);
    System.out.println(other);
    long startTime = System.currentTimeMillis();
    GenericUInt inner = UInt.innerProduct(left, right);
    long endTime = System.currentTimeMillis();
    long duration = (endTime - startTime);
    System.out.println(inner);
    System.out.println(duration);
  }

  public static void runUInt128() {
    CompUInt128Factory factory = new CompUInt128Factory();
    int numValues = 1000000;
    List<UInt128> left = new ArrayList<>(numValues);
    List<UInt128> right = new ArrayList<>(numValues);
    for (int i = 0; i < numValues; i++) {
      left.add(factory.createRandom());
      right.add(factory.createRandom());
    }
    UInt128 other = UInt.innerProduct(left, right);
    System.out.println(other);
    long startTime = System.currentTimeMillis();
    UInt128 inner = UInt.innerProduct(left, right);
    long endTime = System.currentTimeMillis();
    long duration = (endTime - startTime);
    System.out.println(inner);
    System.out.println(duration);
  }

  public static void main(String[] args) {
    runUInt128();
    runUInt();
  }

}
