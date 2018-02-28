package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    // TODO get rid off extra multiplication
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
  public GenericCompUInt computeOverflow() {
    // TODO definitely wrong...
    GenericCompUInt low = new GenericCompUInt(getLeastSignificant(), ints.length * Integer.SIZE);
    return low.subtract(this).getMostSignificant();
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

  private static int[] initIntArray(int value, int requiredBitLength) {
    int[] temp = new int[requiredBitLength / Integer.SIZE];
    temp[temp.length - 1] = value;
    return temp;
  }

  private static void runUInt() {
    GenericCompUIntFactory factory = new GenericCompUIntFactory(64, 64);
    int numValues = 1000000;
    List<GenericCompUInt> left = new ArrayList<>(numValues);
    List<GenericCompUInt> right = new ArrayList<>(numValues);
    for (int i = 0; i < numValues; i++) {
      left.add(factory.createRandom());
      right.add(factory.createRandom());
    }
    GenericCompUInt other = UInt.innerProduct(left, right);
    long startTime = System.currentTimeMillis();
    GenericCompUInt inner = UInt.innerProduct(left, right);
    long endTime = System.currentTimeMillis();
    long duration = (endTime - startTime);
    System.out.println(inner);
    System.out.println(duration);
  }

  private static void runUInt128() {
    CompUInt128Factory factory = new CompUInt128Factory();
    int numValues = 1000000;
    List<CompUInt128> left = new ArrayList<>(numValues);
    List<CompUInt128> right = new ArrayList<>(numValues);
    for (int i = 0; i < numValues; i++) {
      left.add(factory.createRandom());
      right.add(factory.createRandom());
    }
    CompUInt128 other = UInt.innerProduct(left, right);
    System.out.println(other);
    long startTime = System.currentTimeMillis();
    CompUInt128 inner = UInt.innerProduct(left, right);
    long endTime = System.currentTimeMillis();
    long duration = (endTime - startTime);
    System.out.println(inner);
    System.out.println(duration);
  }

  public static void main(String[] args) {
    runUInt128();
    runUInt();
    runUInt128();
    runUInt();
  }

}
